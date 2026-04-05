package tn.farah.NetflixJava.utils;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.farah.NetflixJava.Controller.EpisodeViewController;
import tn.farah.NetflixJava.Controller.FilmPlayerController;
import tn.farah.NetflixJava.Controller.videoController;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;

import java.util.List;
import java.util.function.Consumer;

/**
 * CardFactory — fabrique centralisée pour toutes les cartes, popups et carousels.
 *
 * FIX PRINCIPAL : l'overlayPane est passé via un tableau à 1 élément (Pane[1])
 * ce qui permet de le capturer par référence dans les lambdas.
 * Ainsi même si overlayPane est null au moment de la construction du carousel,
 * il sera disponible au moment du hover (après que la scène soit prête).
 *
 * ADD-TO-LIST FIX : FavoriService est stocké statiquement (setFavoriService)
 * et le bouton + bascule en ✓ selon l'état réel en base.
 */
public class CardFactory {

    // ── Taille FIXE ───────────────────────────────────────────────
    public static final double CARD_W      = 260.0;
    public static final double CARD_H      = 146.0;
    public static final double GAP         = 12.0;
    public static final double HOVER_SCALE = 1.4;
    public static final double SCROLL_H    = CARD_H + 24.0;

    // ── Pour HomeController (responsive) ─────────────────────────
    public static final double CARDS_VISIBLE = 3.0;
    public static final double ASPECT        = 9.0 / 16.0;

    // ── FavoriService (set once at app startup) ───────────────────
    private static FavoriService favoriService;
    private static NotificationService notificationService;


    public static void setFavoriService(FavoriService service) {
        favoriService = service;
    }

    private static FavoriService getFavoriService() {
        return favoriService;
    }

    // ═══════════════════════════════════════════════════════════════
    //  OVERLAY — appelé une seule fois par controller
    // ═══════════════════════════════════════════════════════════════

    public static Pane createOverlay(javafx.scene.Scene scene, Pane[] overlayRef) {
        if (!(scene.getRoot() instanceof Pane root)) return null;
        if (overlayRef[0] != null) return overlayRef[0];

        Pane overlay = new Pane();
        overlay.setMouseTransparent(false);
        overlay.setPickOnBounds(false);
        overlay.setPrefSize(scene.getWidth(), scene.getHeight());
        scene.widthProperty() .addListener((o, ow, nw) -> overlay.setPrefWidth(nw.doubleValue()));
        scene.heightProperty().addListener((o, oh, nh) -> overlay.setPrefHeight(nh.doubleValue()));
        root.getChildren().add(overlay);

        overlayRef[0] = overlay;
        return overlay;
    }

    /** Version simple sans ref (pour compatibilité) */
    public static Pane createOverlay(javafx.scene.Scene scene) {
        Pane[] ref = new Pane[1];
        return createOverlay(scene, ref);
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSEL FIXE (Films & Series pages)
    // ═══════════════════════════════════════════════════════════════

    public static VBox buildFilmCarousel(String title, List<Film> films,
                                          Pane overlayPane, Consumer<Film> onFilmClick) {
        Pane[] overlayRef = { overlayPane };
        return buildFilmCarousel(title, films, overlayRef, onFilmClick);
    }

    public static VBox buildFilmCarousel(String title, List<Film> films,
                                          Pane[] overlayRef, Consumer<Film> onFilmClick) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = buildRow();
        films.forEach(f -> row.getChildren().add(buildFilmCard(f, CARD_W, CARD_H, overlayRef, onFilmClick)));

        ScrollPane scroll = buildFixedScrollPane(row);
        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    public static VBox buildSerieCarousel(String title, List<Serie> series,
                                           Pane overlayPane, Consumer<Serie> onSerieClick) {
        Pane[] overlayRef = { overlayPane };
        return buildSerieCarousel(title, series, overlayRef, onSerieClick);
    }

    public static VBox buildSerieCarousel(String title, List<Serie> series,
                                           Pane[] overlayRef, Consumer<Serie> onSerieClick) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = buildRow();
        series.forEach(s -> row.getChildren().add(buildSerieCard(s, CARD_W, CARD_H, overlayRef, onSerieClick)));

        ScrollPane scroll = buildFixedScrollPane(row);
        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSEL RESPONSIVE (HomeController)
    // ═══════════════════════════════════════════════════════════════

    public static VBox buildResponsiveFilmCarousel(String title, List<Film> films,
                                                    Pane overlayPane, Consumer<Film> onFilmClick) {
        Pane[] overlayRef = { overlayPane };
        return buildResponsiveFilmCarousel(title, films, overlayRef, onFilmClick);
    }

    public static VBox buildResponsiveFilmCarousel(String title, List<Film> films,
                                                    Pane[] overlayRef, Consumer<Film> onFilmClick) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = buildRow();
        ScrollPane scroll = buildResponsiveScrollPane(row);

        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            if (row.getUserData() != null) return;
            double cardW = computeCardWidth(newW.doubleValue());
            double cardH = cardW * ASPECT;
            scroll.setPrefHeight(cardH + 40);
            scroll.setMinHeight(cardH + 40);
            scroll.setMaxHeight(Double.MAX_VALUE);
            row.setUserData("built");
            row.getChildren().clear();
            films.forEach(f -> row.getChildren().add(
                buildFilmCard(f, cardW, cardH, overlayRef, onFilmClick)));
        });

        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    public static VBox buildResponsiveSerieCarousel(String title, List<Serie> series,
                                                     Pane overlayPane, Consumer<Serie> onSerieClick) {
        Pane[] overlayRef = { overlayPane };
        return buildResponsiveSerieCarousel(title, series, overlayRef, onSerieClick);
    }

    public static VBox buildResponsiveSerieCarousel(String title, List<Serie> series,
                                                     Pane[] overlayRef, Consumer<Serie> onSerieClick) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = buildRow();
        ScrollPane scroll = buildResponsiveScrollPane(row);

        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            if (row.getUserData() != null) return;
            double cardW = computeCardWidth(newW.doubleValue());
            double cardH = cardW * ASPECT;
            scroll.setPrefHeight(cardH + 40);
            scroll.setMinHeight(cardH + 40);
            scroll.setMaxHeight(Double.MAX_VALUE);
            row.setUserData("built");
            row.getChildren().clear();
            series.forEach(s -> row.getChildren().add(
                buildSerieCard(s, cardW, cardH, overlayRef, onSerieClick)));
        });

        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARTES
    // ═══════════════════════════════════════════════════════════════

    public static StackPane buildFilmCard(Film film, double cardW, double cardH,
                                           Pane overlayPane, Consumer<Film> onClick) {
        return buildFilmCard(film, cardW, cardH, new Pane[]{ overlayPane }, onClick);
    }

    public static StackPane buildFilmCard(Film film, double cardW, double cardH,
                                           Pane[] overlayRef, Consumer<Film> onClick) {
        Pane thumbnail = createThumbnail(film.getUrlImageCover(), cardW, cardH);

        StackPane card = new StackPane(thumbnail);
        card.setPrefSize(cardW, cardH);
        card.setMinSize(cardW, cardH);
        card.setMaxSize(cardW, cardH);
        card.setStyle("-fx-cursor: hand;");

        if (onClick != null)
            card.setOnMouseClicked(e -> onClick.accept(film));

        VBox popup = buildFilmPopup(film, cardW * HOVER_SCALE, onClick);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        attachHoverBehavior(card, popup, cardW, overlayRef);
        return card;
    }

    public static StackPane buildSerieCard(Serie serie, double cardW, double cardH,
                                            Pane overlayPane, Consumer<Serie> onClick) {
        return buildSerieCard(serie, cardW, cardH, new Pane[]{ overlayPane }, onClick);
    }

    public static StackPane buildSerieCard(Serie serie, double cardW, double cardH,
                                            Pane[] overlayRef, Consumer<Serie> onClick) {
        Pane thumbnail = createThumbnail(serie.getUrlImageCover(), cardW, cardH);

        Label badge = new Label("SÉRIE");
        badge.getStyleClass().add("serie-badge");
        StackPane.setAlignment(badge, Pos.BOTTOM_LEFT);

        StackPane card = new StackPane(thumbnail, badge);
        card.setPrefSize(cardW, cardH);
        card.setMinSize(cardW, cardH);
        card.setMaxSize(cardW, cardH);
        card.setStyle("-fx-cursor: hand;");

        if (onClick != null)
            card.setOnMouseClicked(e -> onClick.accept(serie));

        VBox popup = buildSeriePopup(serie, cardW * HOVER_SCALE, onClick);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        attachHoverBehavior(card, popup, cardW, overlayRef);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  POPUPS
    // ═══════════════════════════════════════════════════════════════

    public static VBox buildFilmPopup(Film film, double popupW) {
        return buildFilmPopup(film, popupW, null);
    }

    public static VBox buildFilmPopup(Film film, double popupW, Consumer<Film> onInfo) {
        double thumbH = popupW * ASPECT;
        Pane bigThumb = createThumbnail(film.getUrlImageCover(), popupW, thumbH);

        int matchPct = computeMatch(film.getTitre());
        String age   = film.getAgeRating() != null ? film.getAgeRating().name() : "PG";

        Label titleLbl = new Label(film.getTitre() != null ? film.getTitre() : "");
        titleLbl.getStyleClass().add("popup-title");
        titleLbl.setMaxWidth(popupW - 28);

        Label matchLbl = new Label(matchPct + "% Match"); matchLbl.getStyleClass().add("popup-match");
        Label durLbl   = new Label(film.getDuree() + "m"); durLbl.getStyleClass().add("popup-meta");
        Label ageLbl   = new Label(age);  ageLbl.getStyleClass().addAll("popup-meta", "popup-badge");
        Label hdLbl    = new Label("HD"); hdLbl .getStyleClass().addAll("popup-meta", "popup-badge");
        Label typeLbl  = new Label("🎬 FILM"); typeLbl.getStyleClass().add("popup-type-film");

        HBox meta = new HBox(8, matchLbl, durLbl, ageLbl, hdLbl, typeLbl);
        meta.setAlignment(Pos.CENTER_LEFT);

        String genreText = "—";
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreText = film.getGenres().stream()
                .map(Category::getName).limit(2)
                .reduce((a, b) -> a + " · " + b).orElse("—");
        }
        Label genreLbl = new Label(genreText);
        genreLbl.getStyleClass().add("popup-genre");

        VBox info = buildPopupInfo(
        	    popupW, titleLbl, meta, genreLbl,
        	    onInfo != null ? () -> onInfo.accept(film) : null,
        	    () -> lancerFilm(film),           // ← onPlayAction null pour les films
        	    getFavoriService(),
        	    film.getId()
        	);
        return assemblePopup(popupW, thumbH, bigThumb, info);
    }

    public static VBox buildSeriePopup(Serie serie, double popupW) {
        return buildSeriePopup(serie, popupW, null);
    }

    public static VBox buildSeriePopup(Serie serie, double popupW, Consumer<Serie> onInfo) {
        double thumbH = popupW * ASPECT;
        Pane bigThumb = createThumbnail(serie.getUrlImageCover(), popupW, thumbH);

        int matchPct = computeMatch(serie.getTitre());

        Label titleLbl = new Label(serie.getTitre() != null ? serie.getTitre() : "");
        titleLbl.getStyleClass().add("popup-title");
        titleLbl.setMaxWidth(popupW - 28);

        Label matchLbl  = new Label(matchPct + "% Match"); matchLbl.getStyleClass().add("popup-match");
        Label statusLbl = new Label(serie.isTerminee() ? "Terminée" : "En cours");
        statusLbl.getStyleClass().add(serie.isTerminee() ? "popup-badge-ended" : "popup-badge-ongoing");
        Label typeLbl   = new Label("📺 SÉRIE"); typeLbl.getStyleClass().add("popup-type-serie");

        HBox meta = new HBox(8, matchLbl, statusLbl, typeLbl);
        meta.setAlignment(Pos.CENTER_LEFT);

        String genreText = "—";
        if (serie.getGenres() != null && !serie.getGenres().isEmpty()) {
            genreText = serie.getGenres().stream()
                .map(Category::getName).limit(2)
                .reduce((a, b) -> a + " · " + b).orElse("—");
        }
        Label genreLbl = new Label(genreText);
        genreLbl.getStyleClass().add("popup-genre");

        VBox info = buildPopupInfo(
        	    popupW, titleLbl, meta, genreLbl,
        	    onInfo != null ? () -> onInfo.accept(serie) : null,
        	    () -> lancerPremierEpisode(serie),           // ← onPlayAction null pour les films
        	    getFavoriService(),
        	    serie.getId()
        	);
        return assemblePopup(popupW, thumbH, bigThumb, info);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SECTION HEADER
    // ═══════════════════════════════════════════════════════════════

    public static HBox buildSectionHeader(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-header");

        Separator sep = new Separator();
        sep.getStyleClass().add("section-separator");
        HBox.setHgrow(sep, Priority.ALWAYS);

        HBox header = new HBox(16, label, sep);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("section-header-box");
        return header;
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS PRIVÉS
    // ═══════════════════════════════════════════════════════════════

    private static HBox buildRow() {
        HBox row = new HBox(GAP);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("carousel-row");
        row.setPrefHeight(CARD_H);
        row.setMinHeight(CARD_H);
        row.setMaxHeight(CARD_H);
        return row;
    }

    private static ScrollPane buildFixedScrollPane(HBox row) {
        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToHeight(true);
        scroll.setFitToWidth(false);
        scroll.setPrefHeight(SCROLL_H);
        scroll.setMinHeight(SCROLL_H);
        scroll.setMaxHeight(SCROLL_H);
        scroll.getStyleClass().add("carousel-scroll");
        
        return scroll;
    }

    private static ScrollPane buildResponsiveScrollPane(HBox row) {
        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("carousel-scroll");
        return scroll;
    }

    public static Pane createThumbnail(String imageUrl, double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setMinSize(w, h);
        pane.setMaxSize(w, h);
        pane.setStyle(
            (imageUrl != null && !imageUrl.isEmpty())
                ? "-fx-background-image: url('" + imageUrl + "');" +
                  "-fx-background-size: cover; -fx-background-position: center;"
                : "-fx-background-color: #1a1a1a;"
        );
        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(6); clip.setArcHeight(6);
        pane.setClip(clip);
        return pane;
    }

    /**
     * Boutons du popup.
     * ✅ addBtn toggle : lit l'état réel depuis FavoriService à chaque clic.
     * ✅ userId lu depuis SessionManager au moment du clic (jamais mis en cache).
     */
    private static VBox buildPopupInfo(double popupW, Label titleLbl,
            HBox meta, Label genreLbl,
            Runnable onInfoAction,
            Runnable onPlayAction,       // ← NOUVEAU
            FavoriService favoriService,
            int mediaId) {
        Button playBtn = new Button("▶"); playBtn.getStyleClass().add("btn-round-white");
        Button likeBtn = new Button("♥"); likeBtn.getStyleClass().add("btn-round-outline");
        Button infoBtn = new Button("ℹ"); infoBtn.getStyleClass().add("btn-round-outline");
        Region spacer  = new Region();    HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Add-to-list button ────────────────────────────────────
        int userId = SessionManager.getInstance().getCurrentUserId();
        boolean alreadyFav = favoriService != null && userId > 0
                             && favoriService.estFavori(userId, mediaId);

        Button addBtn = new Button(alreadyFav ? "  ✓" : "  +");
        addBtn.getStyleClass().add(alreadyFav ? "btn-round-white" : "btn-round-outline");

        if (favoriService != null && userId > 0) {
            addBtn.setOnAction(e -> {
                int uid = SessionManager.getInstance().getCurrentUserId();
                if (uid <= 0) return;

                boolean isFav = favoriService.estFavori(uid, mediaId);
                if (isFav) {
                    favoriService.supprimerFavori(uid, mediaId);
                    addBtn.setText("+");
                    addBtn.getStyleClass().setAll("btn-round-outline");
                } else {
                    Favori favori = new Favori();
                    favori.setUserId(uid);
                    favori.setMediaId(mediaId);
                    favoriService.ajouterFavori(favori);
                    addBtn.setText("✓");
                    addBtn.getStyleClass().setAll("btn-round-white");
                    if (notificationService != null) {
                        Notification n = new Notification(0, uid, "FAVORI",
                            "Ajouté à Ma Liste",
                            "\"" + titleLbl.getText() + "\" a été ajouté à votre liste.",
                            java.time.LocalDate.now().toString(),
                            false, false);
                        notificationService.addNotification(n);
                    }
                    
                }
            });
            addBtn.setStyle("-fx-cursor: hand;");
        }
        // ─────────────────────────────────────────────────────────

        if (onInfoAction != null) {
            infoBtn.setOnAction(e -> onInfoAction.run());
            infoBtn.setStyle("-fx-cursor: hand;");
        }

        if (onPlayAction != null) {
            playBtn.setOnAction(e -> onPlayAction.run());
            playBtn.setStyle("-fx-cursor: hand;");
        } else {
        }
    
        final boolean[] liked = { false };
        likeBtn.setOnAction(e -> {
            liked[0] = !liked[0];
            if (liked[0]) {
                likeBtn.setStyle("-fx-background-color: #E50914; -fx-text-fill: white; -fx-background-radius: 50%; -fx-cursor: hand;");
            } else {
                likeBtn.getStyleClass().setAll("btn-round-outline");
                likeBtn.setStyle("-fx-cursor: hand;");
            }
        });

        HBox actions = new HBox(8, playBtn, addBtn, likeBtn, spacer, infoBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(10, actions, titleLbl, meta, genreLbl);
        info.getStyleClass().add("popup-info");
        info.setPrefWidth(popupW);
        return info;
    }

    private static VBox assemblePopup(double popupW, double thumbH, Pane bigThumb, VBox info) {
        VBox container = new VBox(0, bigThumb, info);
        container.setPrefWidth(popupW);
        container.setMinWidth(popupW);
        container.setMaxWidth(popupW);
        Rectangle clip = new Rectangle(popupW, thumbH + 130);
        clip.setArcWidth(10); clip.setArcHeight(10);
        container.setClip(clip);
        return container;
    }

    /**
     * FIX OVERLAY NULL :
     * On utilise overlayRef[0] au moment du hover (pas au moment de la construction).
     */
    private static void attachHoverBehavior(StackPane card, VBox popup,
                                             double cardW, Pane[] overlayRef) {
        double popupW = cardW * HOVER_SCALE;

        Timeline showTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            Pane overlay = overlayRef[0];
            if (overlay == null) return;

            Bounds b    = card.localToScene(card.getBoundsInLocal());
            double x    = b.getMinX() - (popupW - cardW) / 2.0;
            double y    = b.getMinY() - 20;
            double maxX = overlay.getScene().getWidth() - popupW - 10;

            popup.setLayoutX(Math.max(10, Math.min(x, maxX)));
            popup.setLayoutY(Math.max(10, y));
            if (!overlay.getChildren().contains(popup))
                overlay.getChildren().add(popup);

            popup.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(180), popup);
            ft.setToValue(1.0);
            ft.play();
        }));

        Timeline hideTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), popup);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                popup.setVisible(false);
                Pane overlay = overlayRef[0];
                if (overlay != null) overlay.getChildren().remove(popup);
            });
            ft.play();
        }));

        card.setOnMouseEntered(e -> { hideTimer.stop(); showTimer.playFromStart(); });
        card.setOnMouseExited (e -> { showTimer.stop(); hideTimer.playFromStart(); });
        popup.setPickOnBounds(false);
        popup.setMouseTransparent(false);
        popup.setOnMouseEntered(e -> hideTimer.stop());
        popup.setOnMouseExited (e -> hideTimer.playFromStart());
    }

    public static double computeCardWidth(double scrollWidth) {
        double available = scrollWidth - 80;
        return (available - GAP * (CARDS_VISIBLE - 1)) / CARDS_VISIBLE;
    }

    public static int computeMatch(String titre) {
        return 60 + (Math.abs(titre != null ? titre.hashCode() : 0) % 35);
    }
    private static void lancerPremierEpisode(Serie serie) {
        // 1. Récupère la connexion et les services
        java.sql.Connection conn = ConxDB.getInstance();
        SaisonService saisonService = new SaisonService(conn);
        SerieService serieService = new SerieService(conn);

        // 2. Trouve le premier épisode
        int firstSaisonId = saisonService.findFirstSeasonIdBySerie(serie.getId());
        if (firstSaisonId == -1) return;

        List<tn.farah.NetflixJava.Entities.Episode> episodes =
            serieService.findEpisodeBySaison(firstSaisonId);
        if (episodes == null || episodes.isEmpty()) return;

        int episodeId = episodes.get(0).getId();

        // 3. Navigate vers le lecteur
        videoController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.video); // adapte au nom de ton Screen
        if (ctrl != null) ctrl.initEpisode(episodeId);
    }
    public static void setNotificationService(NotificationService service) {
        notificationService = service;
    }
    private static void lancerFilm(Film film) {
        FilmPlayerController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.filmPlayer); // adapte au nom de ton Screen
        if (ctrl != null) ctrl.initFilm(film);
    }
}