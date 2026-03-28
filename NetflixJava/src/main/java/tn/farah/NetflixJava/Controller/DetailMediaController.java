package tn.farah.NetflixJava.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.CardFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DetailMediaController implements Initializable {

    // ── Hero ──────────────────────────────────────────────────────
    @FXML private StackPane heroStack;
    @FXML private Pane      heroBanner;

    // ── Labels ────────────────────────────────────────────────────
    @FXML private Label lblTitre;
    @FXML private Label lblType;
    @FXML private Label lblRating;
    @FXML private Label lblAnnee;
    @FXML private Label lblDuree;
    @FXML private Label lblAgeRating;
    @FXML private Label lblSynopsis;
    @FXML private Label lblCategories;
    @FXML private Label lblCasting;
    @FXML private Label lblWarnings;
    @FXML private Label lblNote;

    // ── Boutons ───────────────────────────────────────────────────
    @FXML private Button btnPlay;
    @FXML private Button btnTrailer;
    @FXML private Button btnFavori;

    // ── Carousels similaires — peut être null si absent du FXML ──
    @FXML private VBox similarContainer;

    // ── State ─────────────────────────────────────────────────────
    private Film  currentFilm;
    private Serie currentSerie;
    private final Pane[] overlayRef = new Pane[1];

    private FilmService  filmService;
    private SerieService serieService;

    // ═══════════════════════════════════════════════════════════════
    //  INIT — null-safe partout
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        if (connection == null) return;

        filmService  = new FilmService(connection);
        serieService = new SerieService(connection);

        // ✅ FIX : null-check avant d'utiliser similarContainer
        if (similarContainer != null) {
            similarContainer.sceneProperty().addListener((obs, old, scene) -> {
                if (scene == null) return;
                Platform.runLater(() -> CardFactory.createOverlay(scene, overlayRef));
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  API PUBLIQUE
    // ═══════════════════════════════════════════════════════════════

    public void setFilm(Film film) {
        this.currentFilm  = film;
        this.currentSerie = null;
        populateFilm(film);
        loadSimilarFilms(film);
    }

    public void setSerie(Serie serie) {
        this.currentSerie = serie;
        this.currentFilm  = null;
        populateSerie(serie);
        loadSimilarSeries(serie);
    }

    // ═══════════════════════════════════════════════════════════════
    //  POPULATE FILM
    // ═══════════════════════════════════════════════════════════════

    private void populateFilm(Film film) {
        String img = film.getUrlImageBanner() != null && !film.getUrlImageBanner().isEmpty()
            ? film.getUrlImageBanner() : film.getUrlImageCover();
        setBanner(img);

        setText(lblType,    "FILM");
        setText(lblTitre,   film.getTitre() != null ? film.getTitre().toUpperCase() : "");
        setText(lblSynopsis, film.getSynopsis() != null ? film.getSynopsis() : "—");
        setText(lblCasting,  film.getCasting()  != null ? film.getCasting()  : "—");
        setText(lblDuree,    film.getDuree() + " min");
        setText(lblRating,   String.format("%.0f%% Match", film.getRatingMoyen() * 10));
        setText(lblNote,     String.format("%.1f / 10", film.getRatingMoyen()));

        if (film.getDateSortie() != null)
            setText(lblAnnee, String.valueOf(film.getDateSortie().getYear()));
        if (film.getAgeRating() != null)
            setText(lblAgeRating, film.getAgeRating().name());

        if (film.getGenres() != null && !film.getGenres().isEmpty())
            setText(lblCategories, film.getGenres().stream()
                .map(Category::getName).collect(Collectors.joining(" · ")));
        else setText(lblCategories, "—");

        if (film.getWarnings() != null && !film.getWarnings().isEmpty())
            setText(lblWarnings, film.getWarnings().stream()
                .map(w -> w.getNom()).collect(Collectors.joining(", ")));
        else setText(lblWarnings, "—");
    }

    // ═══════════════════════════════════════════════════════════════
    //  POPULATE SÉRIE
    // ═══════════════════════════════════════════════════════════════

    private void populateSerie(Serie serie) {
        String img = serie.getUrlImageBanner() != null && !serie.getUrlImageBanner().isEmpty()
            ? serie.getUrlImageBanner() : serie.getUrlImageCover();
        setBanner(img);

        setText(lblType,    "SÉRIE");
        setText(lblTitre,   serie.getTitre() != null ? serie.getTitre().toUpperCase() : "");
        setText(lblSynopsis, serie.getSynopsis() != null ? serie.getSynopsis() : "—");
        setText(lblCasting,  serie.getCasting()  != null ? serie.getCasting()  : "—");
        setText(lblDuree,    serie.isTerminee() ? "Terminée" : "En cours");
        setText(lblRating,   String.format("%.0f%% Match", serie.getRatingMoyen() * 10));
        setText(lblNote,     String.format("%.1f / 10", serie.getRatingMoyen()));

        if (serie.getDateSortie() != null)
            setText(lblAnnee, String.valueOf(serie.getDateSortie().getYear()));
        if (serie.getAgeRating() != null)
            setText(lblAgeRating, serie.getAgeRating().name());

        if (serie.getGenres() != null && !serie.getGenres().isEmpty())
            setText(lblCategories, serie.getGenres().stream()
                .map(Category::getName).collect(Collectors.joining(" · ")));
        else setText(lblCategories, "—");

        if (serie.getWarnings() != null && !serie.getWarnings().isEmpty())
            setText(lblWarnings, serie.getWarnings().stream()
                .map(w -> w.getNom()).collect(Collectors.joining(", ")));
        else setText(lblWarnings, "—");
    }

    // ═══════════════════════════════════════════════════════════════
    //  SIMILAIRES — null-safe
    // ═══════════════════════════════════════════════════════════════

    private void loadSimilarFilms(Film film) {
        if (similarContainer == null) return;
        similarContainer.getChildren().clear();
        try {
            List<Film> similar = filmService.getAllFilmsSorted().stream()
                .filter(f -> f.getId() != film.getId())
                .filter(f -> f.getGenres() != null && film.getGenres() != null &&
                    f.getGenres().stream().anyMatch(c ->
                        film.getGenres().stream().anyMatch(fc -> fc.getName().equals(c.getName()))))
                .limit(20).collect(Collectors.toList());

            if (!similar.isEmpty())
                similarContainer.getChildren().add(
                    CardFactory.buildFilmCarousel("🎬  Films similaires", similar, overlayRef,
                        f -> {
                            DetailMediaController ctrl = ScreenManager.getInstance()
                                .navigateAndGetController(Screen.detail);
                            if (ctrl != null) ctrl.setFilm(f);
                        }));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadSimilarSeries(Serie serie) {
        if (similarContainer == null) return;
        similarContainer.getChildren().clear();
        try {
            List<Serie> similar = serieService.getAllFilmsSorted().stream()
                .filter(s -> s.getId() != serie.getId())
                .filter(s -> s.getGenres() != null && serie.getGenres() != null &&
                    s.getGenres().stream().anyMatch(c ->
                        serie.getGenres().stream().anyMatch(sc -> sc.getName().equals(c.getName()))))
                .limit(20).collect(Collectors.toList());

            if (!similar.isEmpty())
                similarContainer.getChildren().add(
                    CardFactory.buildSerieCarousel("📺  Séries similaires", similar, overlayRef,
                        s -> {
                            DetailMediaController ctrl = ScreenManager.getInstance()
                                .navigateAndGetController(Screen.detail);
                            if (ctrl != null) ctrl.setSerie(s);
                        }));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    /** Null-safe setText */
    private void setText(Label label, String value) {
        if (label != null) label.setText(value);
    }

    private void setBanner(String imageUrl) {
        if (heroBanner == null) return;
        heroBanner.setStyle(
            (imageUrl != null && !imageUrl.isEmpty())
                ? "-fx-background-image: url('" + imageUrl + "');" +
                  "-fx-background-size: cover; -fx-background-position: center top;"
                : "-fx-background-color: #1a1a1a;"
        );
    }

    // ═══════════════════════════════════════════════════════════════
    //  HANDLERS FXML
    // ═══════════════════════════════════════════════════════════════

    @FXML private void onPlay() {
        String url = currentFilm  != null ? currentFilm .getUrlTeaser()
                   : currentSerie != null ? currentSerie.getUrlTeaser() : null;
        System.out.println(url != null ? "Lecture : " + url : "Aucun lien.");
    }

    @FXML private void onTrailer() { System.out.println("Bande-annonce"); }

    @FXML private void onFavori() {
        String titre = currentFilm  != null ? currentFilm .getTitre()
                     : currentSerie != null ? currentSerie.getTitre() : "";
        System.out.println("Ajouté à Ma liste : " + titre);
        if (btnFavori != null) {
            btnFavori.setText("✓  Dans ma liste");
            btnFavori.setStyle("-fx-background-color: #ffffff22;");
        }
    }

    @FXML private void onBack()   { ScreenManager.getInstance().goBack(); }
    @FXML private void onHome()   { ScreenManager.getInstance().navigateTo(Screen.home); }
    @FXML private void onMovies() { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void onSeries() { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML private void onMyList() { }
}