package tn.farah.NetflixJava.Controller;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.History;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.HistoryService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

public class MyListController implements Initializable {

    @FXML private VBox             pageContainer;
    @FXML private VBox             emptyState;
    @FXML private Label            totalLabel;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterGenre;
    @FXML private ComboBox<String> sortCombo;
    @FXML private TextField        searchField;

    private FavoriService  favoriService;
    private FilmService    filmService;
    private SerieService   serieService;
    private HistoryService historyService;
    private EpisodeService episodeService;
    private SaisonService saisonService;

    private List<Favori>  allFavoris;
    private List<History> allHistory;
    private int           userId;

    // ── Dimensions ───────────────────────────────────────────────────────────
    private static final double CARD_W       = 160;   // largeur par défaut
    private static final double CARD_W_HOVER = 320;   // largeur au hover (double)
    private static final double CARD_H       = 220;   // hauteur image fixe
    private static final double INFO_H       = 58;    // hauteur zone infos SOUS l'image
    private static final double BAR_H        = 4;     // hauteur barre de progression
    private static final double RADIUS       = 6;
    private static final int    ANIM_MS      = 250;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection conn = ConxDB.getInstance();
        favoriService  = new FavoriService(conn);
        filmService    = new FilmService(conn);
        serieService   = new SerieService(conn);
        historyService = new HistoryService(conn);
        saisonService= new SaisonService(conn);
        episodeService=new EpisodeService(conn);
        userId = SessionManager.getInstance().getCurrentUserId();
        initCombos();
        loadPage();
    }

    private void initCombos() {
        filterType.getItems().addAll("Tous", "Film", "Série");
        filterType.setValue("Tous");
        Set<String> genres = new TreeSet<>();
        try {
            filmService.getAllFilmsByCategory().keySet().forEach(genres::add);
            serieService.getAllSeriesByCategory().keySet().forEach(genres::add);
        } catch (Exception ignored) {}

        filterGenre.getItems().add("Tous");
        filterGenre.getItems().addAll(genres);
        filterGenre.setValue("Tous");
        sortCombo.getItems().addAll("Ajout récent", "Titre A→Z", "Titre Z→A");
        sortCombo.setValue("Ajout récent");
    }

    private void loadPage() {
        pageContainer.getChildren().clear();
        if (userId == -1) { showEmpty("Connectez-vous pour voir votre liste."); return; }

        allHistory = historyService.findByUser(userId);
        allFavoris = favoriService.getFavorisByUser(userId);

        boolean hasHistory = allHistory != null && !allHistory.isEmpty();
        boolean hasFavoris = allFavoris != null && !allFavoris.isEmpty();

        if (!hasHistory && !hasFavoris) { showEmpty("Votre liste est vide."); return; }

        emptyState.setVisible(false);
        emptyState.setManaged(false);

        int total = hasFavoris ? allFavoris.size() : 0;
        totalLabel.setText(total + " titre(s) dans Ma Liste");

        if (hasHistory) {
            pageContainer.getChildren().add(sectionHeader("▶  Continuer à regarder"));
            buildHistoryRow(allHistory);
        }
        if (hasFavoris) {
            pageContainer.getChildren().add(sectionHeader("☰  Ma Liste"));
            buildFavorisRow(allFavoris);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HISTORY ROW
    // ═══════════════════════════════════════════════════════════════════════
    private void buildHistoryRow(List<History> histories) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10, 40, 16, 40));
        row.setAlignment(Pos.BOTTOM_LEFT);

        for (History h : histories) {
            String titre, posterUrl, subInfo;
            int    mediaId;
            double progress;
            int saisonNbr;
            int episodeNbre;

            if (h.isFilm()) {
                Film f = filmService.findById(h.getFilmId());
                if (f == null) continue;
                titre     = f.getTitre();
                posterUrl = f.getUrlImageCover();
                mediaId   = f.getId();
                progress  = historyService.getProgressPercentFilm(h.getId());
                subInfo   = formatTimeLeft(h);
            } else {
                Serie s = serieService.findByEpisodeId(h.getEpisodeId());
                if (s == null) continue;
                
                posterUrl = s.getUrlImageCover();
                mediaId   = s.getId();
                progress  = historyService.getProgressPercentEpisode(h.getId());
                subInfo   = formatTimeLeft(h);
                saisonNbr = saisonService.getSaisonbyEpisodeId(h.getEpisodeId()).getNumeroSaison();
                episodeNbre = episodeService.findById(h.getEpisodeId()).getNumeroEpisode();
                
                // AJOUT : Formatage avec S et E directement à côté du titre
                titre     = s.getTitre() + " (S " + saisonNbr + " E " + episodeNbre + ")";
            }

            row.getChildren().add(
                buildCard(titre, posterUrl, progress, subInfo,true, h, null, mediaId)
            );
        }

        ScrollPane sp = transparentScroll(row);
        sp.setPrefHeight(CARD_H + BAR_H + INFO_H + 30);
        pageContainer.getChildren().add(sp);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FAVORIS ROW
    // ═══════════════════════════════════════════════════════════════════════
    private void buildFavorisRow(List<Favori> favoris) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10, 40, 16, 40));
        row.setAlignment(Pos.BOTTOM_LEFT);

        for (Favori fav : favoris) {
            Film  film  = filmService.findById(fav.getMediaId());
            Serie serie = (film == null) ? serieService.findById(fav.getMediaId()) : null;

            String titre     = film != null ? film.getTitre()         : (serie != null ? serie.getTitre()         : "?");
            String posterUrl = film != null ? film.getUrlImageCover() : (serie != null ? serie.getUrlImageCover() : null);
            String badge     = film != null ? "Film" : "Série";

            row.getChildren().add(
                buildCard(titre, posterUrl, -1, badge, false, null, fav, fav.getMediaId())
            );
        }

        ScrollPane sp = transparentScroll(row);
        sp.setPrefHeight(CARD_H + INFO_H + 30);
        pageContainer.getChildren().add(sp);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CARD BUILDER
    // ═══════════════════════════════════════════════════════════════════════
    private VBox buildCard(String titre, String posterUrl,
                           double progress,
                           String subInfo,
                           boolean isHistory,
                           History history,
                           Favori  favori,
                           int mediaId ) {

        // ─────────────────────────────────────────────────────────────────
        // 1. Clip animé — contrôle la largeur visible de l'image
        // ─────────────────────────────────────────────────────────────────
        Rectangle clip = new Rectangle(CARD_W, CARD_H);
        clip.setArcWidth(RADIUS * 2);
        clip.setArcHeight(RADIUS * 2);

        // ─────────────────────────────────────────────────────────────────
        // 2. imagePane — taille max dès le départ, clippé à CARD_W
        // ─────────────────────────────────────────────────────────────────
        StackPane imagePane = new StackPane();
        imagePane.setPrefSize(CARD_W_HOVER, CARD_H);
        imagePane.setMinSize(CARD_W_HOVER, CARD_H);
        imagePane.setMaxSize(CARD_W_HOVER, CARD_H);
        imagePane.setClip(clip);

        Region placeholder = new Region();
        placeholder.setPrefSize(CARD_W_HOVER, CARD_H);
        placeholder.setStyle("-fx-background-color: #2a2a2a;");

        ImageView iv = buildImageView(posterUrl, CARD_W_HOVER, CARD_H);

        // Gradient sombre en bas de l'image (caché par défaut)
        Region gradient = new Region();
        gradient.setPrefSize(CARD_W_HOVER, CARD_H);
        gradient.setStyle(
            "-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.80) 0%, transparent 55%);");
        gradient.setVisible(false);

        Button btnX = makeXButton();
        StackPane.setAlignment(btnX, Pos.TOP_RIGHT);
        StackPane.setMargin(btnX, new Insets(6, 6, 0, 0));
        btnX.setVisible(false);

        imagePane.getChildren().addAll(placeholder, iv, gradient, btnX);

        btnX.setOnAction(e -> {
            e.consume();
            if (isHistory) {
                if (confirmDelete("Supprimer l'historique",
                        "Retirer \"" + titre + "\" de votre historique ?")) {
                    historyService.delete(history.getId());
                    loadPage();
                }
            } else {
                if (confirmDelete("Retirer de Ma Liste",
                        "Retirer \"" + titre + "\" de votre liste ?")) {
                    favoriService.supprimerFavori(favori.getUserId(), favori.getMediaId());
                    loadPage();
                }
            }
        });

        // ─────────────────────────────────────────────────────────────────
        // 3. Barre de progression rouge (history seulement)
        // ─────────────────────────────────────────────────────────────────
        AnchorPane barPane    = null;
        Region     trackFillRef = null;

        if (isHistory) {
            double pct   = (progress >= 0) ? Math.min(Math.max(progress, 0.0), 1.0) : 0.0;
            double fillW = pct * CARD_W;

            barPane = new AnchorPane();
            barPane.setPrefHeight(BAR_H);
            barPane.setMinHeight(BAR_H);
            barPane.setMaxHeight(BAR_H);
            barPane.setPrefWidth(CARD_W);
            barPane.setMaxWidth(CARD_W_HOVER);

            Region trackBg = new Region();
            AnchorPane.setLeftAnchor(trackBg,   0.0);
            AnchorPane.setRightAnchor(trackBg,  0.0);
            AnchorPane.setTopAnchor(trackBg,    0.0);
            AnchorPane.setBottomAnchor(trackBg, 0.0);
            trackBg.setStyle("-fx-background-color: rgba(255,255,255,0.25);");

            Region trackFill = new Region();
            trackFill.setPrefWidth(fillW);
            trackFill.setMaxWidth(fillW);
            trackFill.setMinWidth(0);
            trackFill.setPrefHeight(BAR_H);
            AnchorPane.setLeftAnchor(trackFill,   0.0);
            AnchorPane.setTopAnchor(trackFill,    0.0);
            AnchorPane.setBottomAnchor(trackFill, 0.0);
            trackFill.setStyle("-fx-background-color: #e50914;");

            barPane.getChildren().addAll(trackBg, trackFill);
            trackFillRef = trackFill;
        }

        // ─────────────────────────────────────────────────────────────────
        // 4. infoPane — SOUS l'image, fond sombre
        // ─────────────────────────────────────────────────────────────────
        VBox infoPane = new VBox(3);
        infoPane.setPrefHeight(INFO_H);
        infoPane.setMaxHeight(INFO_H);
        infoPane.setMinHeight(INFO_H);
        infoPane.setPadding(new Insets(8, 10, 8, 10));
        infoPane.setStyle("-fx-background-color: #181818; -fx-background-radius: 0 0 6 6;");
        infoPane.setVisible(false);
        infoPane.setManaged(false);

        Label lblTitre = new Label(titre);
        lblTitre.setMaxWidth(CARD_W_HOVER - 20);
        lblTitre.setWrapText(false);
        lblTitre.setEllipsisString("…");
        lblTitre.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label lblSub = new Label(subInfo != null ? subInfo : "");
        lblSub.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px;");

     // Remplacez la ligne du lblBadge par celle-ci :
        String badgeText = "";
        if (isHistory && history != null) {
            badgeText = !history.getEstTermine() ? "● En cours" : "Terminé";
        } else {
            // Si c'est un favori, on affiche juste le subInfo (Film ou Série)
            badgeText = (subInfo != null) ? subInfo : "";
        }

        Label lblBadge = new Label(badgeText);
        lblBadge.setStyle("-fx-text-fill: #e50914; -fx-font-size: 10px; -fx-font-weight: bold;");

        infoPane.getChildren().addAll(lblTitre, lblSub, lblBadge);

        // ─────────────────────────────────────────────────────────────────
        // 5. Assemblage VBox card (animée)
        // ─────────────────────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_LEFT);
        card.setCursor(Cursor.HAND);
        card.setPrefWidth(CARD_W);
        card.setMaxWidth(CARD_W);
        card.setMinWidth(CARD_W);

        if (isHistory && barPane != null) {
            card.getChildren().addAll(imagePane, barPane, infoPane);
        } else {
            card.getChildren().addAll(imagePane, infoPane);
        }

        // ─────────────────────────────────────────────────────────────────
        // 6. Animation hover — CARD_W → CARD_W_HOVER (smooth 250ms)
        // ─────────────────────────────────────────────────────────────────
        final double    pctFinal = (progress >= 0) ? Math.min(Math.max(progress, 0.0), 1.0) : 0;
        final Region    fillRef  = trackFillRef;
        final AnchorPane barRef  = barPane;

        Timeline expandAnim = new Timeline(
            new KeyFrame(Duration.millis(ANIM_MS),
                new KeyValue(clip.widthProperty(),     CARD_W_HOVER),
                new KeyValue(card.prefWidthProperty(), CARD_W_HOVER),
                new KeyValue(card.maxWidthProperty(),  CARD_W_HOVER)
            )
        );
        expandAnim.setOnFinished(e -> {
            infoPane.setVisible(true);
            infoPane.setManaged(true);
            gradient.setVisible(true);
            if (barRef  != null) barRef.setPrefWidth(CARD_W_HOVER);
            if (fillRef != null) {
                double w = pctFinal * CARD_W_HOVER;
                fillRef.setPrefWidth(w);
                fillRef.setMaxWidth(w);
            }
        });

        Timeline collapseAnim = new Timeline(
            new KeyFrame(Duration.millis(ANIM_MS),
                new KeyValue(clip.widthProperty(),     CARD_W),
                new KeyValue(card.prefWidthProperty(), CARD_W),
                new KeyValue(card.maxWidthProperty(),  CARD_W)
            )
        );
        collapseAnim.setOnFinished(e -> {
            if (barRef  != null) barRef.setPrefWidth(CARD_W);
            if (fillRef != null) {
                double w = pctFinal * CARD_W;
                fillRef.setPrefWidth(w);
                fillRef.setMaxWidth(w);
            }
        });

        card.setOnMouseEntered(e -> {
            collapseAnim.stop();
            if (expandAnim.getStatus() != Animation.Status.RUNNING) {
                expandAnim.playFromStart();
            }
            btnX.setVisible(true);
        });

        card.setOnMouseExited(e -> {
            expandAnim.stop();
            infoPane.setVisible(false);
            infoPane.setManaged(false);
            gradient.setVisible(false);
            btnX.setVisible(false);
            collapseAnim.playFromStart();
        });

        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) return;
            if (isHistory && history != null) {
                ouvrirHistoryDetail(history);
            } else {
                ouvrirDetail(mediaId);
            }
        });

        return card;
    }

    // ─── Helpers UI ──────────────────────────────────────────────────────────

    private ScrollPane transparentScroll(HBox row) {
        ScrollPane sp = new ScrollPane(row);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setFitToHeight(false);
        sp.setStyle("-fx-background:transparent; -fx-background-color:transparent; -fx-border-color:transparent;");
        return sp;
    }

    private HBox sectionHeader(String text) {
        Label label = new Label(text);
        label.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-padding: 24 0 8 0;
        """);
        HBox h = new HBox(label);
        h.setStyle("-fx-padding: 0 40;");
        return h;
    }

    private Button makeXButton() {
        Button btn = new Button(" ✕ ");
        String base = """
            -fx-background-color: rgba(20,20,20,0.85);
            -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;
            -fx-background-radius: 50%;
            -fx-min-width: 24; -fx-min-height: 24;
            -fx-max-width: 24; -fx-max-height: 24;
            -fx-cursor: hand;
            -fx-border-color: rgba(255,255,255,0.4);
            -fx-border-radius: 50%; -fx-border-width: 1;
        """;
        String hov = base.replace("rgba(20,20,20,0.85)", "#e50914")
                         .replace("rgba(255,255,255,0.4)", "#e50914");
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hov));
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }

    private boolean confirmDelete(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dp = alert.getDialogPane();
        dp.setStyle("-fx-background-color: #1c1c1c; -fx-border-color: #3a3a3a; -fx-border-width: 1;");
        Label content = (Label) dp.lookup(".content.label");
        if (content != null)
            content.setStyle("-fx-text-fill: #eeeeee; -fx-font-size: 14px;");

        ButtonType oui = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType non = new ButtonType("Annuler",        ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(oui, non);

        dp.lookupButton(oui).setStyle("""
            -fx-background-color: #e50914; -fx-text-fill: white;
            -fx-font-weight: bold; -fx-background-radius: 4;
            -fx-cursor: hand; -fx-padding: 6 16;
        """);
        dp.lookupButton(non).setStyle("""
            -fx-background-color: #3a3a3a; -fx-text-fill: #ddd;
            -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 6 16;
        """);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == oui;
    }

    private ImageView buildImageView(String url, double w, double h) {
        ImageView iv = new ImageView();
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(false);
        if (url == null || url.isBlank()) return iv;
        try {
            String imgUrl = (url.startsWith("http") || url.startsWith("file:"))
                ? url
                : new File(url).exists() ? new File(url).toURI().toString() : url;
            Image img = new Image(imgUrl, w, h, false, true, true);
            img.errorProperty().addListener((obs, o, err) -> { if (err) iv.setImage(null); });
            iv.setImage(img);
        } catch (Exception ignored) {}
        return iv;
    }

    private String formatTimeLeft(History h) {
        try {
            int s = h.isFilm()
                ? historyService.getRemainingSecondsFilm(h.getId())
                : historyService.getRemainingSecondsSerie(h.getId());
            if (s <= 0) return "Terminé";
            int min = s / 60;
            if (min <= 0) return "Moins d'1 min";
            return min < 60 ? min + " min restantes" : (min / 60) + "h " + (min % 60) + " min restantes";
        } catch (Exception e) { return ""; }
    }

    // ─── Filtres ─────────────────────────────────────────────────────────────

    @FXML
   
    private void applyFilters() {
        if (allFavoris == null) return;
        String type  = filterType.getValue();
        String genre = filterGenre.getValue();
        String sort  = sortCombo.getValue();

        List<Favori> filtered = allFavoris.stream()
            .filter(f -> {
                Film  film  = filmService.findById(f.getMediaId());
                Serie serie = (film == null) ? serieService.findById(f.getMediaId()) : null;

                // Filtre type
                if ("Film".equals(type)  && film  == null) return false;
                if ("Série".equals(type) && serie == null) return false;

                // ✅ Filtre genre depuis la base
                if (genre != null && !genre.equals("Tous")) {
                    boolean hasGenre = false;
                    if (film != null && film.getGenres() != null)
                        hasGenre = film.getGenres().stream()
                            .anyMatch(g -> g.getName().equalsIgnoreCase(genre));
                    if (serie != null && serie.getGenres() != null)
                        hasGenre = serie.getGenres().stream()
                            .anyMatch(g -> g.getName().equalsIgnoreCase(genre));
                    if (!hasGenre) return false;
                }
                return true;
            }).collect(Collectors.toList());

        if ("Titre A→Z".equals(sort))
            filtered.sort((a, b) -> getTitre(a).compareToIgnoreCase(getTitre(b)));
        else if ("Titre Z→A".equals(sort))
            filtered.sort((a, b) -> getTitre(b).compareToIgnoreCase(getTitre(a)));

        pageContainer.getChildren().clear();
        if (allHistory != null && !allHistory.isEmpty()) {
            pageContainer.getChildren().add(sectionHeader("▶  Continuer à regarder"));
            buildHistoryRow(allHistory);
        }
        pageContainer.getChildren().add(sectionHeader("☰  Ma Liste"));
        buildFavorisRow(filtered);
    }

    @FXML private void resetFilters() {
        filterType.setValue("Tous"); filterGenre.setValue("Tous");
        sortCombo.setValue("Ajout récent"); loadPage();
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) { loadPage(); return; }
        if (allFavoris == null) return;
        List<Favori> filtered = allFavoris.stream()
            .filter(f -> getTitre(f).toLowerCase().contains(q))
            .collect(Collectors.toList());
        pageContainer.getChildren().clear();
        pageContainer.getChildren().add(sectionHeader("Résultats pour \"" + q + "\""));
        buildFavorisRow(filtered);
    }

    @FXML private void goHome() { ScreenManager.getInstance().navigateTo(Screen.home); }

    private void ouvrirDetail(int mediaId) {
       
        	Film film = filmService.findById(mediaId);
        	if (film != null) {
        	    MediaViewController ctrl = ScreenManager.getInstance()
        	        .navigateAndGetController(Screen.MediaView);
        	    if (ctrl != null) ctrl.setFilm(film);
        	    return;
        	}
        	Serie serie = serieService.findById(mediaId);
        	if (serie != null) {
        	    MediaViewController ctrl = ScreenManager.getInstance()
        	        .navigateAndGetController(Screen.MediaView);  // ← ou Screen.episodeView selon ton enum
        	    if (ctrl != null) {
        	        ctrl.setSerie(serie);
        	        int saisonId = saisonService.findFirstSeasonIdBySerie(serie.getId());
        	        
        	    }
        	}
        }
        

    private void showEmpty(String message) {
        emptyState.setVisible(true); emptyState.setManaged(true);
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
    private void ouvrirHistoryDetail(History history) {
    	int userId = SessionManager.getInstance().getCurrentUserId();
        if (history == null) return;

        if (history.isFilm()) {
            // ── Film : ouvrir le lecteur film ──────────────────────────────
            Integer filmId = history.getFilmId();
            if (filmId == null) return;
            Film film = filmService.findById(filmId);
            if (film == null) return;
            UniversalPlayerController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.Player);
            if (ctrl != null) {
                ctrl.initFilm(film,userId);
                ctrl.seekToSeconds(history.getTempsArret());
            }
        } else {
            // ── Épisode / Série : ouvrir le lecteur épisode ───────────────
            Integer episodeId = history.getEpisodeId();
            if (episodeId == null) return;
            UniversalPlayerController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.Player);
            if (ctrl != null) {
                ctrl.initEpisode(episodeId,userId);
                ctrl.seekToSeconds(history.getTempsArret());
            }
        }
    }
    @FXML private void onMovies()  { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void onSeries()  { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML private void onMyList()  { /* déjà ici */ }
}