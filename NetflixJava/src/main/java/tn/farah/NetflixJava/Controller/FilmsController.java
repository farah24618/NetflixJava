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
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class FilmsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;
    @FXML private FlowPane  filterBar;

    private FilmService filmService;
    private Pane        overlayPane;

    private List<Film>              allFilms        = new ArrayList<>();
    private Map<String, List<Film>> filmsByCategory = new LinkedHashMap<>();
    private final Set<String>       activeFilters   = new LinkedHashSet<>();

    // ── Exactement les mêmes constantes que HomeController ────────
    private static final double CARDS_VISIBLE = 1.5;
    private static final double GAP           = 12.0;
    private static final double ASPECT        = 5.0 / 10.0;
    private static final double HOVER_SCALE   = 1.5;

    // ═══════════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        if (connection == null) return;

        filmService = new FilmService(connection);

        try {
            allFilms        = filmService.getAllFilmsSorted();
            filmsByCategory = filmService.getAllFilmsByCategory();
        } catch (SQLException e) { e.printStackTrace(); }

        buildFilterBar();
        renderCarousels(filmsByCategory);

        if (avatarLabel != null) avatarLabel.setText("U");

        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> initOverlay(scene));
        });
    }

    private void initOverlay(javafx.scene.Scene scene) {
        if (overlayPane != null) return;
        if (!(scene.getRoot() instanceof Pane root)) return;

        overlayPane = new Pane();
        overlayPane.setMouseTransparent(false);
        overlayPane.setPickOnBounds(false);
        overlayPane.setPrefSize(scene.getWidth(), scene.getHeight());
        scene.widthProperty() .addListener((o, ow, nw) -> overlayPane.setPrefWidth(nw.doubleValue()));
        scene.heightProperty().addListener((o, oh, nh) -> overlayPane.setPrefHeight(nh.doubleValue()));
        root.getChildren().add(overlayPane);
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
            renderCarousels(filmsByCategory);
        });
        filterBar.getChildren().add(allBtn);
        filterBar.setHgap(10);
        filterBar.setVgap(30); // Espace vertical pour éviter que ça se touche
        //filterBar.setPadding(new javafx.geometry.Insets(10, 0, 20, 0));

        filmsByCategory.keySet().stream().sorted().forEach(cat -> {
            ToggleButton btn = new ToggleButton(cat);
            btn.getStyleClass().add("filter-chip");
            btn.setOnAction(e -> {
                allBtn.setSelected(false);
                if (btn.isSelected()) activeFilters.add(cat);
                else                  activeFilters.remove(cat);

                if (activeFilters.isEmpty()) {
                    allBtn.setSelected(true);
                    renderCarousels(filmsByCategory);
                } else {
                    Map<String, List<Film>> filtered = new LinkedHashMap<>();
                    activeFilters.forEach(f -> {
                        if (filmsByCategory.containsKey(f))
                            filtered.put(f, filmsByCategory.get(f));
                    });
                    renderCarousels(filtered);
                }
            });
            filterBar.getChildren().add(btn);
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  RENDER CAROUSELS
    // ═══════════════════════════════════════════════════════════════

    private void renderCarousels(Map<String, List<Film>> data) {
        carouselContainer.getChildren().clear();

        if (activeFilters.isEmpty() && !allFilms.isEmpty())
            carouselContainer.getChildren().add(buildCarousel("🎬 Tous les films", allFilms));
        carouselContainer.setSpacing(30);
        new TreeMap<>(data).forEach((cat, films) -> {
            if (!films.isEmpty())
                carouselContainer.getChildren().add(buildCarousel("🎬 " + cat, films));
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  SEARCH
    // ═══════════════════════════════════════════════════════════════

    private void applySearch(String query) {
        if (query == null || query.isBlank()) {
            renderCarousels(filmsByCategory);
            return;
        }
        String q = query.toLowerCase();
        List<Film> results = allFilms.stream()
            .filter(f -> f.getTitre() != null && f.getTitre().toLowerCase().contains(q))
            .toList();

        carouselContainer.getChildren().clear();
        if (!results.isEmpty()) {
            carouselContainer.getChildren().add(buildCarousel("Résultats : \"" + query + "\"", results));
        } else {
            Label empty = new Label("Aucun film trouvé pour \"" + query + "\"");
            empty.getStyleClass().add("empty-label");
            carouselContainer.getChildren().add(empty);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSEL — identique à HomeController
    // ═══════════════════════════════════════════════════════════════

    private VBox buildCarousel(String title, List<Film> films) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("carousel-title");

        HBox row = new HBox(GAP);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("carousel-row");

        ScrollPane scroll = buildScrollPane(row);

        scroll.widthProperty().addListener((obs, oldW, newW) -> {
            if (row.getUserData() != null) return;
            double cardW = computeCardWidth(newW.doubleValue());
            double cardH = cardW * ASPECT;

            scroll.setPrefHeight(cardH + 40);
            scroll.setMinHeight(cardH + 40);
            scroll.setMaxHeight(Double.MAX_VALUE);

            row.setUserData("built");
            row.getChildren().clear();
            films.forEach(f -> row.getChildren().add(buildFilmCard(f, cardW, cardH)));
        });

        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARTE FILM — identique à HomeController
    // ═══════════════════════════════════════════════════════════════

    private StackPane buildFilmCard(Film film, double cardW, double cardH) {
       
            Pane thumbnail = createThumbnail(film.getUrlImageCover(), cardW, cardH);
            StackPane card = new StackPane(thumbnail);
           
            // ON FORCE LES 3 TAILLES POUR BLOQUER LE CHANGEMENT
            card.setMinWidth(cardW);  card.setMinHeight(cardH);
            card.setPrefWidth(cardW); card.setPrefHeight(cardH);
            card.setMaxWidth(cardW);  card.setMaxHeight(cardH);

            card.setStyle("-fx-cursor: hand;");
            
        
       

        card.setOnMouseClicked(e -> {
            // FilmDetailController ctrl = ScreenManager.getInstance()
            //     .navigateAndGetController(Screen.FILM_DETAIL);
            // ctrl.setFilm(film);
        });

        VBox popup = buildFilmPopup(film, cardW * HOVER_SCALE);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        attachHoverBehavior(card, popup, cardW);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  POPUP FILM — identique à HomeController
    // ═══════════════════════════════════════════════════════════════

    private VBox buildFilmPopup(Film film, double popupW) {
        double thumbH = popupW * ASPECT;
        Pane bigThumb = createThumbnail(film.getUrlImageCover(), popupW, thumbH);

        int matchPct = computeMatch(film.getTitre());
        String age   = film.getAgeRating() != null ? film.getAgeRating().name() : "PG";

        Label titleLbl = new Label(film.getTitre() != null ? film.getTitre() : "");
        titleLbl.getStyleClass().add("popup-title");
        titleLbl.setMaxWidth(popupW - 28);

        Label matchLbl = new Label(matchPct + "% Match"); matchLbl.getStyleClass().add("popup-match");
        Label durLbl   = new Label(film.getDuree() + "m"); durLbl.getStyleClass().add("popup-meta");
        Label ageLbl   = new Label(age);  ageLbl.getStyleClass().addAll("popup-meta", "popup-badge");
        Label hdLbl    = new Label("HD"); hdLbl .getStyleClass().addAll("popup-meta", "popup-badge");
        Label typeLbl  = new Label("🎬 FILM"); typeLbl.getStyleClass().add("popup-type-film");

        HBox meta = new HBox(8, matchLbl, durLbl, ageLbl, hdLbl, typeLbl);
        meta.setAlignment(Pos.CENTER_LEFT);

        String genreText = "—";
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreText = film.getGenres().stream()
                .map(Category::getName)
                .limit(2)
                .reduce((a, b) -> a + " · " + b)
                .orElse("—");
        }
        Label genreLbl = new Label(genreText);
        genreLbl.getStyleClass().add("popup-genre");

        VBox info = buildPopupInfo(popupW, titleLbl, meta, genreLbl);
        return assemblePopup(popupW, thumbH, bigThumb, info);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS — copiés mot pour mot depuis HomeController
    // ═══════════════════════════════════════════════════════════════

    private ScrollPane buildScrollPane(HBox row) {
        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("carousel-scroll");
        return scroll;
    }

    private double computeCardWidth(double scrollWidth) {
        double widthToUse = 500;
        
        // Si on est en plein filtrage (scrollWidth proche de 0), 
        // on va chercher la largeur de la fenêtre globale
        if (widthToUse <= 100 && carouselContainer.getScene() != null) {
            widthToUse = carouselContainer.getScene().getWidth();
        }
        
        // Si toujours rien, valeur par défaut de secours
        if (widthToUse <= 100) widthToUse = 1200; 

        double available = widthToUse - 80;
        return (available - GAP * (CARDS_VISIBLE - 1)) / CARDS_VISIBLE;
    }

    private Pane createThumbnail(String imageUrl, double w, double h) {
        Pane pane = new Pane();
        pane.setPrefSize(w, h);
        pane.setMinSize(w, h);
        pane.setMaxSize(w, h);
        pane.setStyle(
            (imageUrl != null && !imageUrl.isEmpty())
                ? "-fx-background-image: url('" + imageUrl + "');" +
                  "-fx-background-size: cover; -fx-background-position: center;"
                : "-fx-background-color: #1a1a1a;"
        );
        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(6); clip.setArcHeight(6);
        pane.setClip(clip);
        return pane;
    }

    private VBox buildPopupInfo(double popupW, Label titleLbl, HBox meta, Label genreLbl) {
        Button playBtn = new Button("▶"); playBtn.getStyleClass().add("btn-round-white");
        Button addBtn  = new Button("+"); addBtn .getStyleClass().add("btn-round-outline");
        Button likeBtn = new Button("♥"); likeBtn.getStyleClass().add("btn-round-outline");
        Button moreBtn = new Button("▾"); moreBtn.getStyleClass().add("btn-round-outline");
        Region spacer  = new Region();    HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8, playBtn, addBtn, likeBtn, spacer, moreBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(10, actions, titleLbl, meta, genreLbl);
        info.getStyleClass().add("popup-info");
        info.setPrefWidth(popupW);
        return info;
    }

    private VBox assemblePopup(double popupW, double thumbH, Pane bigThumb, VBox info) {
        VBox container = new VBox(0, bigThumb, info);
        container.setPrefWidth(popupW);
        container.setMinWidth(popupW);
        container.setMaxWidth(popupW);
        Rectangle clip = new Rectangle(popupW, thumbH + 130);
        clip.setArcWidth(10); clip.setArcHeight(10);
        container.setClip(clip);
        return container;
    }

    private void attachHoverBehavior(StackPane card, VBox popup, double cardW) {
        double popupW = cardW * HOVER_SCALE;

        Timeline showTimer = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            if (overlayPane == null) return;
            Bounds b    = card.localToScene(card.getBoundsInLocal());
            double x    = b.getMinX() - (popupW - cardW) / 2.0;
            double y    = b.getMinY() - 20;
            double maxX = overlayPane.getScene().getWidth() - popupW - 10;

            popup.setLayoutX(Math.max(10, Math.min(x, maxX)));
            popup.setLayoutY(Math.max(10, y));
            if (!overlayPane.getChildren().contains(popup))
                overlayPane.getChildren().add(popup);

            popup.setVisible(true);
            FadeTransition showFade = new FadeTransition(Duration.millis(180), popup);
            showFade.setToValue(1.0);
            showFade.play();
        }));

        Timeline hideTimer = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), popup);
            ft.setToValue(0);
            ft.setOnFinished(ev -> {
                popup.setVisible(false);
                if (overlayPane != null) overlayPane.getChildren().remove(popup);
            });
            ft.play();
        }));

        card.setOnMouseEntered(e -> { hideTimer.stop(); showTimer.playFromStart(); });
        card.setOnMouseExited (e -> { showTimer.stop(); hideTimer.playFromStart(); });
        popup.setPickOnBounds(false);
        popup.setMouseTransparent(false);
        popup.setOnMouseEntered(e -> hideTimer.stop());
        popup.setOnMouseExited (e -> hideTimer.playFromStart());
    }

    private int computeMatch(String titre) {
        return 60 + (Math.abs(titre != null ? titre.hashCode() : 0) % 35);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HANDLERS FXML
    // ═══════════════════════════════════════════════════════════════

    @FXML private void onHome()      { ScreenManager.getInstance().navigateTo(Screen.mainView); }
    @FXML private void onMovies()    { /* déjà sur cette page */ }
    @FXML private void onSeries()    { ScreenManager.getInstance().navigateTo(Screen.mainView); }
    @FXML private void onMyList()    { ScreenManager.getInstance().navigateTo(Screen.mainView); }
    @FXML private void onSearchBtn() { onSearch(); }

    @FXML
    private void onSearch() {
        applySearch(searchField != null ? searchField.getText().trim() : "");
    }
}