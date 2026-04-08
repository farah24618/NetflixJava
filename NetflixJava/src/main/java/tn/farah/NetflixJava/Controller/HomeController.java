package tn.farah.NetflixJava.Controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.Service.HistoryService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.SaisonService;
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

    // ── FXML ────────────────────────────────────────────────────────────────
    @FXML private StackPane heroSection;
    @FXML private Pane      heroBackdrop;
    @FXML private Label     heroTitle, heroGenreBadge, heroType,
                             heroRating, heroDuration, heroYear, heroCertif,
                             heroSynopsis, heroWarning, heroCategories;
    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;
    @FXML private Label notifBadge;

    // ── Services ─────────────────────────────────────────────────────────────
    private FilmService    filmService;
    private SerieService   serieService;
    private FavoriService  FavoriService;
    private SaisonService saisonService;
    private Connection connection;

    // ── Data caches (loaded once, filtered on demand) ─────────────────────
    private List<Film>              allFilms  = Collections.emptyList();
    private List<Serie>             allSeries = Collections.emptyList();
    private Map<String, List<Film>> filmsByCategory  = Collections.emptyMap();
    private Map<String, List<Serie>> seriesByCategory = Collections.emptyMap();

    // ── Hero state ───────────────────────────────────────────────────────────
    private final List<Film> heroFilms  = new ArrayList<>();
    private final Pane[]     overlayRef = new Pane[1];   // lambda-safe ref
    private int              heroIndex  = 0;
    private ScaleTransition  heroZoom;
    private Timeline         heroTimer;

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
         connection = ConxDB.getInstance();
        if (connection == null) return;

        filmService   = new FilmService(connection);
        serieService  = new SerieService(connection);
        FavoriService = new FavoriService(connection);
        saisonService=new SaisonService(connection);
        CardFactory.setFavoriService(FavoriService);
        CardFactory.setNotificationService(new NotificationService(connection));
        loadData();
        initHero();
        chargerBadgeNotif();
        buildCarousels(allFilms, allSeries, filmsByCategory, seriesByCategory);
        initSearch();

        if (avatarLabel != null) avatarLabel.setText("U");

        // Overlay must wait for the scene to be ready
        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> CardFactory.createOverlay(scene, overlayRef));
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATA LOADING  (called once at startup)
    // ════════════════════════════════════════════════════════════════════════

    private void loadData() {
        try {
            allFilms         = filmService.getAllFilmsSorted();
            allSeries        = serieService.getAllSeries();
            filmsByCategory  = filmService.getAllFilmsByCategory();
            seriesByCategory = serieService.getAllSeriesByCategory();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SEARCH  — live filter, rebuilds carousels every keystroke
    // ════════════════════════════════════════════════════════════════════════

    private void initSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            final String query = newVal == null ? "" : newVal.trim().toLowerCase();
            applySearch(query);
        });
    }

    private void applySearch(final String query) {
        if (query.isEmpty()) {
            // Restore full data
            buildCarousels(allFilms, allSeries, filmsByCategory, seriesByCategory);
            return;
        }

        // Filter flat lists
        final List<Film> filteredFilms = allFilms.stream()
            .filter(f -> matchesFilm(f, query))
            .collect(Collectors.toList());

        final List<Serie> filteredSeries = allSeries.stream()
            .filter(s -> matchesSerie(s, query))
            .collect(Collectors.toList());

        // Filter category maps
        final Map<String, List<Film>> filteredFilmCats = new TreeMap<>();
        filmsByCategory.forEach((cat, films) -> {
            final List<Film> sub = films.stream()
                .filter(f -> matchesFilm(f, query))
                .collect(Collectors.toList());
            if (!sub.isEmpty()) filteredFilmCats.put(cat, sub);
        });

        final Map<String, List<Serie>> filteredSerieCats = new TreeMap<>();
        seriesByCategory.forEach((genre, series) -> {
            final List<Serie> sub = series.stream()
                .filter(s -> matchesSerie(s, query))
                .collect(Collectors.toList());
            if (!sub.isEmpty()) filteredSerieCats.put(genre, sub);
        });

        buildCarousels(filteredFilms, filteredSeries, filteredFilmCats, filteredSerieCats);
    }

    /** Match a Film against the search query (title, synopsis, genre names). */
    private boolean matchesFilm(final Film f, final String q) {
        if (f.getTitre()    != null && f.getTitre().toLowerCase().contains(q))    return true;
        if (f.getSynopsis() != null && f.getSynopsis().toLowerCase().contains(q)) return true;
        if (f.getGenres()   != null) {
            return f.getGenres().stream()
                .anyMatch(g -> g.getName().toLowerCase().contains(q));
        }
        return false;
    }

    /** Match a Serie against the search query (titre, synopsis). */
    private boolean matchesSerie(final Serie s, final String q) {
        if (s.getTitre()    != null && s.getTitre().toLowerCase().contains(q))    return true;
        if (s.getSynopsis() != null && s.getSynopsis().toLowerCase().contains(q)) return true;
        return false;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CAROUSEL BUILDER  — clears and rebuilds the VBox
    // ════════════════════════════════════════════════════════════════════════

    private void buildCarousels(
            final List<Film>              films,
            final List<Serie>             series,
            final Map<String, List<Film>> filmCats,
            final Map<String, List<Serie>> serieCats) {

        carouselContainer.getChildren().clear();

        if (!films.isEmpty()) {
            carouselContainer.getChildren().add(CardFactory.buildSectionHeader("🎬  Films"));
            carouselContainer.getChildren().add(
                CardFactory.buildResponsiveFilmCarousel("Top Films", films,
                    overlayRef, goToFilmDetail()));
        }

        if (!series.isEmpty()) {
            carouselContainer.getChildren().add(CardFactory.buildSectionHeader("📺  Séries"));
            carouselContainer.getChildren().add(
                CardFactory.buildResponsiveSerieCarousel("Top Séries", series,
                    overlayRef, goToSerieDetail()));
        }

        filmCats.forEach((cat, catFilms) -> {
            if (!catFilms.isEmpty())
                carouselContainer.getChildren().add(
                    CardFactory.buildResponsiveFilmCarousel("🎬 " + cat, catFilms,
                        overlayRef, goToFilmDetail()));
        });

        serieCats.forEach((genre, genreSeries) -> {
            if (!genreSeries.isEmpty())
                carouselContainer.getChildren().add(
                    CardFactory.buildResponsiveSerieCarousel("📺 " + genre, genreSeries,
                        overlayRef, goToSerieDetail()));
        });

        // Show "no results" message when everything is empty after a search
        if (films.isEmpty() && series.isEmpty() && filmCats.isEmpty() && serieCats.isEmpty()) {
            final Label empty = new Label("No results found.");
            empty.setStyle("-fx-text-fill: #aaa; -fx-font-size: 16px; -fx-padding: 40;");
            carouselContainer.getChildren().add(empty);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ADD TO LIST  — saves current hero film to DB
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onAddFeaturedToList() {
        if (heroFilms.isEmpty()) return;

        final int    userId = SessionManager.getInstance().getCurrentUserId();
        final Film   film   = heroFilms.get(heroIndex);
        Favori favori =new Favori(userId, film.getId());
        final boolean added = FavoriService.ajouterFavori(favori);
		showToast(added ? "✔ Added to My List" : "Already in your list");
    }

    /**
     * Lightweight toast: a Label that fades in/out at the bottom of the hero.
     * No extra FXML node required — created programmatically each time.
     */
    private void showToast(final String message) {
        final Label toast = new Label(message);
        toast.setStyle(
            "-fx-background-color: rgba(0,0,0,0.75);" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-background-radius: 20;" +
            "-fx-font-size: 13px;"
        );
        toast.setOpacity(0);
        toast.setMouseTransparent(true);

        heroSection.getChildren().add(toast);
        StackPane.setAlignment(toast, javafx.geometry.Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new javafx.geometry.Insets(0, 0, 30, 0));

        final FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setToValue(1);

        final FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(2));
        fadeOut.setOnFinished(e -> heroSection.getChildren().remove(toast));

        new SequentialTransition(fadeIn, fadeOut).play();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HERO
    // ════════════════════════════════════════════════════════════════════════

    private void initHero() {
        if (allFilms.isEmpty()) return;
        allFilms.stream().limit(5).forEach(heroFilms::add);
        showHeroFilm(0);

        heroTimer = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
            heroIndex = (heroIndex + 1) % heroFilms.size();
            showHeroFilm(heroIndex);
        }));
        heroTimer.setCycleCount(Timeline.INDEFINITE);
        heroTimer.play();
    }

    private void showHeroFilm(final int index) {
        final Film film = heroFilms.get(index);

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

        heroTitle   .setText(film.getTitre()    != null ? film.getTitre().toUpperCase() : "");
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

        final FadeTransition fade = new FadeTransition(Duration.millis(600), heroSection);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.play();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private Consumer<Film> goToFilmDetail() {
        return film -> {
            final FilmViewController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detailFilm);
            if (ctrl != null) ctrl.setFilm(film);
        };
    }

    private Consumer<Serie> goToSerieDetail() {
        return serie -> {
            // 1. On récupère l'ID de la première saison (via un nouveau DAO ou méthode)
            int firstSeasonId = saisonService.findFirstSeasonIdBySerie(serie.getId());

            // 2. Navigation
            final EpisodeViewController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detail);

            if (ctrl != null) {
                ctrl.setSerie(serie);
                System.out.println("saison:"+firstSeasonId);
                ctrl.setSaisonId(firstSeasonId); // Nouvelle méthode dans votre EpisodeViewController
            }
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FXML HANDLERS
    // ════════════════════════════════════════════════════════════════════════

    @FXML private void onMyList()           { ScreenManager.getInstance().navigateTo(Screen.myList); }
    @FXML private void onMovies()           { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void onSeries()           { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML 
    private void onPlayFeatured() {
        if (heroFilms.isEmpty()) {
        	System.out.println("hero is empty");
        	return;
        }
        
        // 1. Stopper le timer AVANT de naviguer
        if (heroTimer != null) heroTimer.stop();
        
        // 2. Capturer le film IMMÉDIATEMENT
        final Film film = heroFilms.get(heroIndex);
        
        int userId = SessionManager.getInstance().getCurrentUserId();        // 3. Naviguer
        FilmPlayerController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.filmPlayer); 
            
        if (ctrl != null) {
            ctrl.initFilm(film,userId);
        } else {
            System.err.println("FilmPlayerController null — vérifie Screen.filmPlayer");
        }
    }   

    @FXML 
    private void onMoreInfoFeatured() {
        if (heroFilms.isEmpty()) return;
        
        // 1. Stopper le timer AVANT de naviguer
        if (heroTimer != null) heroTimer.stop();
        
        // 2. Capturer le film IMMÉDIATEMENT
        final Film film = heroFilms.get(heroIndex);
        
        // 3. Naviguer
        FilmViewController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.detailFilm);
            
        if (ctrl != null) {
            ctrl.setFilm(film);
        } else {
            System.err.println("FilmViewController null — vérifie Screen.detailFilm");
        }
    }
    @FXML
    private void onSearch() {
        // called by onKeyReleased on the TextField
        applySearch(searchField.getText() == null ? ""
                : searchField.getText().trim().toLowerCase());
    }

    @FXML
    private void onSearchBtn() {
        // called by the 🔍 button
        applySearch(searchField.getText() == null ? ""
                : searchField.getText().trim().toLowerCase());
    }
    private void chargerBadgeNotif() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        NotificationService service = new NotificationService(connection);
        int count = service.countUnread(userId);
        if (notifBadge != null) {
            notifBadge.setText(String.valueOf(count));
            notifBadge.setVisible(count > 0);
        }
    }

    @FXML private void onNotifications() {
        ScreenManager.getInstance().navigateTo(Screen.notification); // adapte le nom exact
    }
    @FXML
    private void onEditProfile() {
        ScreenManager.getInstance().navigateTo(Screen.editProfiles);
    }

}