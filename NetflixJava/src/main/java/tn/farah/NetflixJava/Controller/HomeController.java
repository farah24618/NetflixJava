package tn.farah.NetflixJava.Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;
import tn.farah.NetflixJava.utils.CardFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HomeController implements Initializable {

    @FXML private StackPane heroSection;
    @FXML private Pane      heroBackdrop;
    @FXML private Label     heroTitle, heroGenreBadge, heroType,
                             heroRating, heroDuration, heroYear, heroCertif,
                             heroSynopsis, heroWarning, heroCategories;
    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;

    private FilmService  filmService;
    private SerieService serieService;

    // ✅ FIX : tableau à 1 élément pour capturer l'overlay par référence dans les lambdas
    private final Pane[] overlayRef = new Pane[1];

    private final List<Film> heroFilms = new ArrayList<>();
    private int               heroIndex = 0;
    private ScaleTransition   heroZoom;
    private Timeline          heroTimer;

    // ═══════════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        if (connection == null) return;

        filmService  = new FilmService(connection);
        serieService = new SerieService(connection);

        initHero();
        loadAllCarousels();
        SessionManager.getInstance().getCurrentUserId();
        if (avatarLabel != null) avatarLabel.setText("U");

        // ✅ FIX : createOverlay remplit overlayRef[0] après que la scène soit prête
        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> CardFactory.createOverlay(scene, overlayRef));
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAVIGATION HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Consumer<Film> goToFilmDetail() {
        return film -> {
            DetailMediaController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detail);
            if (ctrl != null) ctrl.setFilm(film);
        };
    }

    private Consumer<Serie> goToSerieDetail() {
        return serie -> {
            EpisodeViewController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detail);
            if (ctrl != null) ctrl.setSerie(serie);
        };
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

        if (film.getGenres() != null)
            heroCategories.setText(film.getGenres().stream()
                .map(c -> c.getName().toUpperCase())
                .collect(Collectors.joining("   ")));

        if (film.getWarnings() != null)
            heroWarning.setText(film.getWarnings().stream()
                .map(w -> w.getNom().toUpperCase())
                .collect(Collectors.joining(" ")));

        if (heroGenreBadge != null) heroGenreBadge.setText("");
        if (film.getDateSortie() != null) heroYear  .setText(String.valueOf(film.getDateSortie().getYear()));
        if (film.getAgeRating()  != null) heroCertif.setText(film.getAgeRating().name());

        FadeTransition fade = new FadeTransition(Duration.millis(600), heroSection);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.play();
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSELS — passent overlayRef (pas overlayRef[0])
    // ═══════════════════════════════════════════════════════════════

    private void loadAllCarousels() {
        try {
            List<Film>  allFilms  = filmService.getAllFilmsSorted();
            List<Serie> allSeries = serieService.getAllFilmsSorted();

            if (!allFilms.isEmpty()) {
                carouselContainer.getChildren().add(CardFactory.buildSectionHeader("🎬  Films"));
                carouselContainer.getChildren().add(
                    CardFactory.buildResponsiveFilmCarousel("Top Films", allFilms,
                        overlayRef, goToFilmDetail()));
            }

            if (!allSeries.isEmpty()) {
                carouselContainer.getChildren().add(CardFactory.buildSectionHeader("📺  Séries"));
                carouselContainer.getChildren().add(
                    CardFactory.buildResponsiveSerieCarousel("Top Séries", allSeries,
                        overlayRef, goToSerieDetail()));
            }

            if (!allFilms.isEmpty()) {
                Map<String, List<Film>> byCategory = filmService.getAllFilmsByCategory();
                new TreeMap<>(byCategory).forEach((cat, films) -> {
                    if (!films.isEmpty())
                        carouselContainer.getChildren().add(
                            CardFactory.buildResponsiveFilmCarousel("🎬 " + cat, films,
                                overlayRef, goToFilmDetail()));
                });
            }

            if (!allSeries.isEmpty()) {
                Map<String, List<Serie>> byGenre = serieService.getAllFilmsByCategory();
                new TreeMap<>(byGenre).forEach((genre, series) -> {
                    if (!series.isEmpty())
                        carouselContainer.getChildren().add(
                            CardFactory.buildResponsiveSerieCarousel("📺 " + genre, series,
                                overlayRef, goToSerieDetail()));
                });
            }

        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HANDLERS FXML
    // ═══════════════════════════════════════════════════════════════

    @FXML private void onMyList()            {  ScreenManager.getInstance().navigateTo(Screen.myList); }
    @FXML private void onMovies()            { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void onSeries()            { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML private void onPlayFeatured()      { System.out.println("Lecture hero"); }
    @FXML private void onMoreInfoFeatured()  {
        if (!heroFilms.isEmpty()) goToFilmDetail().accept(heroFilms.get(heroIndex));
    }
    @FXML private void onAddFeaturedToList() { System.out.println("Ajout à la liste hero"); }
    @FXML private void onSearchBtn()         { onSearch(); }

    @FXML
    private void onSearch() {
        String query = searchField != null ? searchField.getText().trim() : "";
        if (query.isEmpty()) return;
        System.out.println("Recherche : " + query);
    }
}