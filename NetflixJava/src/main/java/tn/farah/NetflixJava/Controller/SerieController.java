package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.DAO.SerieDAO;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.utils.DatabaseConnection;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.sql.Connection;
import java.util.List;

public class SerieController {

    @FXML private VBox seriesListContainer;
    @FXML private TextField searchField;
    @FXML private Label seriesCountLabel;

    private SerieDAO serieDAO;

    @FXML
    public void initialize() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            serieDAO = new SerieDAO(conn);
            
            // Premier chargement des données
            refreshList(""); 

            // Recherche dynamique (appelle findByTitle de ton SerieDAO)
            searchField.textProperty().addListener((obs, old, newValue) -> {
                refreshList(newValue);
            });
        }
    }

    private void refreshList(String query) {
        try {
            if (seriesListContainer == null) return;
            
            seriesListContainer.getChildren().clear();
            List<Serie> seriesList;

            if (query == null || query.isEmpty()) {
                seriesList = serieDAO.findAll();
            } else {
                seriesList = serieDAO.findByTitle(query);
            }

            if (seriesCountLabel != null) {
                seriesCountLabel.setText("Mes Séries (" + seriesList.size() + ")");
            }

            for (Serie s : seriesList) {
                // Charge le fichier FXML de l'item
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/farah/NetflixJava/admin_series_item.fxml"));
                Parent item = loader.load();
                
                // Configure le contrôleur de l'item
                SeriesItemController controller = loader.getController();
                controller.setSeriesData(s);
                
                seriesListContainer.getChildren().add(item);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de la liste : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavDashboard() {
        ScreenManager.getInstance().navigateTo(Screen.dashboard);
    }

    @FXML
    private void handleNavFilms() {
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
    }

    @FXML
    private void handleLogout() {
        System.exit(0);
    }
    
    @FXML private void handleNavUsers() { ScreenManager.getInstance().navigateTo(Screen.manageUsers); }
    @FXML private void handleNavNotifications() { /* Implémenter */ }
    @FXML private void handleNavComments() { ScreenManager.getInstance().navigateTo(Screen.episodeComments); }
}