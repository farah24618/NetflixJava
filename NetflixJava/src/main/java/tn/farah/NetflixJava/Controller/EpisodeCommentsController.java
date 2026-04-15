package tn.farah.NetflixJava.Controller;

import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Service.CommentaireService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EpisodeCommentsController {

    @FXML
    private Label episodeTitleLabel;

    @FXML
    private Label seriesTitleLabel;

    @FXML
    private Label episodeDescriptionLabel;

    @FXML
    private ListView<Commentaire> commentsListView;

    @FXML
    private TextArea commentInputArea;

    @FXML
    private CheckBox spoilerCheckBox;

    private final CommentaireService commentaireService = new CommentaireService();

    private int currentMediaId;
    private String currentMediaType;
    private int currentUserId;
    private String currentUsername;

    @FXML
    public void initialize() {
        commentsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Commentaire commentaire, boolean empty) {
                super.updateItem(commentaire, empty);

                if (empty || commentaire == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                VBox container = new VBox(8);
                container.setPadding(new Insets(14));
                container.setStyle("-fx-background-color: #1b1b1b; -fx-background-radius: 10;");

                Label userLabel = new Label(
                        "@" + commentaire.getUsername() + " • " +
                        commentaire.getDateCommentaire().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
                userLabel.setStyle("-fx-text-fill: #cfcfcf; -fx-font-size: 13px;");

                Label contentLabel = new Label();
                contentLabel.setWrapText(true);
                contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");

                Button spoilerButton = new Button("Afficher le spoiler");
                spoilerButton.setStyle("-fx-background-color: #2f2f2f; -fx-text-fill: orange; -fx-background-radius: 8;");

                if (commentaire.isSpoiler()) {
                    contentLabel.setText("⚠ Contenu masqué (spoiler)");
                    contentLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 15px;");
                    spoilerButton.setVisible(true);
                    spoilerButton.setManaged(true);

                    spoilerButton.setOnAction(e -> {
                        contentLabel.setText(commentaire.getContenu());
                        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
                        spoilerButton.setVisible(false);
                        spoilerButton.setManaged(false);
                    });
                } else {
                    contentLabel.setText(commentaire.getContenu());
                    spoilerButton.setVisible(false);
                    spoilerButton.setManaged(false);
                }

                Button likeButton = new Button("❤ " + commentaire.getLikes());
                likeButton.setStyle("-fx-background-color: #2f2f2f; -fx-text-fill: #ff5c5c; -fx-background-radius: 8;");

                likeButton.setOnAction(e -> {
                    commentaireService.ajouterLike(commentaire.getId());
                    loadComments();
                });

                Button replyButton = new Button("Répondre");
                replyButton.setStyle("-fx-background-color: #2f2f2f; -fx-text-fill: white; -fx-background-radius: 8;");

                HBox actionsBar = new HBox(10);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                actionsBar.getChildren().addAll(likeButton, replyButton, spoilerButton, spacer);

                container.getChildren().addAll(userLabel, contentLabel, actionsBar);
                setGraphic(container);
            }
        });
    }

    public void initData(int mediaId, String mediaType, String titre, String description, int userId, String username) {
        this.currentMediaId = mediaId;
        this.currentMediaType = mediaType;
        this.currentUserId = userId;
        this.currentUsername = username;

        episodeTitleLabel.setText(titre);
        seriesTitleLabel.setText(mediaType);
        episodeDescriptionLabel.setText(description != null ? description : "");

        loadComments();
    }

    private void loadComments() {
        if (currentMediaId <= 0 || currentMediaType == null) {
            commentsListView.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Commentaire> commentaires =
                commentaireService.getCommentairesByMedia(currentMediaId, currentMediaType);

        commentsListView.setItems(FXCollections.observableArrayList(commentaires));
    }

    @FXML
    public void handleAddComment() {
        String contenu = commentInputArea.getText();

        if (currentMediaId <= 0 || currentMediaType == null || currentMediaType.trim().isEmpty()) {
            showAlert("Erreur", "Aucun média sélectionné.");
            return;
        }

        if (contenu == null || contenu.trim().isEmpty()) {
            showAlert("Champ vide", "Veuillez écrire un commentaire.");
            return;
        }

        Commentaire commentaire = new Commentaire();
        commentaire.setMediaId(currentMediaId);
        commentaire.setMediaType(currentMediaType);
        commentaire.setUserId(currentUserId);
        commentaire.setUsername(currentUsername != null ? currentUsername : "Utilisateur");
        commentaire.setContenu(contenu.trim());
        commentaire.setSpoiler(spoilerCheckBox.isSelected());

        boolean ok = commentaireService.ajouterCommentaire(commentaire);

        if (ok) {
            commentInputArea.clear();
            spoilerCheckBox.setSelected(false);
            loadComments();
        } else {
            showAlert("Erreur", "Impossible d'enregistrer le commentaire.");
        }
    }

    @FXML
    public void goBack() {
        if (ScreenManager.getInstance().canGoBack()) {
            ScreenManager.getInstance().goBack();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
