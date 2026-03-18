package tn.farah.NetflixJava.Controller;

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
                carouselContainer.getChildren().add(creerCarousel("🎬 Tous les Films", tousLesFilms));
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
}