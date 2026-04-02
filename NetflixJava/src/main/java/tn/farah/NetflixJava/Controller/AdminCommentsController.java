package tn.farah.NetflixJava.Controller;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminCommentsController implements Initializable {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private ComboBox<String> typeFilterCombo;

    @FXML
    private Button refreshButton;

    @FXML
    private ListView<CommentItem> commentsListView;

    @FXML
    private Label totalCommentsLabel;

    @FXML
    private Label flaggedCommentsLabel;

    @FXML
    private Label hiddenCommentsLabel;

    @FXML
    private Label authorLabel;

    @FXML
    private Label mediaLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label riskLabel;

    @FXML
    private TextArea contentArea;

    @FXML
    private Button flagButton;

    @FXML
    private Button hideButton;

    @FXML
    private Button restoreButton;

    @FXML
    private Button deleteButton;

    private final ObservableList<CommentItem> allComments = FXCollections.observableArrayList();
    private final ObservableList<CommentItem> filteredComments = FXCollections.observableArrayList();

    private final List<String> badWords = Arrays.asList(
            "idiot", "stupide", "nul", "merde", "sale", "con", "imbécile"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initFilters();
        loadFakeComments();
        detectInappropriateComments();
        refreshStats();
        applyFilters();
        initListView();
        animatePage();
    }

    private void initFilters() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "NORMAL", "SIGNALÉ", "MASQUÉ"
        ));
        statusFilterCombo.setValue("Tous");

        typeFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "Film", "Série", "Épisode"
        ));
        typeFilterCombo.setValue("Tous");
    }

    private void loadFakeComments() {
        allComments.clear();

        allComments.addAll(
                new CommentItem("Yasmine", "The Matrix", "Film", "Excellent film, très captivant.", LocalDate.now().minusDays(2), "NORMAL"),
                new CommentItem("Farah", "Breaking Bad", "Série", "Franchement incroyable.", LocalDate.now().minusDays(1), "NORMAL"),
                new CommentItem("Amine", "Dark", "Série", "Cette série est nulle, scénario idiot.", LocalDate.now().minusDays(5), "NORMAL"),
                new CommentItem("Ali", "Episode 4", "Épisode", "Très bon épisode mais un peu long.", LocalDate.now().minusDays(3), "NORMAL"),
                new CommentItem("Sarra", "Inception", "Film", "C'est de la merde, je déteste.", LocalDate.now().minusDays(4), "NORMAL"),
                new CommentItem("Maha", "Friends", "Série", "Super drôle, j’adore.", LocalDate.now().minusDays(6), "NORMAL"),
                new CommentItem("Nour", "Episode 2", "Épisode", "Acteur nul, épisode stupide.", LocalDate.now().minusDays(7), "NORMAL")
        );
    }

    private void detectInappropriateComments() {
        for (CommentItem item : allComments) {
            String lower = item.getContent().toLowerCase();
            boolean detected = badWords.stream().anyMatch(lower::contains);

            if (detected && !"MASQUÉ".equals(item.getStatus())) {
                item.setStatus("SIGNALÉ");
                item.setRiskLevel("ÉLEVÉ");
            } else {
                item.setRiskLevel("FAIBLE");
            }
        }
    }

    private void initListView() {
        commentsListView.setItems(filteredComments);

        commentsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(CommentItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    Label author = new Label(item.getAuthor());
                    author.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

                    Label media = new Label(item.getMediaTitle() + " • " + item.getType());
                    media.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 13px;");

                    Label content = new Label(shorten(item.getContent(), 70));
                    content.setWrapText(true);
                    content.setStyle("-fx-text-fill: #e6e6e6; -fx-font-size: 14px;");

                    Label badge = new Label(item.getStatus());
                    badge.setStyle(getBadgeStyle(item.getStatus()));

                    Label date = new Label(item.getDate().toString());
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
            }
        });

        commentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                showCommentDetails(selected);
            }
        });

        if (!filteredComments.isEmpty()) {
            commentsListView.getSelectionModel().selectFirst();
        }
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

    private void showCommentDetails(CommentItem item) {
        authorLabel.setText(item.getAuthor());
        mediaLabel.setText(item.getMediaTitle());
        typeLabel.setText(item.getType());
        dateLabel.setText(item.getDate().toString());
        statusLabel.setText(item.getStatus());
        riskLabel.setText(item.getRiskLevel());
        contentArea.setText(item.getContent());

        if ("SIGNALÉ".equals(item.getStatus())) {
            statusLabel.setStyle("-fx-text-fill: #ffb347; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else if ("MASQUÉ".equals(item.getStatus())) {
            statusLabel.setStyle("-fx-text-fill: #E50914; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #35d07f; -fx-font-size: 16px; -fx-font-weight: bold;");
        }

        if ("ÉLEVÉ".equals(item.getRiskLevel())) {
            riskLabel.setStyle("-fx-text-fill: #ff4d57; -fx-font-size: 16px; -fx-font-weight: bold;");
        } else {
            riskLabel.setStyle("-fx-text-fill: #35d07f; -fx-font-size: 16px; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleFilter() {
        applyFilters();
    }

    @FXML
    private void handleRefresh() {
        detectInappropriateComments();
        applyFilters();
        refreshStats();
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String selectedStatus = statusFilterCombo.getValue();
        String selectedType = typeFilterCombo.getValue();

        List<CommentItem> result = allComments.stream()
                .filter(item -> keyword.isEmpty()
                        || item.getAuthor().toLowerCase().contains(keyword)
                        || item.getContent().toLowerCase().contains(keyword)
                        || item.getMediaTitle().toLowerCase().contains(keyword))
                .filter(item -> "Tous".equals(selectedStatus) || item.getStatus().equals(selectedStatus))
                .filter(item -> "Tous".equals(selectedType) || item.getType().equals(selectedType))
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
                allComments.stream().filter(c -> "SIGNALÉ".equals(c.getStatus())).count()
        ));
        hiddenCommentsLabel.setText(String.valueOf(
                allComments.stream().filter(c -> "MASQUÉ".equals(c.getStatus())).count()
        ));
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

    @FXML
    private void handleFlagComment() {
        CommentItem selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setStatus("SIGNALÉ");
        selected.setRiskLevel("ÉLEVÉ");
        commentsListView.refresh();
        showCommentDetails(selected);
        refreshStats();
    }

    @FXML
    private void handleHideComment() {
        CommentItem selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setStatus("MASQUÉ");
        commentsListView.refresh();
        showCommentDetails(selected);
        refreshStats();
    }

    @FXML
    private void handleRestoreComment() {
        CommentItem selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selected.setStatus("NORMAL");
        selected.setRiskLevel("FAIBLE");
        commentsListView.refresh();
        showCommentDetails(selected);
        refreshStats();
    }

    @FXML
    private void handleDeleteComment() {
        CommentItem selected = commentsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        allComments.remove(selected);
        filteredComments.remove(selected);
        refreshStats();

        if (!filteredComments.isEmpty()) {
            commentsListView.getSelectionModel().selectFirst();
        } else {
            clearDetails();
        }
    }

    @FXML
    private void handleBackDashboard() {
    	ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    private void animatePage() {
        FadeTransition fade = new FadeTransition(Duration.millis(700), commentsListView);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private String shorten(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max) + "...";
    }

    public static class CommentItem {
        private String author;
        private String mediaTitle;
        private String type;
        private String content;
        private LocalDate date;
        private String status;
        private String riskLevel;

        public CommentItem(String author, String mediaTitle, String type, String content, LocalDate date, String status) {
            this.author = author;
            this.mediaTitle = mediaTitle;
            this.type = type;
            this.content = content;
            this.date = date;
            this.status = status;
            this.riskLevel = "FAIBLE";
        }

        public String getAuthor() {
            return author;
        }

        public String getMediaTitle() {
            return mediaTitle;
        }

        public String getType() {
            return type;
        }

        public String getContent() {
            return content;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }
    }
}