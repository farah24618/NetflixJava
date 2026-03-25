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
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.utils.ConxDB;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    // ── État interne ─────────────────────────────────────────────
    private FilmService filmService;
    private Pane        overlayPane;

    private final List<Film> heroFilms    = new ArrayList<>();
    private int               heroIndex   = 0;
    private ScaleTransition   heroZoom;
    private Timeline          heroTimer;

    // ── Constantes de layout des cartes ──────────────────────────
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

        filmService = new FilmService(connection);

        initHero();
        loadCarousels();
        if (avatarLabel != null) avatarLabel.setText("U");

        // Crée l'overlayPane dès que la scène est disponible
        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> initOverlay(scene));
        });
    }

    /** Ajoute un Pane transparent au-dessus de tout pour afficher les popups. */
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
    //  HERO
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

        // Image de fond
        if (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty()) {
            heroBackdrop.setStyle(
                "-fx-background-image: url('" + film.getUrlImageCover() + "');" +
                "-fx-background-size: cover; -fx-background-position: center top;"
            );
            // Note : seule l'URL dynamique reste en setStyle — impossible autrement
        }

        // Animation zoom continu sur le backdrop
        if (heroZoom != null) heroZoom.stop();
        heroZoom = new ScaleTransition(Duration.seconds(10), heroBackdrop);
        heroZoom.setFromX(1.0); heroZoom.setToX(1.08);
        heroZoom.setFromY(1.0); heroZoom.setToY(1.08);
        heroZoom.setAutoReverse(true);
        heroZoom.setCycleCount(Timeline.INDEFINITE);
        heroZoom.play();

        // Remplissage des labels (logique pure, aucun style)
        heroTitle   .setText(film.getTitre()   != null ? film.getTitre().toUpperCase() : "");
        heroSynopsis.setText(film.getSynopsis() != null ? film.getSynopsis() : "");
        heroRating  .setText(String.format("%.0f%% Match", film.getRatingMoyen() * 10));
        heroDuration.setText(film.getDuree() + "m");
        heroType    .setText("FILM");
        if (heroGenreBadge != null) heroGenreBadge.setText("");
        if (film.getDateSortie()  != null) heroYear  .setText(String.valueOf(film.getDateSortie().getYear()));
        if (film.getAgeRating()   != null) heroCertif.setText(film.getAgeRating().name());

        // Fondu d'entrée
        FadeTransition fade = new FadeTransition(Duration.millis(600), heroSection);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.play();
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSELS
    // ═══════════════════════════════════════════════════════════════

    private void loadCarousels() {
        try {
            List<Film> films = filmService.getAllFilmsSorted();
            if (films.isEmpty()) return;
            carouselContainer.getChildren().addAll(
                buildCarousel("Top Films",       films),
                buildCarousel("Recently Added",  films)
            );
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox buildCarousel(String title, List<Film> films) {
        // Structure déclarée en code car elle dépend de données dynamiques
        // (largeur des cartes calculée à l'exécution)
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = new HBox(GAP);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("carousel-row");

        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("carousel-scroll");

        // Calcul réactif de la taille des cartes selon la largeur disponible
        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            if (row.getUserData() != null) return;
            double available = newW.doubleValue() - 80; // padding gauche+droite
            double cardW = (available - GAP * (CARDS_VISIBLE - 1)) / CARDS_VISIBLE;
            double cardH = cardW * ASPECT;

            scroll.setPrefHeight(cardH + 40);
            scroll.setMinHeight(cardH + 40);
            scroll.setMaxHeight(Double.MAX_VALUE);

            row.setUserData("built");
            row.getChildren().clear();
            films.forEach(f -> row.getChildren().add(buildCard(f, cardW, cardH)));
        });

        VBox bloc = new VBox(10);
        bloc.getStyleClass().add("carousel-block");
        bloc.getChildren().addAll(titleLabel, scroll);
        return bloc;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARTE FILM
    // ═══════════════════════════════════════════════════════════════

    private StackPane buildCard(Film film, double cardW, double cardH) {
        Pane thumbnail = createThumbnail(film, cardW, cardH);

        StackPane card = new StackPane(thumbnail);
        card.setPrefSize(cardW, cardH);
        card.setMinSize(cardW, cardH);
        card.setMaxSize(cardW, cardH);
        card.setStyle("-fx-cursor: hand;");

        // Popup (construit une seule fois par carte)
        VBox popup = buildPopup(film, cardW * HOVER_SCALE);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        attachHoverBehavior(card, popup, cardW);
        return card;
    }

    /** Crée le panneau image avec clip arrondi. */
    private Pane createThumbnail(Film film, double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setMinSize(w, h);
        pane.setMaxSize(w, h);

        String bg = (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty())
            ? "-fx-background-image: url('" + film.getUrlImageCover() + "');" +
              "-fx-background-size: cover; -fx-background-position: center;"
            : "-fx-background-color: #1a1a1a;";
        pane.setStyle(bg);

        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(6); clip.setArcHeight(6);
        pane.setClip(clip);
        return pane;
    }

    /** Attache les timers show/hide et les handlers de survol. */
    private void attachHoverBehavior(StackPane card, VBox popup, double cardW) {
        double popupW = cardW * HOVER_SCALE;

        Timeline showTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            if (overlayPane == null) return;
            Bounds b = card.localToScene(card.getBoundsInLocal());

            double x = b.getMinX() - (popupW - cardW) / 2.0;
            double y = b.getMinY() - 20;
            double maxX = overlayPane.getScene().getWidth() - popupW - 10;

            popup.setLayoutX(Math.max(10, Math.min(x, maxX)));
            popup.setLayoutY(Math.max(10, y));
            if (!overlayPane.getChildren().contains(popup))
                overlayPane.getChildren().add(popup);

            popup.setVisible(true);
            FadeTransition showFade = new FadeTransition(Duration.millis(180), popup);
            showFade.setToValue(1.0);
            showFade.play();        }));

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

    // ═══════════════════════════════════════════════════════════════
    //  POPUP AU SURVOL
    // ═══════════════════════════════════════════════════════════════

    private VBox buildPopup(Film film, double popupW) {
        double thumbH = popupW * ASPECT;

        // Vignette agrandie
        Pane bigThumb = createThumbnail(film, popupW, thumbH);

        // Boutons d'action — styles via CSS
        Button playBtn = new Button("▶"); playBtn.getStyleClass().add("btn-round-white");
        Button addBtn  = new Button("+"); addBtn .getStyleClass().add("btn-round-outline");
        Button likeBtn = new Button("♥"); likeBtn.getStyleClass().add("btn-round-outline");
        Button moreBtn = new Button("▾"); moreBtn.getStyleClass().add("btn-round-outline");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8, playBtn, addBtn, likeBtn, spacer, moreBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        // Titre
        Label titleLbl = new Label(film.getTitre() != null ? film.getTitre() : "");
        titleLbl.getStyleClass().add("popup-title");
        titleLbl.setMaxWidth(popupW - 28);
        titleLbl.setWrapText(false);

        // Méta
        int matchPct = 60 + (Math.abs(film.getTitre() != null ? film.getTitre().hashCode() : 0) % 35);
        Label matchLbl = new Label(matchPct + "% Match"); matchLbl.getStyleClass().add("popup-match");
        Label durLbl   = new Label(film.getDuree() + "m"); durLbl.getStyleClass().add("popup-meta");
        Label ageLbl   = new Label(film.getAgeRating() != null ? film.getAgeRating().name() : "PG");
        ageLbl.getStyleClass().addAll("popup-meta", "popup-badge");
        Label hdLbl    = new Label("HD"); hdLbl.getStyleClass().addAll("popup-meta", "popup-badge");

        HBox meta = new HBox(8, matchLbl, durLbl, ageLbl, hdLbl);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label genreLbl = new Label("Action · Drama");
        genreLbl.getStyleClass().add("popup-genre");

        VBox info = new VBox(10, actions, titleLbl, meta, genreLbl);
        info.getStyleClass().add("popup-info");
        info.setPrefWidth(popupW);

        VBox container = new VBox(0, bigThumb, info);
        container.setPrefWidth(popupW);
        container.setMinWidth(popupW);
        container.setMaxWidth(popupW);

        // Clip arrondi
        Rectangle clip = new Rectangle(popupW, thumbH + 130);
        clip.setArcWidth(10); clip.setArcHeight(10);
        container.setClip(clip);

        return container;
    }

    // ═══════════════════════════════════════════════════════════════
    //  HANDLERS FXML
    // ═══════════════════════════════════════════════════════════════

    @FXML private void onMyList()            { System.out.println("Ma Liste"); }
    @FXML private void onMovies()            { System.out.println("Films"); }
    @FXML private void onSeries()            { System.out.println("Séries"); }
    @FXML private void onSearch()            { System.out.println("Recherche : " + (searchField != null ? searchField.getText() : "")); }
    @FXML private void onSearchBtn()         { onSearch(); }
    @FXML private void onPlayFeatured()      { System.out.println("Lecture du film en vedette"); }
    @FXML private void onMoreInfoFeatured()  { System.out.println("Plus d'infos"); }
    @FXML private void onAddFeaturedToList() { System.out.println("Ajout à la liste"); }
}
/*package tn.farah.NetflixJava.Controller;


import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;



import javafx.animation.*;
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
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.utils.ConxDB;

public class HomeController implements Initializable {

    // FIX 1: Restored all @FXML labels that were in the FXML but removed
    @FXML private StackPane heroSection;
    @FXML private Pane      heroBackdrop;
    @FXML private Label     heroTitle, heroGenreBadge, heroType,
                             heroRating, heroDuration, heroYear, heroCertif, heroSynopsis;
    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;

    private Pane        overlayPane;
    private FilmService filmService;
    private Connection  connection;

    private final List<Film> listeFilmsHero = new ArrayList<>();
    private int             indexCourant   = 0;
    private ScaleTransition zoomActuel;
    private Timeline        heroTimer;

    private static final double CARDS_VISIBLE = 3.0;
    private static final double GAP           = 12;
    private static final double ASPECT        = 9.0 / 16.0;
    private static final double HOVER_SCALE   = 2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connection = ConxDB.getInstance();
        if (connection == null) {
			return;
		}
        filmService = new FilmService(connection);

        configurerHero();
        chargerCarousels();
        if (avatarLabel != null) {
			avatarLabel.setText("U");
		}

        // FIX 2: Guard against scene root not being a Pane
        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) {
				return;
			}
            // Use Platform.runLater to ensure scene is fully laid out
            javafx.application.Platform.runLater(() -> {
                if (overlayPane != null) {
					return;
				}
                javafx.scene.Parent root = scene.getRoot();
                if (root instanceof Pane) {
                    overlayPane = new Pane();
                    overlayPane.setMouseTransparent(true);
                    overlayPane.setStyle("-fx-background-color: transparent;");
                    overlayPane.setPrefSize(scene.getWidth(), scene.getHeight());
                    scene.widthProperty().addListener((o, ow, nw) -> overlayPane.setPrefWidth(nw.doubleValue()));
                    scene.heightProperty().addListener((o, oh, nh) -> overlayPane.setPrefHeight(nh.doubleValue()));
                    ((Pane) root).getChildren().add(overlayPane);
                }
            });
        });
    }

    // ══════════════════════════════════════════════════════
    //  HERO
    // ══════════════════════════════════════════════════════

    private void configurerHero() {
        try {
            List<Film> films = filmService.getAllFilmsSorted();
            if (films.isEmpty()) {
				return;
			}
            for (int i = 0; i < Math.min(5, films.size()); i++) {
				listeFilmsHero.add(films.get(i));
			}
            chargerElementHero(0);
            heroTimer = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
                indexCourant = (indexCourant + 1) % listeFilmsHero.size();
                chargerElementHero(indexCourant);
            }));
            heroTimer.setCycleCount(Timeline.INDEFINITE);
            heroTimer.play();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void chargerElementHero(int index) {
        Film film = listeFilmsHero.get(index);

        if (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty()) {
            heroBackdrop.setStyle(
                "-fx-background-image: url('" + film.getUrlImageCover() + "');" +
                "-fx-background-size: cover; -fx-background-position: center top;"
            );
        }

        if (zoomActuel != null) {
			zoomActuel.stop();
		}
        zoomActuel = new ScaleTransition(Duration.seconds(10), heroBackdrop);
        zoomActuel.setFromX(1.0); zoomActuel.setToX(1.08);
        zoomActuel.setFromY(1.0); zoomActuel.setToY(1.08);
        zoomActuel.setAutoReverse(true);
        zoomActuel.setCycleCount(Timeline.INDEFINITE);
        zoomActuel.play();

        // FIX 3: Null-safe getText calls
        heroTitle.setText(film.getTitre() != null ? film.getTitre().toUpperCase() : "");
        heroSynopsis.setText(film.getSynopsis() != null ? film.getSynopsis() : "");
        heroRating.setText(String.format("%.0f%% Match", film.getRatingMoyen() * 10));
        heroDuration.setText(film.getDuree() + "m");
        heroType.setText("FILM");
        if (heroGenreBadge != null) {
			heroGenreBadge.setText("");
		}
        if (film.getDateSortie() != null) {
			heroYear.setText(String.valueOf(film.getDateSortie().getYear()));
		}
        if (film.getAgeRating() != null) {
			heroCertif.setText(film.getAgeRating().name());
		}

        FadeTransition ft = new FadeTransition(Duration.millis(600), heroSection);
        ft.setFromValue(0.5);
        ft.setToValue(1.0);
        ft.play();
    }

    // ══════════════════════════════════════════════════════
    //  CAROUSELS
    // ══════════════════════════════════════════════════════

    private void chargerCarousels() {
        try {
            List<Film> tous = filmService.getAllFilmsSorted();
            if (!tous.isEmpty()) {
                carouselContainer.getChildren().add(creerCarousel("Top Films", tous));
                carouselContainer.getChildren().add(creerCarousel("Recently Added", tous));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox creerCarousel(String titre, List<Film> films) {
        VBox bloc = new VBox(10);
        bloc.setStyle("-fx-background-color: transparent;");

        Label labelTitre = new Label(titre);
        labelTitre.setStyle(
            "-fx-text-fill: #e5e5e5; -fx-font-size: 1.4em; -fx-font-weight: bold;" +
            "-fx-padding: 0 0 0 40;"
        );

        HBox rangee = new HBox(GAP);
        rangee.setAlignment(Pos.TOP_LEFT);
        // No padding on HBox — let ScrollPane handle it via fitToWidth
        rangee.setStyle("-fx-background-color: transparent;");

        ScrollPane scroll = new ScrollPane(rangee);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent;" +
            "-fx-border-color: transparent; -fx-padding: 0 40 0 40;"
        );

        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            if (rangee.getUserData() != null) {
				return;
			}
            // Full available width minus left+right padding (40+40=80)
            double w     = newW.doubleValue() - 80;
            double cardW = (w - GAP * (CARDS_VISIBLE - 1)) / CARDS_VISIBLE;
            double cardH = cardW * ASPECT;

            scroll.setPrefHeight(cardH + 40);
            scroll.setMinHeight(cardH + 40);
            scroll.setMaxHeight(Double.MAX_VALUE);

            rangee.setUserData("built");
            rangee.getChildren().clear();
            for (Film f : films) {
				rangee.getChildren().add(creerCarteFilm(f, cardW, cardH));
			}
        });

        bloc.getChildren().addAll(labelTitre, scroll);
        return bloc;
    }

    // ══════════════════════════════════════════════════════
    //  CARD
    // ══════════════════════════════════════════════════════

    private StackPane creerCarteFilm(Film film, double cardW, double cardH) {
        Pane thumbnail = new Pane();
        thumbnail.setPrefSize(cardW, cardH);
        thumbnail.setMinSize(cardW, cardH);
        thumbnail.setMaxSize(cardW, cardH);
        thumbnail.setStyle(
            (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty())
                ? "-fx-background-image: url('" + film.getUrlImageCover() + "');" +
                  "-fx-background-size: cover; -fx-background-position: center;"
                : "-fx-background-color: #1a1a1a;"
        );
        Rectangle clip = new Rectangle(cardW, cardH);
        clip.setArcWidth(6); clip.setArcHeight(6);
        thumbnail.setClip(clip);

        StackPane wrapper = new StackPane(thumbnail);
        wrapper.setPrefSize(cardW, cardH);
        wrapper.setMinSize(cardW, cardH);
        wrapper.setMaxSize(cardW, cardH);
        wrapper.setStyle("-fx-cursor: hand;");

        double popupW = cardW * HOVER_SCALE;
        VBox popup = buildPopup(film, popupW);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        // Delay timers to avoid flicker when mouse moves card→popup or popup→card
        Timeline showTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            if (overlayPane == null) {
				return;
			}
            Bounds b = wrapper.localToScene(wrapper.getBoundsInLocal());
            double popupX = b.getMinX() - (popupW - cardW) / 2.0;
            double popupY = b.getMinY() - 20;
            double maxX = overlayPane.getScene().getWidth() - popupW - 10;
            popup.setLayoutX(Math.max(10, Math.min(popupX, maxX)));
            popup.setLayoutY(Math.max(10, popupY));
            if (!overlayPane.getChildren().contains(popup)) {
				overlayPane.getChildren().add(popup);
			}
            popup.setVisible(true);
            FadeTransition ft = new FadeTransition(Duration.millis(180), popup);
            ft.setToValue(1); ft.play();
        }));

        Timeline hideTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), popup);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                popup.setVisible(false);
                if (overlayPane != null) {
					overlayPane.getChildren().remove(popup);
				}
            });
            ft.play();
        }));

        // Card hover
        wrapper.setOnMouseEntered(e -> {
            hideTimer.stop();
            showTimer.playFromStart();
        });
        wrapper.setOnMouseExited(e -> {
            showTimer.stop();
            hideTimer.playFromStart();
        });

        // Popup hover — keep it visible while mouse is on popup
        popup.setOnMouseEntered(e -> hideTimer.stop());
        popup.setOnMouseExited(e -> hideTimer.playFromStart());

        // CRITICAL: overlay is ALWAYS mouse-transparent
        // popup receives events directly since it's in the overlay pane
        // but we need overlay NOT transparent so popup can receive mouse
        // Solution: only the popup itself gets mouse events, overlay pane stays transparent
        popup.setMouseTransparent(false);

        return wrapper;
    }

    // ══════════════════════════════════════════════════════
    //  POPUP
    // ══════════════════════════════════════════════════════

    private VBox buildPopup(Film film, double popupW) {
        double thumbH = popupW * ASPECT;

        Pane bigThumb = new Pane();
        bigThumb.setPrefSize(popupW, thumbH);
        bigThumb.setMinSize(popupW, thumbH);
        bigThumb.setMaxSize(popupW, thumbH);
        bigThumb.setStyle(
            (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty())
                ? "-fx-background-image: url('" + film.getUrlImageCover() + "');" +
                  "-fx-background-size: cover; -fx-background-position: center;"
                : "-fx-background-color: #1a1a1a;"
        );

        // Info panel
        VBox info = new VBox(10);
        info.setStyle("-fx-background-color: #181818; -fx-padding: 14 14 16 14;");
        info.setPrefWidth(popupW);

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getChildren().addAll(
            makeBtn("▶", true),
            makeBtn("+", false),
            makeBtn("♥", false)
        );
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        actions.getChildren().addAll(sp, makeBtn("▾", false));

        // Title
        Label titleLbl = new Label(film.getTitre() != null ? film.getTitre() : "");
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        titleLbl.setMaxWidth(popupW - 28);
        titleLbl.setWrapText(false);

        // Match + meta row
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        int matchPct = 60 + (Math.abs(film.getTitre() != null ? film.getTitre().hashCode() : 0) % 35);
        Label matchLbl = new Label(matchPct + "% Match");
        matchLbl.setStyle("-fx-text-fill: #46d369; -fx-font-weight: bold; -fx-font-size: 13px;");
        String age = (film.getAgeRating() != null) ? film.getAgeRating().name() : "PG";
        Label ageLbl = new Label(age);
        ageLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;" +
            "-fx-border-color: #666; -fx-border-width: 1; -fx-padding: 1 5; -fx-border-radius: 2;");
        Label hdLbl = new Label("HD");
        hdLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;" +
            "-fx-border-color: #666; -fx-border-width: 1; -fx-padding: 1 5; -fx-border-radius: 2;");
        // FIX 6: Duration label was missing text-fill
        Label durLbl = new Label(film.getDuree() + "m");
        durLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
        meta.getChildren().addAll(matchLbl, durLbl, ageLbl, hdLbl);

        Label genreLbl = new Label("Action · Drama");
        genreLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

        info.getChildren().addAll(actions, titleLbl, meta, genreLbl);

        VBox container = new VBox(0);
        container.setPrefWidth(popupW);
        container.setMinWidth(popupW);
        container.setMaxWidth(popupW);
        container.getChildren().addAll(bigThumb, info);

        // Rounded corners on entire popup
        Rectangle popupClip = new Rectangle(popupW, thumbH + 130);
        popupClip.setArcWidth(10); popupClip.setArcHeight(10);
        container.setClip(popupClip);

        return container;
    }

    private Button makeBtn(String text, boolean white) {
        Button b = new Button(text);
        String style = white
            ? "-fx-background-color: white; -fx-text-fill: black;"
            : "-fx-background-color: transparent; -fx-text-fill: white;" +
              "-fx-border-color: #888; -fx-border-width: 2; -fx-border-radius: 50;";
        b.setStyle(style +
            "-fx-background-radius: 50;" +
            "-fx-min-width: 34; -fx-min-height: 34;" +
            "-fx-max-width: 34; -fx-max-height: 34;" +
            "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 0;"
        );
        return b;
    }

    // ══════════════════════════════════════════════════════
    //  FXML HANDLERS
    //  FIX 7: All handlers referenced in FXML are present
    // ══════════════════════════════════════════════════════

    @FXML private void onMyList()            { System.out.println("Ma Liste"); }
    @FXML private void onMovies()            { System.out.println("Films"); }
    @FXML private void onSeries()            { System.out.println("Séries"); }
    @FXML private void onSearch()            { System.out.println("Recherche : " + (searchField != null ? searchField.getText() : "")); }
    @FXML private void onSearchBtn()         { onSearch(); }
    @FXML private void onPlayFeatured()      { System.out.println("Lecture du film en vedette"); }
    @FXML private void onMoreInfoFeatured()  { System.out.println("Plus d'infos"); }
    @FXML private void onAddFeaturedToList() { System.out.println("Ajout à la liste"); }
}
*/