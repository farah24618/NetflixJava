package tn.farah.NetflixJava.Controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.NotificationService;
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

public class FilmsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;
    @FXML private FlowPane  filterBar;

    private FilmService filmService;
    private FavoriService favoriService;

    private final Pane[] overlayRef = new Pane[1];

    private List<Film>              allFilms        = new ArrayList<>();
    private Map<String, List<Film>> filmsByCategory = new LinkedHashMap<>();
    private final Set<String>       activeFilters   = new LinkedHashSet<>();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        if (connection == null) return;

        filmService = new FilmService(connection);
        favoriService=new FavoriService(connection);
        CardFactory.setNotificationService(new NotificationService(connection));
        CardFactory.setFavoriService(favoriService);

        try {
            allFilms        = filmService.getAllFilmsSorted();
            filmsByCategory = filmService.getAllFilmsByCategory();
        } catch (SQLException e) { e.printStackTrace(); }
        CardFactory.setFavoriService(favoriService);
        buildFilterBar();
        renderSections(allFilms);

        if (avatarLabel != null) avatarLabel.setText("U");

        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> CardFactory.createOverlay(scene, overlayRef));
        });
    }


    private Consumer<Film> goToDetail() {
        return film -> {
            MediaViewController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.MediaView);
            if (ctrl != null) ctrl.setFilm(film);
        };
    }

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
            renderSections(allFilms);
        });
        filterBar.getChildren().add(allBtn);

        filmsByCategory.keySet().stream().sorted().forEach(cat -> {
            ToggleButton btn = new ToggleButton(cat);
            btn.getStyleClass().add("filter-chip");
            btn.setOnAction(e -> {
                allBtn.setSelected(false);
                if (btn.isSelected()) activeFilters.add(cat);
                else                  activeFilters.remove(cat);

                if (activeFilters.isEmpty()) {
                    allBtn.setSelected(true);
                    renderSections(allFilms);
                } else {
                    List<Film> filtered = allFilms.stream()
                        .filter(f -> f.getGenres() != null &&
                            activeFilters.stream().allMatch(filter ->
                                f.getGenres().stream().anyMatch(c -> c.getName().equals(filter))))
                        .collect(Collectors.toList());
                    renderSections(filtered);
                }
            });
            filterBar.getChildren().add(btn);
        });
    }


    private void renderSections(List<Film> source) {
        carouselContainer.getChildren().clear();

        if (source.isEmpty()) {
            Label empty = new Label("Aucun film trouvé.");
            empty.getStyleClass().add("empty-label");
            carouselContainer.getChildren().add(empty);
            return;
        }


        List<Film> recent = source.stream()
            .filter(f -> f.getDateSortie() != null)
            .sorted(Comparator.comparing(Film::getDateSortie).reversed())
            .limit(20).collect(Collectors.toList());
        if (!recent.isEmpty())
            carouselContainer.getChildren().add(
            		CardFactory.buildFilmCarousel("🆕  Récemment ajoutés", recent, overlayRef, goToFilmDetail()));

        List<Film> topRated = source.stream()
            .filter(f -> f.getRatingMoyen() > 0)
            .sorted(Comparator.comparingDouble(Film::getRatingMoyen).reversed())
            .limit(20).collect(Collectors.toList());
        if (!topRated.isEmpty())
            carouselContainer.getChildren().add(
                CardFactory.buildFilmCarousel("⭐  Top Rated", topRated,
                    overlayRef, goToDetail()));

        // 3. Par catégorie
        Map<String, List<Film>> bycat = new LinkedHashMap<>();
        for (Film f : source) {
            if (f.getGenres() == null) continue;
            for (Category c : f.getGenres())
                bycat.computeIfAbsent(c.getName(), k -> new ArrayList<>()).add(f);
        }
        new TreeMap<>(bycat).forEach((cat, films) -> {
            if (!films.isEmpty())
                carouselContainer.getChildren().add(
                    CardFactory.buildFilmCarousel("🎬  " + cat, films,
                        overlayRef, goToDetail()));
        });
    }



    private void applySearch(String query) {
        if (query == null || query.isBlank()) { renderSections(allFilms); return; }
        String q = query.toLowerCase();
        List<Film> results = allFilms.stream()
            .filter(f -> f.getTitre() != null && f.getTitre().toLowerCase().contains(q))
            .collect(Collectors.toList());

        carouselContainer.getChildren().clear();
        if (!results.isEmpty())
            carouselContainer.getChildren().add(
                CardFactory.buildFilmCarousel("🔍  \"" + query + "\"", results,
                    overlayRef, goToDetail()));
        else {
            Label empty = new Label("Aucun film trouvé pour \"" + query + "\"");
            empty.getStyleClass().add("empty-label");
            carouselContainer.getChildren().add(empty);
        }
    }



    @FXML private void onHome()      { ScreenManager.getInstance().navigateTo(Screen.home); }
    @FXML private void onMovies()    { /* déjà ici */ }
    @FXML private void onSeries()    { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML private void onMyList()    { ScreenManager.getInstance().navigateTo(Screen.myList); }
    @FXML private void onSearchBtn() { onSearch(); }

    @FXML
    private void onSearch() {
        applySearch(searchField != null ? searchField.getText().trim() : "");
    }
    private Consumer<Film> goToFilmDetail() {
        return film -> {
            final MediaViewController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.MediaView);
            if (ctrl != null) ctrl.setFilm(film);
        };
    }
    @FXML
    private void onEditProfile() {
        ScreenManager.getInstance().navigateTo(Screen.editProfiles);
    }
    
}