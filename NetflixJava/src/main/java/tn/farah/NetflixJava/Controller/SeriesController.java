package tn.farah.NetflixJava.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.CardFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SeriesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;
    @FXML private FlowPane  filterBar;

    private SerieService serieService;
    private FavoriService favoriService;

    // ✅ FIX : tableau à 1 élément — capturé par référence dans les lambdas
    private final Pane[] overlayRef = new Pane[1];

    private List<Serie>              allSeries     = new ArrayList<>();
    private Map<String, List<Serie>> seriesByGenre = new LinkedHashMap<>();
    private final Set<String>        activeFilters = new LinkedHashSet<>();

    // ═══════════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        if (connection == null) return;

        serieService = new SerieService(connection);
        favoriService=new FavoriService(connection);
        CardFactory.setNotificationService(new NotificationService(connection));
        CardFactory.setFavoriService(favoriService);

        allSeries     = serieService.getAllSeries();
		seriesByGenre = serieService.getAllSeriesByCategory();

        buildFilterBar();
        renderSections(allSeries);

        if (avatarLabel != null) avatarLabel.setText("U");

        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> CardFactory.createOverlay(scene, overlayRef));
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAVIGATION HELPER
    // ═══════════════════════════════════════════════════════════════

    private Consumer<Serie> goToDetail() {
        return serie -> {
           EpisodeViewController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detail);
            if (ctrl != null) ctrl.setSerie(serie);
        };
    }

    // ═══════════════════════════════════════════════════════════════
    //  FILTRES
    // ═══════════════════════════════════════════════════════════════

    private void buildFilterBar() {
        ToggleButton allBtn = new ToggleButton("Tous");
        allBtn.getStyleClass().add("filter-chip");
        allBtn.setSelected(true);
        allBtn.setOnAction(e -> {
            activeFilters.clear();
            filterBar.getChildren().stream()
                .filter(n -> n instanceof ToggleButton && n != allBtn)
                .forEach(n -> ((ToggleButton) n).setSelected(false));
            allBtn.setSelected(true);
            renderSections(allSeries);
        });
        filterBar.getChildren().add(allBtn);

        seriesByGenre.keySet().stream().sorted().forEach(genre -> {
            ToggleButton btn = new ToggleButton(genre);
            btn.getStyleClass().add("filter-chip");
            btn.setOnAction(e -> {
                allBtn.setSelected(false);
                if (btn.isSelected()) activeFilters.add(genre);
                else                  activeFilters.remove(genre);

                if (activeFilters.isEmpty()) {
                    allBtn.setSelected(true);
                    renderSections(allSeries);
                } else {
                    List<Serie> filtered = allSeries.stream()
                        .filter(s -> s.getGenres() != null &&
                            activeFilters.stream().allMatch(filter ->
                                s.getGenres().stream().anyMatch(c -> c.getName().equals(filter))))
                        .collect(Collectors.toList());
                    renderSections(filtered);
                }
            });
            filterBar.getChildren().add(btn);
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  3 SECTIONS
    // ═══════════════════════════════════════════════════════════════

    private void renderSections(List<Serie> source) {
        carouselContainer.getChildren().clear();

        if (source.isEmpty()) {
            Label empty = new Label("Aucune série trouvée.");
            empty.getStyleClass().add("empty-label");
            carouselContainer.getChildren().add(empty);
            return;
        }

        // 1. Récemment ajoutées
        List<Serie> recent = source.stream()
            .filter(s -> s.getDateSortie() != null)
            .sorted(Comparator.comparing(Serie::getDateSortie).reversed())
            .limit(20).collect(Collectors.toList());
        if (!recent.isEmpty())
            carouselContainer.getChildren().add(
                CardFactory.buildSerieCarousel("🆕  Récemment ajoutées", recent,
                    overlayRef, goToSerieDetail()));

        // 2. Top Rated
        List<Serie> topRated = source.stream()
            .filter(s -> s.getRatingMoyen() > 0)
            .sorted(Comparator.comparingDouble(Serie::getRatingMoyen).reversed())
            .limit(20).collect(Collectors.toList());
        if (!topRated.isEmpty())
            carouselContainer.getChildren().add(
                CardFactory.buildSerieCarousel("⭐  Top Rated", topRated,
                    overlayRef, goToSerieDetail()));

        // 3. Par genre
        Map<String, List<Serie>> byGenre = new LinkedHashMap<>();
        for (Serie s : source) {
            if (s.getGenres() == null) continue;
            for (Category c : s.getGenres())
                byGenre.computeIfAbsent(c.getName(), k -> new ArrayList<>()).add(s);
        }
        new TreeMap<>(byGenre).forEach((genre, series) -> {
            if (!series.isEmpty())
                carouselContainer.getChildren().add(
                    CardFactory.buildSerieCarousel("📺  " + genre, series,
                        overlayRef, goToSerieDetail()));
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  SEARCH
    // ═══════════════════════════════════════════════════════════════

    private void applySearch(String query) {
        if (query == null || query.isBlank()) { renderSections(allSeries); return; }
        String q = query.toLowerCase();
        List<Serie> results = allSeries.stream()
            .filter(s -> s.getTitre() != null && s.getTitre().toLowerCase().contains(q))
            .collect(Collectors.toList());

        carouselContainer.getChildren().clear();
        if (!results.isEmpty())
            carouselContainer.getChildren().add(
                CardFactory.buildSerieCarousel("🔍  \"" + query + "\"", results,
                    overlayRef, goToDetail()));
        else {
            Label empty = new Label("Aucune série trouvée pour \"" + query + "\"");
            empty.getStyleClass().add("empty-label");
            carouselContainer.getChildren().add(empty);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HANDLERS FXML
    // ═══════════════════════════════════════════════════════════════

    @FXML private void onHome()      { ScreenManager.getInstance().navigateTo(Screen.home); }
    @FXML private void onMovies()    { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void onSeries()    { /* déjà ici */ }
    @FXML private void onMyList()    {  ScreenManager.getInstance().navigateTo(Screen.myList); }
    @FXML private void onSearchBtn() { onSearch(); }

    @FXML
    private void onSearch() {
        applySearch(searchField != null ? searchField.getText().trim() : "");
    }
    private Consumer<Serie> goToSerieDetail() {
        return serie -> {
            EpisodeViewController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.detail);
            if (ctrl != null) {
                ctrl.setSerie(serie);
                // setSerie charge déjà le premier épisode automatiquement
            }
        };
    }

    @FXML
    private void onEditProfile() {
        ScreenManager.getInstance().navigateTo(Screen.editProfiles);
    }
}