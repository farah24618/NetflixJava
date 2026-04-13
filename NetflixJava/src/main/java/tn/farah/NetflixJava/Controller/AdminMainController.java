package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
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

    @FXML private VBox movieListContainer;
    @FXML private TextField searchField;
    @FXML private Label titleCountLabel; // ✅ lié au fx:id dans le FXML

    private FilmDao filmDao;

    @FXML
    public void initialize() {
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            filmDao = new FilmDao(conn);
            System.out.println("✅ Connexion base de données réussie.");

            loadMovies("");

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                loadMovies(newValue);
            });
        } else {
            System.err.println("❌ Erreur : Impossible de se connecter à MySQL.");
        }
    }

    private void loadMovies(String query) {
        try {
            if (movieListContainer == null) return;

            movieListContainer.getChildren().clear();
            List<Film> films;

            if (query == null || query.isEmpty()) {
                films = filmDao.findAll();
            } else {
                films = filmDao.findByTitle(query);
            }

            // ✅ Mise à jour dynamique du nombre de films
            if (titleCountLabel != null) {
                titleCountLabel.setText("My Titles (" + films.size() + ")");
            }

            for (Film f : films) {
                String fxmlPath = "/tn/farah/NetflixJava/admin_movie_item.fxml";
                java.net.URL location = getClass().getResource(fxmlPath);

                if (location == null) {
                    System.err.println("❌ Fichier introuvable : " + fxmlPath);
                    continue;
                }

                FXMLLoader loader = new FXMLLoader(location);
                try {
                    Parent item = loader.load();
                    MovieItemController controller = loader.getController();

                    if (controller != null) {
                        controller.setFilmData(f);
                        movieListContainer.getChildren().add(item);
                    }
                } catch (IOException e) {
                    System.err.println("❌ Erreur chargement item : " + f.getTitre());
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
        }
    }

    // --- NAVIGATION ---

    @FXML
    private void handleNavDashboard() {
        ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    @FXML
    private void handleNavUsers() {
        ScreenManager.getInstance().navigateTo(Screen.manageUsers);
    }

    @FXML
    private void handleNavSeries() {
        System.out.println("📺 Navigation vers Séries");
    }

    @FXML
    private void handleNavNotifications() {
        System.out.println("🔔 Notifications cliquées");
    }

    @FXML
    private void handleNavComments() {
        ScreenManager.getInstance().navigateTo(Screen.episodeComments);
    }

    @FXML
    private void handleDashboard() {
        ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    @FXML
    private void handleAddMovie() {
        ScreenManager.getInstance().navigateTo(Screen.addFilm);
    }

    @FXML
    private void handleLogout() {
        System.exit(0);
    }
}