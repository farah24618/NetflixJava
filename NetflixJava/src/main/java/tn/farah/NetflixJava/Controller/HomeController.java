package tn.farah.NetflixJava.Controller;

import java.awt.Button;
import java.awt.Color;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.plaf.synth.Region;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
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
/*package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;

import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.utils.ConxDB;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private StackPane heroSection;
    @FXML private Pane heroBackdrop;
    @FXML private Label heroTitle, heroGenreBadge, heroType, heroRating, heroDuration, heroYear, heroCertif, heroSynopsis;
    @FXML private TextField searchField;
    @FXML private Label avatarLabel;
    @FXML private VBox carouselContainer;

    private FilmService filmService;
    private Connection connection;

    private List<Film> listeFilmsHero = new ArrayList<>();
    private int indexCourant = 0;
    private ScaleTransition zoomActuel;
    private Timeline heroTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("--- Démarrage du HomeController ---");

        // 1. Connexion
        connection = ConxDB.getInstance();
        if (connection == null) {
            System.err.println("ERREUR : La connexion à la base de données a échoué !");
            return;
        }

        // 2. Initialisation du service unique
        filmService = new FilmService(connection);

        // 3. Lancement des composants
        configurerHero();
        chargerCarousels();

        if(avatarLabel != null) avatarLabel.setText("U");
    }

    private void configurerHero() {
        try {
            List<Film> films = filmService.getAllFilmsSorted();
            System.out.println("Nombre de films récupérés pour le Hero : " + films.size());

            if (films.isEmpty()) {
                System.out.println("Aucun film trouvé dans la base de données.");
                return;
            }

            // On prend les 5 premiers pour le diaporama
            listeFilmsHero.clear();
            int limite = Math.min(5, films.size());
            for (int i = 0; i < limite; i++) {
                listeFilmsHero.add(films.get(i));
            }

            chargerElementHero(0);

            // Timer de défilement (8 secondes)
            if (heroTimer != null) heroTimer.stop();
            heroTimer = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
                indexCourant = (indexCourant + 1) % listeFilmsHero.size();
                chargerElementHero(indexCourant);
            }));
            heroTimer.setCycleCount(Timeline.INDEFINITE);
            heroTimer.play();

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans configurerHero : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerElementHero(int index) {
        Film film = listeFilmsHero.get(index);
        System.out.println("Affichage du film Hero : " + film.getTitre());

        // Image de fond
        if (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty()) {
            heroBackdrop.setStyle("-fx-background-image: url('" + film.getUrlImageCover() + "');" +
                                "-fx-background-size: cover; -fx-background-position: center;");
        }

        // Animation Zoom
        if (zoomActuel != null) zoomActuel.stop();
        zoomActuel = new ScaleTransition(Duration.seconds(8), heroBackdrop);
        zoomActuel.setFromX(1.0); zoomActuel.setToX(1.08);
        zoomActuel.setFromY(1.0); zoomActuel.setToY(1.08);
        zoomActuel.setAutoReverse(true);
        zoomActuel.setCycleCount(Timeline.INDEFINITE);
        zoomActuel.play();

        // Remplissage des labels
        heroTitle.setText(film.getTitre());
        heroSynopsis.setText(film.getSynopsis());
        heroRating.setText(String.format("%.1f ★", film.getRatingMoyen()));
        heroDuration.setText(film.getDuree() + " min");
        heroType.setText("FILM");

        if (film.getDateSortie() != null) {
            heroYear.setText(String.valueOf(film.getDateSortie().getYear()));
        }

        if (film.getAgeRating() != null) {
            heroCertif.setText(film.getAgeRating().name());
        }

        // Petit fondu à chaque changement de film
        FadeTransition ft = new FadeTransition(Duration.millis(500), heroSection);
        ft.setFromValue(0.6); ft.setToValue(1.0);
        ft.play();
    }

    private void chargerCarousels() {
        try {
            List<Film> tousLesFilms = filmService.getAllFilmsSorted();
            if (!tousLesFilms.isEmpty()) {
                carouselContainer.getChildren().add(creerCarousel("Top Films ", tousLesFilms));
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans chargerCarousels : " + e.getMessage());
        }
    }

    private VBox creerCarousel(String titre, List<Film> films) {
        VBox bloc = new VBox(10);
        Label labelTitre = new Label(titre);
        labelTitre.getStyleClass().add("carousel-title");

        ScrollPane scroll = new ScrollPane();
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("carousel-scroll");

        HBox rangee = new HBox(15);
        rangee.getStyleClass().add("carousel-row");

        for (Film f : films) {
            rangee.getChildren().add(creerCarteFilm(f));
        }

        scroll.setContent(rangee);
        bloc.getChildren().addAll(labelTitre, scroll);
        return bloc;
    }

    private StackPane creerCarteFilm(Film film) {
        StackPane carte = new StackPane();
        carte.getStyleClass().add("film-card");
        carte.setPrefSize(160, 240);

        // Image de la carte
        Pane image = new Pane();
        if (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty()) {
            image.setStyle("-fx-background-image: url('" + film.getUrlImageCover() + "');" +
                          "-fx-background-size: cover;");
        } else {
            image.setStyle("-fx-background-color: #333;");
        }

        // Overlay avec titre au survol
        VBox overlay = new VBox(5);
        overlay.getStyleClass().add("card-overlay");
        overlay.setOpacity(0);
        Label t = new Label(film.getTitre());
        t.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        overlay.getChildren().add(t);

        carte.getChildren().addAll(image, overlay);

        // Events
        carte.setOnMouseEntered(e -> { overlay.setOpacity(1); carte.setScaleX(1.05); carte.setScaleY(1.05); });
        carte.setOnMouseExited(e -> { overlay.setOpacity(0); carte.setScaleX(1.0); carte.setScaleY(1.0); });

        return carte;
    }

    private void afficherNotification(String msg) {
        System.out.println("NOTIF : " + msg);
    }
 // ════════════════════════════════════════════════════════
    // MÉTHODES APPELÉES PAR LE FXML (Obligatoires pour éviter le crash)
    // ════════════════════════════════════════════════════════

    @FXML
    private void onMyList() {
        System.out.println("Clic sur Ma Liste");
    }

    @FXML
    private void onMovies() {
        System.out.println("Clic sur Films");
    }

    @FXML
    private void onSeries() {
        System.out.println("Clic sur Séries");
    }

    @FXML
    private void onSearch() {
        System.out.println("Recherche lancée pour : " + searchField.getText());
    }

    @FXML
    private void onSearchBtn() {
        onSearch();
    }

    @FXML
    private void onPlayFeatured() {
        System.out.println("Lecture du film en vedette");
    }

    @FXML
    private void onMoreInfoFeatured() {
        System.out.println("Plus d'infos sur le film en vedette");
    }

    @FXML
    private void onAddFeaturedToList() {
        System.out.println("Ajout à la liste");
    }
}*/