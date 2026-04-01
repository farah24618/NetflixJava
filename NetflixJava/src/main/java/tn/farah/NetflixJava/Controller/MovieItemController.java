package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.DAO.FilmDao;
import tn.farah.NetflixJava.utils.DatabaseConnection;
import java.sql.SQLException;

public class MovieItemController {

    @FXML private HBox itemRoot;
    @FXML private ImageView moviePoster;
    @FXML private Label movieTitle;
    @FXML private Label idLabel;

    private Film currentFilm;

    public void setFilmData(Film film) {
        this.currentFilm = film;
        movieTitle.setText(film.getTitre());
        // Formatage de l'ID et de l'année pour le style "Asset QC"
        idLabel.setText(film.getId() + " • Movie • " + film.getDateSortie().getYear());

        if (film.getUrlImageCover() != null && !film.getUrlImageCover().isEmpty()) {
            try {
                moviePoster.setImage(new Image(film.getUrlImageCover(), true));
            } catch (Exception e) {
                System.err.println("Erreur image");
            }
        }
    }

    @FXML
    private void handleDelete() {
        System.out.println("🗑️ Clic sur Supprimer pour : " + currentFilm.getTitre());
        
        // Code de suppression (Optionnel pour tester)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + currentFilm.getTitre() + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    FilmDao dao = new FilmDao(DatabaseConnection.getConnection());
                    dao.delete(currentFilm.getId());
                    // Pour rafraîchir l'affichage sans relancer :
                    itemRoot.setVisible(false); 
                    itemRoot.setManaged(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    
    @FXML
    private void handleShowComments() {
        System.out.println("💬 Ouverture des commentaires pour le film : " + currentFilm.getTitre());
        // Ton code pour ouvrir la vue des commentaires ici
    }
}