package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.Service.AdminDashboardService;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.DatabaseConnection; // Import ajouté pour la BD

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // --- Labels ---
    @FXML private Label lblAdminName;
    @FXML private Label lblNbFilms;
    @FXML private Label lblNbSeries;
    @FXML private Label lblNbEpisodes;
    @FXML private Label lblNbUsers;
    @FXML private Label lblNbComments;
    @FXML private Label lblSummary;

    // --- Charts ---
    @FXML private PieChart contentPieChart;
    @FXML private LineChart<String, Number> inscriptionsChart; // <-- L'ancien contentByYearChart a été remplacé
    @FXML private BarChart<String, Number> commentsByTypeChart;

    // Service
    private AdminDashboardService dashboardService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardService = new AdminDashboardService();
        
        // Simuler ou récupérer le nom de l'admin connecté (via SessionManager par ex)
        lblAdminName.setText("Bienvenue, Admin");

        loadStatistics();
        loadCharts();
        
        // Appel de notre nouvelle méthode pour le graphique des inscriptions
        chargerGraphiqueInscriptions(); 
    }

    private void loadStatistics() {
        // Remplir les cartes (Cards) en haut
        int films = dashboardService.getTotalFilms();
        int series = dashboardService.getTotalSeries();
        
        lblNbFilms.setText(String.valueOf(films));
        lblNbSeries.setText(String.valueOf(series));
        lblNbEpisodes.setText(String.valueOf(dashboardService.getTotalEpisodes()));
        lblNbUsers.setText(String.valueOf(dashboardService.getTotalUsers()));
        lblNbComments.setText(String.valueOf(dashboardService.getTotalComments()));

        lblSummary.setText("Actuellement, la plateforme compte " + (films + series) + " contenus et " + dashboardService.getTotalUsers() + " utilisateurs inscrits.");
    }

    private void loadCharts() {
        // 1. Pie Chart (Répartition Films / Séries)
        PieChart.Data sliceFilms = new PieChart.Data("Films", dashboardService.getTotalFilms());
        PieChart.Data sliceSeries = new PieChart.Data("Séries", dashboardService.getTotalSeries());
        contentPieChart.getData().addAll(sliceFilms, sliceSeries);

        // 2. Bar Chart (Commentaires par type)
        XYChart.Series<String, Number> commentSeries = new XYChart.Series<>();
        commentSeries.setName("Commentaires");
        Map<String, Integer> commentData = dashboardService.getCommentsByTypeData();
        for (Map.Entry<String, Integer> entry : commentData.entrySet()) {
            commentSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        commentsByTypeChart.getData().add(commentSeries);
    }

    // --- NOUVELLE MÉTHODE POUR LE GRAPHIQUE DES INSCRIPTIONS ---
    private void chargerGraphiqueInscriptions() {
        inscriptionsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux inscrits");

        // Requête SQL directe
        String query = "SELECT DATE(date_inscription) as jour, COUNT(*) as total " +
                       "FROM user GROUP BY DATE(date_inscription) ORDER BY jour ASC LIMIT 7";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String jour = rs.getString("jour");
                int total = rs.getInt("total");
                series.getData().add(new XYChart.Data<>(jour, total));
            }

            inscriptionsChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du graphique des inscriptions :");
            e.printStackTrace();
        }
    }

    // ==========================================
    // =          NAVIGATION METHODS            =
    // ==========================================

    @FXML
    void goToFilmsAdmin(ActionEvent event) {
        // Utilise VOTRE ScreenManager pour aller vers Films
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
        
        // Note: Vous pouvez aussi utiliser navigateWithSplash(Screen.films) 
        // si la page des films met du temps à charger.
    }

    @FXML
    void goToSeriesAdmin(ActionEvent event) {
        // Utilise VOTRE ScreenManager pour aller vers Séries
       // ScreenManager.getInstance().navigateTo(Screen.);
    }

    @FXML
    void goToCommentsAdmin(ActionEvent event) {
        // Utilise VOTRE ScreenManager pour aller vers Commentaires
        ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
    }

    // --- Les autres boutons pour plus tard ---
    @FXML void onNotificationsClicked(ActionEvent event) {
   	 ScreenManager.getInstance().navigateTo(Screen.notificationAdmin);
   }
    @FXML
    void goToUsersAdmin(ActionEvent event) {
        System.out.println("Page Utilisateurs non définie dans l'enum Screen pour le moment");
    }
    @FXML void onSettingsClicked(ActionEvent event) { 
    	System.out.println("Aller aux Paramètres");
    	ScreenManager.getInstance().navigateTo(Screen.parametresAdmin);
    	}
   
    
    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Déconnexion en cours...");
        // On retourne sur la page de Login et on efface l'historique
        tn.farah.NetflixJava.utils.ScreenManager.getInstance().navigateAndReplace(tn.farah.NetflixJava.utils.Screen.login);
    }
}