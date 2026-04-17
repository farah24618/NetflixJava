/*package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.farah.NetflixJava.DAO.CommentaireDAO;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.utils.DatabaseConnection;
import tn.farah.NetflixJava.utils.ScreenManager;
import javafx.scene.Node;
import java.sql.Connection;
import java.util.List;

public class CommentListController {

    @FXML private VBox commentsContainer;
    @FXML private Label filmTitleLabel;
    @FXML private Label commentCountLabel;
    @FXML private TextField commentInput;

    private Film currentFilm;
    private CommentaireDAO dao;

    @FXML
    public void initialize() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) dao = new CommentaireDAO(conn);
    }

    public void setFilm(Film film) {
        this.currentFilm = film;
        if (film != null) {
            filmTitleLabel.setText("Comments for " + film.getTitre());
            loadComments();
        }
    }

    private void loadComments() {
        if (dao == null || currentFilm == null) return;
        commentsContainer.getChildren().clear();
        
        List<Commentaire> comments = dao.findByFilmId(currentFilm.getId());
        commentCountLabel.setText("Comments (" + comments.size() + ")");

        for (Commentaire c : comments) {
            Node item = createCommentItem(c);
            if (item != null) commentsContainer.getChildren().add(item);
        }
    }

    private HBox createCommentItem(Commentaire c) {
        try {
            HBox row = new HBox(15);
            row.setAlignment(Pos.TOP_LEFT);
            row.setPadding(new Insets(10, 0, 10, 0));

            // SECURITÉ NOM : Evite le crash si username est vide ou nul
            String name = (c.getUsername() != null && !c.getUsername().isEmpty()) ? c.getUsername() : "User";
            String initial = name.substring(0, 1).toUpperCase();

            // Avatar
            StackPane avatar = new StackPane();
            Circle circle = new Circle(18, Color.web(c.isSpoiler() ? "#E50914" : "#333333"));
            Label lblInitial = new Label(initial);
            lblInitial.setTextFill(Color.WHITE);
            avatar.getChildren().addAll(circle, lblInitial);

            // Texte
            VBox textStack = new VBox(3);
            HBox.setHgrow(textStack, Priority.ALWAYS);
            Label userLabel = new Label(name + " • Now");
            userLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            Label contentLabel = new Label(c.getContenu());
            contentLabel.setStyle("-fx-text-fill: #cccccc;");
            contentLabel.setWrapText(true);
            textStack.getChildren().addAll(userLabel, contentLabel);

            // Delete
            Button delBtn = new Button("🗑");
            delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #E50914; -fx-cursor: hand;");
            delBtn.setOnAction(e -> {
                dao.delete(c.getId());
                loadComments();
            });

            row.getChildren().addAll(avatar, textStack, delBtn);
            return row;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Si un item plante, on ne bloque pas les autres
        }
    }

    @FXML private void handleClose() { ScreenManager.getInstance().goBack(); }
    @FXML private void handlePostComment() { /* Ton code save ici  }
}*/