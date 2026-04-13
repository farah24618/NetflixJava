package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Service.CommentaireService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.sql.SQLException;

public class MovieItemController {

    @FXML private HBox itemRoot;
    @FXML private ImageView moviePoster;
    @FXML private Label movieTitle;
    @FXML private Label idLabel;
    @FXML private Label viewsLabel;      // ✅ compteur de vues
    @FXML private Label commentsLabel;   // ✅ compteur de commentaires

    private Film currentFilm;

    public void setFilmData(Film film) {
        this.currentFilm = film;

        movieTitle.setText(film.getTitre());
        idLabel.setText(film.getId() + " • Movie • " + film.getDateSortie().getYear());

        // ✅ Nombre de vues (déjà dans l'objet Film via nbre_vues en DB)
        viewsLabel.setText(String.valueOf(film.getNbreVue()));

        // ✅ Nombre de commentaires (chargé depuis la DB)
        try {
            CommentaireService commentaireService = new CommentaireService(ConxDB.getInstance());
            int nbComments = commentaireService
                                .getCommentairesByMedia(film.getId(), "film")
                                .size();
            commentsLabel.setText(String.valueOf(nbComments));
        } catch (Exception e) {
            commentsLabel.setText("0");
            System.err.println("Erreur chargement commentaires : " + e.getMessage());
        }

        // ✅ Image de couverture
        if (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty()) {
            try {
                moviePoster.setImage(new Image(film.getUrlImageCover(), true));
            } catch (Exception e) {
                System.err.println("Erreur image : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + currentFilm.getTitre() + " ?",
                ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    FilmService service = new FilmService(ConxDB.getInstance());
                    service.delete(currentFilm.getId());
                    itemRoot.setVisible(false);
                    itemRoot.setManaged(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleUpdate() {
        ScreenManager sm = ScreenManager.getInstance();
        sm.setEditingFilm(currentFilm);
        sm.navigateTo(Screen.addFilm);
    }

    @FXML
    private void handleShowComments() {
        System.out.println("💬 Ouverture des commentaires pour : " + currentFilm.getTitre());
        // Ton code pour ouvrir la vue des commentaires ici
    }
}