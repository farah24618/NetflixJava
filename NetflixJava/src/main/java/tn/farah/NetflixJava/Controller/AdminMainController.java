package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.DAO.FilmDao;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.utils.DatabaseConnection;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdminMainController {
	@FXML private TableView<Film> tableFilms;

    @FXML private VBox movieListContainer;
    @FXML private TextField searchField;
    
    private FilmDao filmDao;
    @FXML
    private void inspecterCommentaires() {
        // 1. On récupère le film sélectionné dans la table
        Film selectionne = tableFilms.getSelectionModel().getSelectedItem();
        
        if (selectionne != null) {
            // 2. On change d'écran
            ScreenManager.getInstance().navigateTo(Screen.episodeComments);
            
            // 3. On récupère le contrôleur de l'écran des commentaires
            CommentListController controller = (CommentListController) ScreenManager.getInstance().getController();
            
            // 4. On lui passe le film pour charger les bons commentaires
            controller.setFilm(selectionne);
            
            System.out.println("✅ Chargement des commentaires pour : " + selectionne.getTitre());
        } else {
            System.out.println("⚠️ Veuillez sélectionner un film dans la table d'abord !");
        }
    }

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