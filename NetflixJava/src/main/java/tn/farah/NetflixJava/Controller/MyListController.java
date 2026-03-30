package tn.farah.NetflixJava.Controller;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.History;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.HistoryService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.CardFactory;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

public class MyListController implements Initializable {

    // ── fx:id FXML ──────────────────────────────────────────────────────────
    @FXML private VBox             pageContainer;   // conteneur principal scrollable
    @FXML private VBox             emptyState;
    @FXML private Label            totalLabel;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterGenre;
    @FXML private ComboBox<String> sortCombo;
    @FXML private TextField        searchField;

    // ── Services ────────────────────────────────────────────────────────────
    private FavoriService  favoriService;
    private FilmService    filmService;
    private SerieService   serieService;
    private HistoryService historyService;

    // ── Données ─────────────────────────────────────────────────────────────
    private List<Favori>  allFavoris;
    private List<History> allHistory;
    private int           userId;

    // ── Overlay pour popups hover ────────────────────────────────────────────
    private final Pane[] overlayRef = new Pane[1];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection conn = ConxDB.getInstance();
        favoriService  = new FavoriService(conn);
        filmService    = new FilmService(conn);
        serieService   = new SerieService(conn);
        historyService = new HistoryService(conn);

        userId = SessionManager.getInstance().getCurrentUserId();

        initCombos();

        // Overlay après que la scène soit prête
        pageContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            javafx.application.Platform.runLater(() ->
                CardFactory.createOverlay(scene, overlayRef));
        });

        loadPage();
    }

    // ── Init ComboBox ───────────────────────────────────────────────────────

    private void initCombos() {
        filterType.getItems().addAll("Tous", "Film", "Série");
        filterType.setValue("Tous");
        filterGenre.getItems().addAll("Tous", "Action", "Comédie", "Drame",
                                      "Horreur", "Science-fiction", "Romance");
        filterGenre.setValue("Tous");
        sortCombo.getItems().addAll("Ajout récent", "Titre A→Z", "Titre Z→A");
        sortCombo.setValue("Ajout récent");
    }

    // ── Chargement principal ────────────────────────────────────────────────

    private void loadPage() {
        pageContainer.getChildren().clear();

        if (userId == -1) {
            showEmpty("Connectez-vous pour voir votre liste.");
            return;
        }

        allHistory = historyService.findByUser(userId);
        allFavoris = favoriService.getFavorisByUser(userId);

        boolean hasHistory  = allHistory  != null && !allHistory.isEmpty();
        boolean hasFavoris  = allFavoris  != null && !allFavoris.isEmpty();

        if (!hasHistory && !hasFavoris) {
            showEmpty("Votre liste est vide.");
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);

        int total = (hasFavoris ? allFavoris.size() : 0);
        totalLabel.setText(total + " titre(s) dans Ma Liste");

        // ── Section 1 : Continuer à regarder ─────────────────────────────
        if (hasHistory) {
            pageContainer.getChildren().add(
                buildSectionHeader("▶  Continuer à regarder")
            );

            // Construire la liste des films/séries depuis l'historique
            List<Film> historyFilms = allHistory.stream()
            	    .filter(h -> h.isFilm())  // filmId != null
            	    .map(h -> filmService.findById(h.getFilmId()))
            	    .filter(f -> f != null)
            	    .collect(Collectors.toList());

            	List<Serie> historySeries = allHistory.stream()
            	    .filter(h -> h.isEpisode())  // episodeId != null
            	    .map(h -> serieService.findByEpisodeId(h.getEpisodeId()))
            	    .filter(s -> s != null)
            	    .collect(Collectors.toList());

            if (!historyFilms.isEmpty()) {
                pageContainer.getChildren().add(
                    CardFactory.buildResponsiveFilmCarousel(
                        "", historyFilms, overlayRef,
                        film -> ouvrirDetail(film.getId())
                    )
                );
            }
            if (!historySeries.isEmpty()) {
                pageContainer.getChildren().add(
                    CardFactory.buildResponsiveSerieCarousel(
                        "", historySeries, overlayRef,
                        serie -> ouvrirDetail(serie.getId())
                    )
                );
            }
        }

        // ── Section 2 : Ma Liste (Favoris) ───────────────────────────────
        if (hasFavoris) {
            pageContainer.getChildren().add(
                buildSectionHeader("☰  Ma Liste")
            );
            renderFavorisGrid(allFavoris);
        }
    }

    // ── Grille des favoris ──────────────────────────────────────────────────

    private void renderFavorisGrid(List<Favori> favoris) {
        List<Film>  favFilms  = favoris.stream()
            .map(f -> filmService.findById(f.getMediaId()))
            .filter(f -> f != null)
            .collect(Collectors.toList());

        List<Serie> favSeries = favoris.stream()
            .filter(f -> filmService.findById(f.getMediaId()) == null)
            .map(f -> serieService.findById(f.getMediaId()))
            .filter(s -> s != null)
            .collect(Collectors.toList());

        if (!favFilms.isEmpty()) {
            pageContainer.getChildren().add(
                CardFactory.buildResponsiveFilmCarousel(
                    "Films sauvegardés", favFilms, overlayRef,
                    film -> ouvrirDetail(film.getId())
                )
            );
        }
        if (!favSeries.isEmpty()) {
            pageContainer.getChildren().add(
                CardFactory.buildResponsiveSerieCarousel(
                    "Séries sauvegardées", favSeries, overlayRef,
                    serie -> ouvrirDetail(serie.getId())
                )
            );
        }
    }

    // ── Section header ──────────────────────────────────────────────────────

    private HBox buildSectionHeader(String text) {
        Label label = new Label(text);
        label.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-padding: 24 0 8 0;
        """);
        HBox header = new HBox(label);
        header.setStyle("-fx-padding: 0 40;");
        return header;
    }

    // ── Filtres ─────────────────────────────────────────────────────────────

    @FXML
    private void applyFilters() {
        if (allFavoris == null) return;

        String type = filterType.getValue();
        String sort = sortCombo.getValue();

        List<Favori> filtered = allFavoris.stream()
            .filter(f -> {
                if (type == null || type.equals("Tous")) return true;
                Film film = filmService.findById(f.getMediaId());
                if (type.equals("Film"))  return film != null;
                if (type.equals("Série")) return film == null;
                return true;
            })
            .collect(Collectors.toList());

        if ("Titre A→Z".equals(sort)) {
            filtered.sort((a, b) -> getTitre(a).compareToIgnoreCase(getTitre(b)));
        } else if ("Titre Z→A".equals(sort)) {
            filtered.sort((a, b) -> getTitre(b).compareToIgnoreCase(getTitre(a)));
        }

        // Re-render uniquement la section favoris
        pageContainer.getChildren().clear();
        if (allHistory != null && !allHistory.isEmpty()) {
            pageContainer.getChildren().add(buildSectionHeader("▶  Continuer à regarder"));
        }
        pageContainer.getChildren().add(buildSectionHeader("☰  Ma Liste"));
        renderFavorisGrid(filtered);
    }

    @FXML
    private void resetFilters() {
        filterType.setValue("Tous");
        filterGenre.setValue("Tous");
        sortCombo.setValue("Ajout récent");
        loadPage();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { loadPage(); return; }

        if (allFavoris == null) return;
        List<Favori> filtered = allFavoris.stream()
            .filter(f -> getTitre(f).toLowerCase().contains(query))
            .collect(Collectors.toList());

        pageContainer.getChildren().clear();
        pageContainer.getChildren().add(buildSectionHeader("Résultats pour \"" + query + "\""));
        renderFavorisGrid(filtered);
    }

    @FXML
    private void goHome() {
        ScreenManager.getInstance().navigateTo(Screen.home);
    }

    // ── Navigation ──────────────────────────────────────────────────────────

    private void ouvrirDetail(int mediaId) {
        Film film = filmService.findById(mediaId);
        if (film != null) {
            DetailMediaController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detail);
            if (ctrl != null) ctrl.setFilm(film);
            return;
        }
        Serie serie = serieService.findById(mediaId);
        if (serie != null) {
            DetailMediaController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detail);
            if (ctrl != null) ctrl.setSerie(serie);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void showEmpty(String message) {
        emptyState.setVisible(true);
        emptyState.setManaged(true);
        totalLabel.setText("0 titre(s)");
        Label msg = (Label) emptyState.lookup(".empty-message");
        if (msg != null) msg.setText(message);
    }

    private String getTitre(Favori f) {
        Film film = filmService.findById(f.getMediaId());
        if (film != null) return film.getTitre();
        Serie serie = serieService.findById(f.getMediaId());
        return serie != null ? serie.getTitre() : "";
    }
}