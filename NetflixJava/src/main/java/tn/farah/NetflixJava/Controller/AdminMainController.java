package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.DAO.FilmDao;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.utils.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdminMainController {

    @FXML private VBox movieListContainer;
    @FXML private TextField searchField;
    
    private FilmDao filmDao;

    @FXML
    public void initialize() {
        // Récupération de la connexion via ta méthode static
        Connection conn = DatabaseConnection.getConnection();
        
        if (conn != null) {
            filmDao = new FilmDao(conn);
            System.out.println("✅ Connexion base de données réussie.");
            
            // Premier chargement
            loadMovies(""); 

            // Recherche dynamique
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                loadMovies(newValue);
            });
        } else {
            System.out.println("❌ Erreur : Impossible de se connecter à MySQL.");
        }
    }

    private void loadMovies(String query) {
        try {
            movieListContainer.getChildren().clear();
            List<Film> movies;
            
            if (query == null || query.isEmpty()) {
                movies = filmDao.findAll();
            } else {
                movies = filmDao.findByTitle(query);
            }

            System.out.println("🎬 Tentative d'affichage de " + movies.size() + " films.");

            for (Film f : movies) {
                // TEST : On utilise un chemin relatif par rapport à la racine des ressources
                // Vérifie bien que "View" prend une majuscule et que le fichier finit bien par .fxml
            	String fxmlPath = "/tn/farah/NetflixJava/admin_movie_item.fxml";
                java.net.URL location = getClass().getResource(fxmlPath);

                if (location == null) {
                    System.err.println("❌ Erreur : Le fichier FXML est introuvable à l'adresse : " + fxmlPath);
                    return; // On arrête pour éviter le crash "Location is not set"
                }

                FXMLLoader loader = new FXMLLoader(location);
                Parent item = loader.load();
                
                MovieItemController controller = loader.getController();
                controller.setFilmData(f);
                
                movieListContainer.getChildren().add(item);
            }
        } catch (SQLException | IOException e) {
            System.err.println("❌ Erreur lors du rendu de la liste : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleAddMovie() {
        // Cette méthode répare l'erreur de chargement FXML (LoadException)
        System.out.println("Ouverture du formulaire d'ajout...");
    }
}