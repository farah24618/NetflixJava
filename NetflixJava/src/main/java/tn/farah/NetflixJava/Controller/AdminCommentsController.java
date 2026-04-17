package tn.farah.NetflixJava.Controller;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Service.CommentaireService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.net.URL;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminCommentsController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private Button refreshButton;
    @FXML private ListView<Commentaire> commentsListView;
    @FXML private Label totalCommentsLabel;
    @FXML private Label flaggedCommentsLabel;
    @FXML private Label hiddenCommentsLabel;
    @FXML private Label authorLabel;
    @FXML private Label mediaLabel;
    @FXML private Label typeLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private Label riskLabel;
    @FXML private TextArea contentArea;
    @FXML private Button flagButton;
    @FXML private Button hideButton;
    @FXML private Button restoreButton;
    @FXML private Button deleteButton;

    // All comments loaded from DB
    private final ObservableList<Commentaire> allComments = FXCollections.observableArrayList();
    // Currently displayed (after filters)
    private final ObservableList<Commentaire> filteredComments = FXCollections.observableArrayList();

    private CommentaireService commentaireService;

    // Bad words list for auto-detection
    private final List<String> badWords = Arrays.asList(
            "idiot", "stupide", "nul", "merde", "sale", "con", "imbécile"
    );

    // ─────────────────────────────────────────────
    // We track "hidden" comments via a local Set
    // because the Commentaire entity uses `signale`
    // for flagged. We reuse spoiler=true as "hidden"
    // OR we simply manage status purely in memory
    // and persist via signale / spoiler fields.
    //
    // Mapping used here:
    //   signale == true  → "SIGNALÉ"
    //   spoiler == true  → "MASQUÉ"  (reused as hidden flag for admin)
    //   both false       → "NORMAL"
    // ─────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = ConxDB.getInstance();
        commentaireService = new CommentaireService(connection);

        initFilters();
        loadComments();
        detectInappropriateComments();
        applyFilters();
        initListView();
        refreshStats();
        animatePage();
    }

    // ── Filters ──────────────────────────────────

    private void initFilters() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "NORMAL", "SIGNALÉ", "MASQUÉ"
        ));
        statusFilterCombo.setValue("Tous");

        // typeFilterCombo kept for future use; Commentaire has no type field yet
        typeFilterCombo.setItems(FXCollections.observableArrayList("Tous"));
        typeFilterCombo.setValue("Tous");
    }

    // ── Load & detect ────────────────────────────

    private void loadComments() {
        allComments.clear();
        List<Commentaire> fromDb = commentaireService.findAll();
        allComments.addAll(fromDb);
    }

    /**
     * Auto-flag comments containing bad words (only in memory for display;
     * persisted when admin explicitly clicks "Flag").
     */
    private void detectInappropriateComments() {
        for (Commentaire c : allComments) {
            String lower = c.getContenu() == null ? "" : c.getContenu().toLowerCase();
            boolean hasBadWord = badWords.stream().anyMatch(lower::contains);
            if (hasBadWord && !c.isSpoiler()) { // not already hidden
                c.setSignale(true);
            }
        }
    }

    // ── ListView ─────────────────────────────────
    @FXML
    private void handleDeleteComment() {
        Commentaire selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Dialog de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce commentaire ?");
        alert.setContentText("Auteur : " + (selected.getUsername() != null ? selected.getUsername() : "Utilisateur #" + selected.getUserId())
                + "\nContenu : " + shorten(selected.getContenu(), 80)
                + "\nStatut : " + resolveStatus(selected));

        // Style sombre pour le dialog
        alert.getDialogPane().setStyle("-fx-background-color: #1a1a1a;");
        alert.getDialogPane().lookup(".content.label")
                .setStyle("-fx-text-fill: white;");

        ButtonType confirmBtn = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn  = new ButtonType("Annuler",   ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == confirmBtn) {
                boolean deleted = commentaireService.delet(selected.getId());
                if (deleted) {
                    allComments.remove(selected);
                    filteredComments.remove(selected);
                    refreshStats();

                    if (!filteredComments.isEmpty()) {
                        commentsListView.getSelectionModel().selectFirst();
                    } else {
                        clearDetails();
                    }
                }
            }
        });
    }

    private void initListView() {
        commentsListView.setItems(filteredComments);

        commentsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Commentaire item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                String status = resolveStatus(item);

                Label author = new Label(item.getUsername() != null ? item.getUsername() : "Utilisateur #" + item.getUserId());
                author.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

                Label media = new Label();
                media.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 13px;");

                Label content = new Label(shorten(item.getContenu(), 70));
                content.setWrapText(true);
                content.setStyle("-fx-text-fill: #e6e6e6; -fx-font-size: 14px;");

                Label badge = new Label(status);
                badge.setStyle(getBadgeStyle(status));

                String dateText = item.getDateCommentaire() != null
                        ? item.getDateCommentaire().toLocalDate().toString()
                        : "—";
                Label date = new Label(dateText);
                date.setStyle("-fx-text-fill: #8d8d8d; -fx-font-size: 12px;");

                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                javafx.scene.layout.HBox topRow = new javafx.scene.layout.HBox(10, author, spacer, badge);
                javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(6, topRow, media, content, date);
                box.setStyle(
                        "-fx-background-color: linear-gradient(to right, #1a1a1a, #131313);" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 14;" +
                        "-fx-border-color: #292929;" +
                        "-fx-border-radius: 16;"
                );

                setGraphic(box);
                setStyle("-fx-background-color: transparent; -fx-padding: 6;");
            }
        });

        commentsListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, selected) -> {
                    if (selected != null) showCommentDetails(selected);
                });

        if (!filteredComments.isEmpty()) {
            commentsListView.getSelectionModel().selectFirst();
        }
    }

    // ── Status helpers ────────────────────────────

    /**
     * Derives a display status string from the Commentaire entity fields.
     * spoiler == true  → MASQUÉ (admin hidden)
     * signale == true  → SIGNALÉ
     * otherwise        → NORMAL
     */
    private String resolveStatus(Commentaire c) {
        if (c.isSpoiler()) return "MASQUÉ";
        if (c.isSignale()) return "SIGNALÉ";
        return "NORMAL";
    }

    private String resolveRisk(Commentaire c) {
        if (c.isSignale() || c.isSpoiler()) return "ÉLEVÉ";
        return "FAIBLE";
    }

    private String getBadgeStyle(String status) {
        return switch (status) {
            case "SIGNALÉ" ->
                    "-fx-background-color: #ffb347; -fx-text-fill: #111111; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 4 10;";
            case "MASQUÉ" ->
                    "-fx-background-color: #E50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 4 10;";
            default ->
                    "-fx-background-color: #2c2c2c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 4 10;";
        };
    }

    // ── Detail panel ──────────────────────────────

    private void showCommentDetails(Commentaire item) {
        String status = resolveStatus(item);
        String risk   = resolveRisk(item);

        authorLabel.setText(item.getUsername() != null ? item.getUsername() : "Utilisateur #" + item.getUserId());
        mediaLabel.setText("Média #" + item.getMediaId());
        typeLabel.setText("—");   // no type field in Commentaire
        dateLabel.setText(item.getDateCommentaire() != null
                ? item.getDateCommentaire().toLocalDate().toString() : "—");
        statusLabel.setText(status);
        riskLabel.setText(risk);
        contentArea.setText(item.getContenu());

        statusLabel.setStyle(switch (status) {
            case "SIGNALÉ" -> "-fx-text-fill: #ffb347; -fx-font-size: 16px; -fx-font-weight: bold;";
            case "MASQUÉ"  -> "-fx-text-fill: #E50914; -fx-font-size: 16px; -fx-font-weight: bold;";
            default        -> "-fx-text-fill: #35d07f; -fx-font-size: 16px; -fx-font-weight: bold;";
        });

        riskLabel.setStyle("ÉLEVÉ".equals(risk)
                ? "-fx-text-fill: #ff4d57; -fx-font-size: 16px; -fx-font-weight: bold;"
                : "-fx-text-fill: #35d07f; -fx-font-size: 16px; -fx-font-weight: bold;");
    }

    private void clearDetails() {
        authorLabel.setText("—");
        mediaLabel.setText("—");
        typeLabel.setText("—");
        dateLabel.setText("—");
        statusLabel.setText("—");
        riskLabel.setText("—");
        contentArea.clear();
    }

    // ── Filters ───────────────────────────────────

    @FXML private void handleSearch() { applyFilters(); }
    @FXML private void handleFilter() { applyFilters(); }

    @FXML
    private void handleRefresh() {
        loadComments();
        detectInappropriateComments();
        applyFilters();
        refreshStats();
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String selectedStatus = statusFilterCombo.getValue();

        List<Commentaire> result = allComments.stream()
                .filter(item -> {
                    if (keyword.isEmpty()) return true;
                    String author  = item.getUsername()  != null ? item.getUsername().toLowerCase()  : "";
                    String content = item.getContenu()   != null ? item.getContenu().toLowerCase()   : "";
                    return author.contains(keyword) || content.contains(keyword);
                })
                .filter(item -> {
                    if ("Tous".equals(selectedStatus)) return true;
                    return resolveStatus(item).equals(selectedStatus);
                })
                .collect(Collectors.toList());

        filteredComments.setAll(result);
        refreshStats();

        if (!filteredComments.isEmpty()) {
            commentsListView.getSelectionModel().selectFirst();
        } else {
            clearDetails();
        }
    }

    private void refreshStats() {
        totalCommentsLabel.setText(String.valueOf(allComments.size()));
        flaggedCommentsLabel.setText(String.valueOf(
                allComments.stream().filter(Commentaire::isSignale).count()
        ));
        hiddenCommentsLabel.setText(String.valueOf(
                allComments.stream().filter(Commentaire::isSpoiler).count()
        ));
    }

    // ── Action buttons ────────────────────────────

    @FXML
    private void handleFlagComment() {
        Commentaire selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setSignale(true);
        commentaireService.update(selected);          // persist to DB
        commentsListView.refresh();
        showCommentDetails(selected);
        refreshStats();
    }

    @FXML
    private void handleHideComment() {
        Commentaire selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // We reuse spoiler=true to mean "admin-hidden / masked"
        selected.setSpoiler(true);
        commentaireService.update(selected);          // persist to DB
        commentsListView.refresh();
        showCommentDetails(selected);
        refreshStats();
    }

    @FXML
    private void handleRestoreComment() {
        Commentaire selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setSignale(false);
        selected.setSpoiler(false);
        commentaireService.update(selected);          // persist to DB
        commentsListView.refresh();
        showCommentDetails(selected);
        refreshStats();
    }

    /*@FXML
    private void handleDeleteComment() {
        Commentaire selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean deleted = commentaireService.delet(selected.getId()); // persist to DB
        if (deleted) {
            allComments.remove(selected);
            filteredComments.remove(selected);
            refreshStats();

            if (!filteredComments.isEmpty()) {
                commentsListView.getSelectionModel().selectFirst();
            } else {
                clearDetails();
            }
        }
    }*/
   

    @FXML
    private void handleBackDashboard() {
        ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    // ── Helpers ───────────────────────────────────

    private void animatePage() {
        FadeTransition fade = new FadeTransition(Duration.millis(700), commentsListView);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private String shorten(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}