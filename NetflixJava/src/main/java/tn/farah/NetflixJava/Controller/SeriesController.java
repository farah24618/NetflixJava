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
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class SeriesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label     avatarLabel;
    @FXML private VBox      carouselContainer;
    @FXML private FlowPane  filterBar;

    private SerieService serieService;
    private Pane         overlayPane;

    private List<Serie>             allSeries      = new ArrayList<>();
    private Map<String, List<Serie>> seriesByGenre = new LinkedHashMap<>();
    private final Set<String>       activeFilters  = new LinkedHashSet<>();

    private static final double CARDS_VISIBLE = 3.0;
    private static final double GAP           = 12.0;
    private static final double ASPECT        = 9.0 / 16.0;
    private static final double HOVER_SCALE   = 2.0;

    // ═══════════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        if (connection == null) return;

        serieService = new SerieService(connection);

        try {
            allSeries    = serieService.getAllFilmsSorted();
            seriesByGenre = serieService.getAllFilmsByCategory();
        } catch (SQLException e) { e.printStackTrace(); }

        buildFilterBar();
        renderCarousels(seriesByGenre);

        if (avatarLabel != null) avatarLabel.setText("U");

        carouselContainer.sceneProperty().addListener((obs, old, scene) -> {
            if (scene == null) return;
            Platform.runLater(() -> initOverlay(scene));
        });
    }

    // ─── Overlay ────────────────────────────────────────────────────
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
            renderCarousels(seriesByGenre);
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
                    renderCarousels(seriesByGenre);
                } else {
                    Map<String, List<Serie>> filtered = new LinkedHashMap<>();
                    activeFilters.forEach(f -> {
                        if (seriesByGenre.containsKey(f))
                            filtered.put(f, seriesByGenre.get(f));
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

    private void renderCarousels(Map<String, List<Serie>> data) {
        carouselContainer.getChildren().clear();

        if (activeFilters.isEmpty() && !allSeries.isEmpty())
            carouselContainer.getChildren().add(buildCarousel("📺 Toutes les séries", allSeries));

        new TreeMap<>(data).forEach((genre, series) -> {
            if (!series.isEmpty())
                carouselContainer.getChildren().add(buildCarousel("📺 " + genre, series));
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  SEARCH
    // ═══════════════════════════════════════════════════════════════

    private void applySearch(String query) {
        if (query == null || query.isBlank()) {
            renderCarousels(seriesByGenre);
            return;
        }
        String q = query.toLowerCase();
        List<Serie> results = allSeries.stream()
            .filter(s -> s.getTitre() != null && s.getTitre().toLowerCase().contains(q))
            .toList();

        carouselContainer.getChildren().clear();
        if (!results.isEmpty())
            carouselContainer.getChildren().add(buildCarousel("Résultats : \"" + query + "\"", results));
        else {
            Label empty = new Label("Aucune série trouvée pour \"" + query + "\"");
            empty.getStyleClass().add("empty-label");
            carouselContainer.getChildren().add(empty);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CAROUSEL
    // ═══════════════════════════════════════════════════════════════

    private VBox buildCarousel(String title, List<Serie> series) {
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
            series.forEach(s -> row.getChildren().add(buildSerieCard(s, cardW, cardH)));
        });

        VBox bloc = new VBox(10, titleLabel, scroll);
        bloc.getStyleClass().add("carousel-block");
        return bloc;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CARTE SÉRIE
    // ═══════════════════════════════════════════════════════════════

    private StackPane buildSerieCard(Serie serie, double cardW, double cardH) {
        Pane thumbnail = createThumbnail(serie.getUrlImageCover(), cardW, cardH);

        Label badge = new Label("SÉRIE");
        badge.getStyleClass().add("serie-badge");
        StackPane.setAlignment(badge, Pos.BOTTOM_LEFT);

        StackPane card = new StackPane(thumbnail, badge);
        card.setPrefSize(cardW, cardH);
        card.setMinSize(cardW, cardH);
        card.setMaxSize(cardW, cardH);
        card.setStyle("-fx-cursor: hand;");

       /* card.setOnMouseClicked(e ->
            ScreenManager.getInstance().navigateTo(Screen.SERIE_DETAIL)
        );*/

        VBox popup = buildSeriePopup(serie, cardW * HOVER_SCALE);
        popup.setVisible(false);
        popup.setOpacity(0);
        popup.setEffect(new DropShadow(24, 0, 8, Color.rgb(0, 0, 0, 0.9)));

        attachHoverBehavior(card, popup, cardW);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  POPUP SÉRIE
    // ═══════════════════════════════════════════════════════════════

    private VBox buildSeriePopup(Serie serie, double popupW) {
        double thumbH = popupW * ASPECT;
        Pane bigThumb = createThumbnail(serie.getUrlImageCover(), popupW, thumbH);
        int matchPct  = computeMatch(serie.getTitre());

        Label titleLbl = new Label(serie.getTitre() != null ? serie.getTitre() : "");
        titleLbl.getStyleClass().add("popup-title");
        titleLbl.setMaxWidth(popupW - 28);

        Label matchLbl  = new Label(matchPct + "% Match"); matchLbl.getStyleClass().add("popup-match");
        Label statusLbl = new Label(serie.isTerminee() ? "Terminée" : "En cours");
        statusLbl.getStyleClass().add(serie.isTerminee() ? "popup-badge-ended" : "popup-badge-ongoing");
        Label typeLbl   = new Label("📺 SÉRIE"); typeLbl.getStyleClass().add("popup-type-serie");

        HBox meta = new HBox(8, matchLbl, statusLbl, typeLbl);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label genreLbl = new Label(serie.getGenre() != null ? serie.getGenre() : "—");
        genreLbl.getStyleClass().add("popup-genre");

        VBox info = buildPopupInfo(popupW, titleLbl, meta, genreLbl);
        return assemblePopup(popupW, thumbH, bigThumb, info);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════

    private ScrollPane buildScrollPane(HBox row) {
        ScrollPane scroll = new ScrollPane(row);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPannable(true);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("carousel-scroll");
        return scroll;
    }

    private double computeCardWidth(double scrollWidth) {
        return (500- 80 - GAP * (CARDS_VISIBLE - 1)) / CARDS_VISIBLE;
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
            FadeTransition ft = new FadeTransition(Duration.millis(180), popup);
            ft.setToValue(1.0);
            ft.play();
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

        card .setOnMouseEntered(e -> { hideTimer.stop(); showTimer.playFromStart(); });
        card .setOnMouseExited (e -> { showTimer.stop(); hideTimer.playFromStart(); });
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

    @FXML private void onHome()      { ScreenManager.getInstance().navigateTo(Screen.home); }
    @FXML private void onMovies()    { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void onSeries()    { /* déjà ici */ }
    @FXML private void onMyList()    { /*ScreenManager.getInstance().navigateTo(Screen.mainView);*/ }
    @FXML private void onSearchBtn() { onSearch(); }

    @FXML
    private void onSearch() {
        applySearch(searchField != null ? searchField.getText().trim() : "");
    }
}