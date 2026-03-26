package tn.farah.NetflixJava.Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    // ── Injections FXML ──────────────────────────────────────────
    @FXML private StackPane heroSection;
    @FXML private Pane      heroBackdrop;
    @FXML private Label     heroTitle, heroGenreBadge, heroType,
                             heroRating, heroDuration, heroYear, heroCertif, heroSynopsis;
    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;

    // ── Services ─────────────────────────────────────────────────
    private FilmService  filmService;
    private SerieService serieService;
    private Pane         overlayPane;

    // ── État Hero ─────────────────────────────────────────────────
    private final List<Film> heroFilms  = new ArrayList<>();
    private int               heroIndex = 0;
    private ScaleTransition   heroZoom;
    private Timeline          heroTimer;

    // ── Constantes cartes ─────────────────────────────────────────
    private static final double CARDS_VISIBLE = 3.0;
    private static final double GAP           = 12.0;
    private static final double ASPECT        = 9.0 / 16.0;
    private static final double HOVER_SCALE   = 2.0;

    // ═══════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        if (connection == null) return;

        filmService  = new FilmService(connection);
        serieService = new SerieService();          // static DAO, pas besoin de connection

        initHero();
        loadAllCarousels();

        if (avatarLabel != null) avatarLabel.setText("U");

        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> initOverlay(scene));
        });
    }

    private void initOverlay(javafx.scene.Scene scene) {
        if (overlayPane != null) return;
        if (!(scene.getRoot() instanceof Pane root)) return;

        overlayPane = new Pane();
        overlayPane.setMouseTransparent(true);
        overlayPane.setPrefSize(scene.getWidth(), scene.getHeight());
        scene.widthProperty() .addListener((o, ow, nw) -> overlayPane.setPrefWidth(nw.doubleValue()));
        scene.heightProperty().addListener((o, oh, nh) -> overlayPane.setPrefHeight(nh.doubleValue()));
        root.getChildren().add(overlayPane);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HERO (Films uniquement)
    // ═══════════════════════════════════════════════════════════════

    private void initHero() {
        try {
            List<Film> films = filmService.getAllFilmsSorted();
            if (films.isEmpty()) return;

            films.stream().limit(5).forEach(heroFilms::add);
            showHeroFilm(0);

            heroTimer = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
                heroIndex = (heroIndex + 1) % heroFilms.size();
                showHeroFilm(heroIndex);
            }));
            heroTimer.setCycleCount(Timeline.INDEFINITE);
            heroTimer.play();

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void showHeroFilm(int index) {
        Film film = heroFilms.get(index);

        if (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty()) {
            heroBackdrop.setStyle(
                "-fx-background-image: url('" + film.getUrlImageCover() + "');" +
                "-fx-background-size: cover; -fx-background-position: center top;"
            );
        }

        if (heroZoom != null) heroZoom.stop();
        heroZoom = new ScaleTransition(Duration.seconds(10), heroBackdrop);
        heroZoom.setFromX(1.0); heroZoom.setToX(1.08);
        heroZoom.setFromY(1.0); heroZoom.setToY(1.08);
        heroZoom.setAutoReverse(true);
        heroZoom.setCycleCount(Timeline.INDEFINITE);
        heroZoom.play();

        heroTitle   .setText(film.getTitre()   != null ? film.getTitre().toUpperCase() : "");
        heroSynopsis.setText(film.getSynopsis() != null ? film.getSynopsis() : "");
        heroRating  .setText(String.format("%.0f%% Match", film.getRatingMoyen() * 10));
        heroDuration.setText(film.getDuree() + "m");
        heroType    .setText("FILM");
        if (heroGenreBadge != null) heroGenreBadge.setText("");
        if (film.getDateSortie() != null) heroYear  .setText(String.valueOf(film.getDateSortie().getYear()));
        if (film.getAgeRating()  != null) heroCertif.setText(film.getAgeRating().name());

        FadeTransition fade = new FadeTransition(Duration.millis(600), heroSection);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.play();
    }

    // ═══════════════════════════════════════════════════════════════
    //  CHARGEMENT DE TOUS LES CAROUSELS
    // ═══════════════════════════════════════════════════════════════

    private void loadAllCarousels() {
        try {
            List<Film>  allFilms  = filmService.getAllFilmsSorted();
            List<Serie> allSeries = serieService.getAllSeries();

            // ── 1. Top Films ──────────────────────────────────────
            if (!allFilms.isEmpty()) {
                carouselContainer.getChildren().add(
                    buildSectionHeader("🎬  Films")
                );
                carouselContainer.getChildren().add(
                    buildFilmCarousel("Top Films", allFilms)
                );
            }

            // ── 2. Top Séries ─────────────────────────────────────
            if (!allSeries.isEmpty()) {
                carouselContainer.getChildren().add(
                    buildSectionHeader("📺  Séries")
                );
                carouselContainer.getChildren().add(
                    buildSerieCarousel("Top Séries", allSeries)
                );
            }

            // ── 3. Carousels Films par catégorie ──────────────────
            if (!allFilms.isEmpty()) {
                Map<String, List<Film>> filmsByCategory = filmService.getAllFilmsByCategory();

                // Trier les catégories alphabétiquement
                new TreeMap<>(filmsByCategory).forEach((category, films) -> {
                    if (films.size() >= 2) {   // n'affiche que si au moins 2 films
                        carouselContainer.getChildren().add(
                            buildFilmCarousel("🎬 " + category, films)
                        );
                    }
                });
            }

            // ── 4. Carousels Séries par genre ─────────────────────
            if (!allSeries.isEmpty()) {
                Map<String, List<Serie>> seriesByGenre = serieService.getAllSeriesByGenre();

                new TreeMap<>(seriesByGenre).forEach((genre, series) -> {
                    if (series.size() >= 2) {
                        carouselContainer.getChildren().add(
                            buildSerieCarousel("📺 " + genre, series)
                        );
                    }
                });
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SECTION HEADER (séparateur visuel entre Films / Séries)
    // ═══════════════════════════════════════════════════════════════

    private HBox buildSectionHeader(String text) {
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
    //  CAROUSEL FILMS
    // ═══════════════════════════════════════════════════════════════

    private VBox buildFilmCarousel(String title, List<Film> films) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = new HBox(GAP);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("carousel-row");

        ScrollPane scroll = buildScrollPane(row);

        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            if (row.getUserData() != null) return;
            double cardW = computeCardWidth(newW.doubleValue());
            double cardH = cardW * ASPECT;

            scroll.setPrefHeight(cardH + 40);
            scroll.setMinHeight(cardH + 40);
            scroll.setMaxHeight(Double.MAX_VALUE);

            row.setUserData("built");
            row.getChildren().clear();
            films.forEach(f -> row.getChildren().add(buildFilmCard(f, cardW, cardH)));
        });

        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSEL SÉRIES
    // ═══════════════════════════════════════════════════════════════

    private VBox buildSerieCarousel(String title, List<Serie> series) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = new HBox(GAP);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("carousel-row");

        ScrollPane scroll = buildScrollPane(row);

        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            if (row.getUserData() != null) return;
            double cardW = computeCardWidth(newW.doubleValue());
            double cardH = cardW * ASPECT;

            scroll.setPrefHeight(cardH + 40);
            scroll.setMinHeight(cardH + 40);
            scroll.setMaxHeight(Double.MAX_VALUE);

            row.setUserData("built");
            row.getChildren().clear();
            series.forEach(s -> row.getChildren().add(buildSerieCard(s, cardW, cardH)));
        });

        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARTE FILM
    // ═══════════════════════════════════════════════════════════════

    private StackPane buildFilmCard(Film film, double cardW, double cardH) {
        Pane thumbnail = createThumbnail(film.getUrlImageCover(), cardW, cardH);

        StackPane card = new StackPane(thumbnail);
        card.setPrefSize(cardW, cardH);
        card.setMinSize(cardW, cardH);
        card.setMaxSize(cardW, cardH);
        card.setStyle("-fx-cursor: hand;");

        VBox popup = buildFilmPopup(film, cardW * HOVER_SCALE);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        attachHoverBehavior(card, popup, cardW);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARTE SÉRIE
    // ═══════════════════════════════════════════════════════════════

    private StackPane buildSerieCard(Serie serie, double cardW, double cardH) {
        Pane thumbnail = createThumbnail(serie.getUrlImageCover(), cardW, cardH);

        // Badge "SÉRIE" en bas à gauche
        Label badge = new Label("SÉRIE");
        badge.getStyleClass().add("serie-badge");
        StackPane.setAlignment(badge, Pos.BOTTOM_LEFT);

        StackPane card = new StackPane(thumbnail, badge);
        card.setPrefSize(cardW, cardH);
        card.setMinSize(cardW, cardH);
        card.setMaxSize(cardW, cardH);
        card.setStyle("-fx-cursor: hand;");

        VBox popup = buildSeriePopup(serie, cardW * HOVER_SCALE);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        attachHoverBehavior(card, popup, cardW);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  POPUP FILM
    // ═══════════════════════════════════════════════════════════════

    private VBox buildFilmPopup(Film film, double popupW) {
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

        // Genre depuis Set<Category>
        String genreText = "—";
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreText = film.getGenres().stream()
                .map(c -> c.getName())          // adapte si le getter s'appelle autrement
                .limit(2)
                .reduce((a, b) -> a + " · " + b)
                .orElse("—");
        }
        Label genreLbl = new Label(genreText);
        genreLbl.getStyleClass().add("popup-genre");

        VBox info = buildPopupInfo(popupW, titleLbl, meta, genreLbl);

        return assemblePopup(popupW, thumbH, bigThumb, info);
    }

    // ═══════════════════════════════════════════════════════════════
    //  POPUP SÉRIE
    // ═══════════════════════════════════════════════════════════════

    private VBox buildSeriePopup(Serie serie, double popupW) {
        double thumbH = popupW * ASPECT;
        Pane bigThumb = createThumbnail(serie.getUrlImageCover(), popupW, thumbH);

        int matchPct = computeMatch(serie.getTitre());

        Label titleLbl = new Label(serie.getTitre() != null ? serie.getTitre() : "");
        titleLbl.getStyleClass().add("popup-title");
        titleLbl.setMaxWidth(popupW - 28);

        Label matchLbl   = new Label(matchPct + "% Match");    matchLbl.getStyleClass().add("popup-match");
        Label statusLbl  = new Label(serie.isTerminee() ? "Terminée" : "En cours");
        statusLbl.getStyleClass().add(serie.isTerminee() ? "popup-badge-ended" : "popup-badge-ongoing");
        Label typeLbl    = new Label("📺 SÉRIE");              typeLbl.getStyleClass().add("popup-type-serie");

        HBox meta = new HBox(8, matchLbl, statusLbl, typeLbl);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label genreLbl = new Label(serie.getGenre() != null ? serie.getGenre() : "—");
        genreLbl.getStyleClass().add("popup-genre");

        VBox info = buildPopupInfo(popupW, titleLbl, meta, genreLbl);

        return assemblePopup(popupW, thumbH, bigThumb, info);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS PARTAGÉS
    // ═══════════════════════════════════════════════════════════════

    /** ScrollPane configuré de façon identique pour films et séries */
    private ScrollPane buildScrollPane(HBox row) {
        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("carousel-scroll");
        return scroll;
    }

    /** Largeur d'une carte en fonction de la largeur du scroll (padding 80) */
    private double computeCardWidth(double scrollWidth) {
        double available = scrollWidth - 80;
        return (available - GAP * (CARDS_VISIBLE - 1)) / CARDS_VISIBLE;
    }

    /** Thumbnail avec clip arrondi — utilisé par films ET séries */
    private Pane createThumbnail(String imageUrl, double w, double h) {
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

    /** Partie info commune du popup (actions + titre + meta + genre) */
    private VBox buildPopupInfo(double popupW, Label titleLbl, HBox meta, Label genreLbl) {
        Button playBtn = new Button("▶"); playBtn.getStyleClass().add("btn-round-white");
        Button addBtn  = new Button("+"); addBtn .getStyleClass().add("btn-round-outline");
        Button likeBtn = new Button("♥"); likeBtn.getStyleClass().add("btn-round-outline");
        Button moreBtn = new Button("▾"); moreBtn.getStyleClass().add("btn-round-outline");
        Region spacer  = new Region();    HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8, playBtn, addBtn, likeBtn, spacer, moreBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(10, actions, titleLbl, meta, genreLbl);
        info.getStyleClass().add("popup-info");
        info.setPrefWidth(popupW);
        return info;
    }

    /** Assemble thumbnail + info dans le conteneur final avec clip arrondi */
    private VBox assemblePopup(double popupW, double thumbH, Pane bigThumb, VBox info) {
        VBox container = new VBox(0, bigThumb, info);
        container.setPrefWidth(popupW);
        container.setMinWidth(popupW);
        container.setMaxWidth(popupW);

        Rectangle clip = new Rectangle(popupW, thumbH + 130);
        clip.setArcWidth(10); clip.setArcHeight(10);
        container.setClip(clip);
        return container;
    }

    /** Timers show/hide + handlers souris partagés cartes films et séries */
    private void attachHoverBehavior(StackPane card, VBox popup, double cardW) {
        double popupW = cardW * HOVER_SCALE;

        Timeline showTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            if (overlayPane == null) return;
            Bounds b    = card.localToScene(card.getBoundsInLocal());
            double x    = b.getMinX() - (popupW - cardW) / 2.0;
            double y    = b.getMinY() - 20;
            double maxX = overlayPane.getScene().getWidth() - popupW - 10;

            popup.setLayoutX(Math.max(10, Math.min(x, maxX)));
            popup.setLayoutY(Math.max(10, y));
            if (!overlayPane.getChildren().contains(popup))
                overlayPane.getChildren().add(popup);

            popup.setVisible(true);
            FadeTransition showFade = new FadeTransition(Duration.millis(180), popup);
            showFade.setToValue(1.0);
            showFade.play();
        }));

        Timeline hideTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), popup);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                popup.setVisible(false);
                if (overlayPane != null) overlayPane.getChildren().remove(popup);
            });
            ft.play();
        }));

        card .setOnMouseEntered(e -> { hideTimer.stop(); showTimer.playFromStart(); });
        card .setOnMouseExited (e -> { showTimer.stop(); hideTimer.playFromStart(); });
        popup.setOnMouseEntered(e ->   hideTimer.stop());
        popup.setOnMouseExited (e ->   hideTimer.playFromStart());
        popup.setMouseTransparent(false);
    }

    /** Calcul déterministe du % match à partir du titre */
    private int computeMatch(String titre) {
        return 60 + (Math.abs(titre != null ? titre.hashCode() : 0) % 35);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HANDLERS FXML
    // ═══════════════════════════════════════════════════════════════

    @FXML private void onMyList()            { System.out.println("Ma Liste"); }
    @FXML private void onMovies()            { System.out.println("Films"); }
    @FXML private void onSeries()            { System.out.println("Séries"); }
    @FXML private void onSearch()            { System.out.println("Recherche : " + (searchField != null ? searchField.getText() : "")); }
    @FXML private void onSearchBtn()         { onSearch(); }
    @FXML private void onPlayFeatured()      { System.out.println("Lecture"); }
    @FXML private void onMoreInfoFeatured()  { System.out.println("Plus d'infos"); }
    @FXML private void onAddFeaturedToList() { System.out.println("Ajout à la liste"); }
}