package tn.farah.NetflixJava.Controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.CategoryService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.*;

public class SearchResultsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label searchQueryLabel;
    @FXML private Label resultCountLabel;

    // Filtres
    @FXML private ComboBox<String> yearFromCombo;
    @FXML private ComboBox<String> yearToCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Slider ratingSlider;
    @FXML private Label ratingValueLabel;
    @FXML private FlowPane genreChipPane;

    // Sections résultats
    @FXML private VBox noResultsPane;
    @FXML private VBox filmsSection;
    @FXML private VBox seriesSection;
    @FXML private HBox filmsRow;
    @FXML private HBox seriesRow;
    @FXML private Label filmsCountLabel;
    @FXML private Label seriesCountLabel;
    @FXML private ScrollPane filmsScroll;
    @FXML private ScrollPane seriesScroll;

    private FilmService filmService;
    private SerieService serieService;
    private SaisonService saisonService;
    private CategoryService categoryService;
    private Pane[] overlayRef = new Pane[1];

    private List<Film> allFilms = new ArrayList<>();
    private List<Serie> allSeries = new ArrayList<>();
    private Set<String> selectedGenres = new HashSet<>();
    private String currentQuery = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	Connection connection=ConxDB.getInstance();
        filmService  = new FilmService(connection);
        serieService = new SerieService(connection);
        saisonService=new SaisonService(connection);
        categoryService=new CategoryService(connection);

        setupYearCombos();
        setupSortCombo();
        setupRatingSlider();

        // Recherche en temps réel
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentQuery = newVal.trim();
            loadData();
        });
    }

    // ── Appelé depuis l'extérieur pour lancer la recherche ───────────────
    public void initSearch(String query) {
        currentQuery = query;
        searchField.setText(query);
        loadData();

        // Overlay après que la scène soit prête
        searchField.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null)
                CardFactory.createOverlay(newScene, overlayRef);
        });
    }

    // ── Chargement & filtrage ─────────────────────────────────────────────
    private void loadData() {
        try {
            if (currentQuery.isEmpty()) {
                allFilms  = filmService.getAllFilmsSorted();
                allSeries = serieService.getAllSeries();
            } else {
                allFilms  = filmService.searchByTitle(currentQuery);
                allSeries = serieService.searchByTitle(currentQuery);
            }
            searchQueryLabel.setText("Résultats pour « " + currentQuery + " »");
            buildGenreChips();
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearchEnter() {
        currentQuery = searchField.getText().trim();
        loadData();
    }

    @FXML
    private void applyFilters() {
        List<Film>  films  = new ArrayList<>(allFilms);
        List<Serie> series = new ArrayList<>(allSeries);

        // Filtre année
        String fromStr = yearFromCombo.getValue();
        String toStr   = yearToCombo.getValue();
        if (fromStr != null && !fromStr.isEmpty()) {
            int from = Integer.parseInt(fromStr);
            films  = films .stream().filter(f -> f.getDateSortie().getYear() >= from).collect(Collectors.toList());
            series = series.stream().filter(s -> s.getDateSortie().getYear() >= from).collect(Collectors.toList());
        }
        if (toStr != null && !toStr.isEmpty()) {
            int to = Integer.parseInt(toStr);
            films  = films .stream().filter(f -> f.getDateSortie().getYear() <= to).collect(Collectors.toList());
            series = series.stream().filter(s -> s.getDateSortie().getYear() <= to).collect(Collectors.toList());
        }

        // Filtre note
        double minRating = ratingSlider.getValue();
        if (minRating > 0) {
            films  = films .stream().filter(f -> f.getRatingMoyen() >= minRating).collect(Collectors.toList());
            series = series.stream().filter(s -> s.getRatingMoyen() >= minRating).collect(Collectors.toList());
        }

        // Filtre genres
        if (!selectedGenres.isEmpty()) {
            films  = films.stream().filter(f ->
                f.getGenres() != null && f.getGenres().stream()
                    .anyMatch(g -> selectedGenres.contains(g.getName()))
            ).collect(Collectors.toList());
            series = series.stream().filter(s ->
                s.getGenres() != null && s.getGenres().stream()
                    .anyMatch(g -> selectedGenres.contains(g.getName()))
            ).collect(Collectors.toList());
        }

        // Tri
        String sort = sortCombo.getValue();
        if ("Note ↓".equals(sort)) {
            films .sort((a, b) -> Double.compare(b.getRatingMoyen(), a.getRatingMoyen()));
            series.sort((a, b) -> Double.compare(b.getRatingMoyen(), a.getRatingMoyen()));
        } else if ("Année ↓".equals(sort)) {
            films .sort((a, b) -> Integer.compare(b.getDateSortie().getYear(), a.getDateSortie().getYear()));
            series.sort((a, b) -> Integer.compare(b.getDateSortie().getYear(), a.getDateSortie().getYear()));
        } else if ("Titre A-Z".equals(sort)) {
            films .sort(Comparator.comparing(Film::getTitre,  Comparator.nullsLast(String::compareToIgnoreCase)));
            series.sort(Comparator.comparing(Serie::getTitre, Comparator.nullsLast(String::compareToIgnoreCase)));
        }

        renderResults(films, series);
    }

    @FXML
    private void resetFilters() {
        yearFromCombo.setValue(null);
        yearToCombo.setValue(null);
        sortCombo.setValue("Pertinence");
        ratingSlider.setValue(0);
        selectedGenres.clear();
        refreshGenreChips();
        applyFilters();
    }

    // ── Rendu des résultats ───────────────────────────────────────────────
    private void renderResults(List<Film> films, List<Serie> series) {
        filmsRow.getChildren().clear();
        seriesRow.getChildren().clear();

        int total = films.size() + series.size();
        resultCountLabel.setText(total + " résultat" + (total > 1 ? "s" : ""));

        // No results
        boolean empty = total == 0;
        noResultsPane.setVisible(empty);
        noResultsPane.setManaged(empty);

        // Films
        boolean hasFilms = !films.isEmpty();
        filmsSection.setVisible(hasFilms);
        filmsSection.setManaged(hasFilms);
        filmsCountLabel.setText(hasFilms ? "(" + films.size() + ")" : "");
     // Films
        films.forEach(f -> {
            StackPane card = CardFactory.buildFilmCard(f, CardFactory.CARD_W, CardFactory.CARD_H, overlayRef,
                film -> {
                    FilmViewController ctrl = ScreenManager.getInstance()
                        .navigateAndGetController(Screen.detailFilm);
                    if (ctrl != null) ctrl.setFilm(film);
                });

            Label nameLbl = new Label(f.getTitre());
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-max-width: " 
                + CardFactory.CARD_W + "; -fx-wrap-text: true;");

            Label infoLbl = new Label(f.getDateSortie().getYear() + " · " + String.format("%.1f", f.getRatingMoyen()) + "★");
            infoLbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");

            VBox cardBox = new VBox(6, card, nameLbl, infoLbl);
            filmsRow.getChildren().add(cardBox);
        });

        
        // Séries
        boolean hasSeries = !series.isEmpty();
        seriesSection.setVisible(hasSeries);
        seriesSection.setManaged(hasSeries);
        seriesCountLabel.setText(hasSeries ? "(" + series.size() + ")" : "");
     // Séries
        series.forEach(s -> {
        		 StackPane card = CardFactory.buildSerieCard(s, CardFactory.CARD_W, CardFactory.CARD_H, overlayRef,
                serie -> {
                	int firstSeasonId = saisonService.findFirstSeasonIdBySerie(s.getId());
                    EpisodeViewController ctrl = ScreenManager.getInstance()
                        .navigateAndGetController(Screen.detail);
                    if (ctrl != null) ctrl.setSerie(serie);
                    ctrl.setSaisonId(firstSeasonId);});
                    Label nameLbl = new Label(s.getTitre());
                    nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-max-width: " 
                        + CardFactory.CARD_W + "; -fx-wrap-text: true;");

                    Label infoLbl = new Label(s.getDateSortie().getYear() + " · " + String.format("%.1f", s.getRatingMoyen()) + "★");
                    infoLbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");

                    VBox cardBox = new VBox(6, card, nameLbl, infoLbl);
                    seriesRow.getChildren().add(cardBox);
                });
        
    }

    // ── Genre chips ───────────────────────────────────────────────────────
    private void buildGenreChips() {
        List<Category> genres; 
        try {
            genres = categoryService.getAllCategoriesSorted();
        } catch (SQLException e) {
            
            e.printStackTrace(); 
            return; 
        }

        if (genres != null) {
            genreChipPane.getChildren().clear();
            
            for (Category genre : genres) {
                String name = genre.getName();
                Button chip = new Button(name);
                
                
                styleChip(chip, selectedGenres.contains(name));

                chip.setOnAction(e -> {
                    if (selectedGenres.contains(name)) {
                        selectedGenres.remove(name);
                    } else {
                        selectedGenres.add(name);
                    }
                    
                    styleChip(chip, selectedGenres.contains(name));
                    applyFilters();
                });

                genreChipPane.getChildren().add(chip);
            }
        }
    }
    private void refreshGenreChips() {
        genreChipPane.getChildren().forEach(node -> {
            if (node instanceof Button chip)
                styleChip(chip, false);
        });
    }

    private void styleChip(Button chip, boolean selected) {
        chip.setStyle(selected
            ? "-fx-background-color: #E50914; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 14; -fx-font-size: 12px; -fx-cursor: hand;"
            : "-fx-background-color: #2a2a2a; -fx-text-fill: #aaaaaa; -fx-background-radius: 20; -fx-padding: 4 14; -fx-font-size: 12px; -fx-border-color: #444; -fx-border-radius: 20; -fx-cursor: hand;");
    }

    // ── Setup helpers ─────────────────────────────────────────────────────
    private void setupYearCombos() {
        int currentYear = java.time.Year.now().getValue();
        List<String> years = new ArrayList<>();
        years.add("");
        for (int y = currentYear; y >= 1990; y--) years.add(String.valueOf(y));
        yearFromCombo.getItems().addAll(years);
        yearToCombo.getItems().addAll(years);
    }

    private void setupSortCombo() {
        sortCombo.getItems().addAll("Pertinence", "Note ↓", "Année ↓", "Titre A-Z");
        sortCombo.setValue("Pertinence");
    }

    private void setupRatingSlider() {
        ratingValueLabel.setText("0.0+");
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double v = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            ratingValueLabel.setText(v + "+");
            applyFilters();
        });
    }

    @FXML
    private void handleRetour() {
        ScreenManager.getInstance().navigateTo(Screen.home);
    }
}
