package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.Service.AdminDashboardService;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.net.URL;
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
    @FXML private LineChart<String, Number> inscriptionsChart;
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

    // --- GRAPHIQUE DES INSCRIPTIONS (Utilise le Service) ---
    private void chargerGraphiqueInscriptions() {
        inscriptionsChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nouveaux inscrits");

        // Appel propre au Service ! Plus de SQL ici.
        Map<String, Integer> inscriptionsData = dashboardService.getInscriptionsData();

        for (Map.Entry<String, Integer> entry : inscriptionsData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        inscriptionsChart.getData().add(series);
    }

    // ==========================================
    // =          NAVIGATION METHODS            =
    // ==========================================

    @FXML
    void goToFilmsAdmin(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
    }

    @FXML
    void goToSeriesAdmin(ActionEvent event) {
        // ScreenManager.getInstance().navigateTo(Screen.);
    }

    @FXML
    void goToCommentsAdmin(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
    }

    @FXML 
    void onNotificationsClicked(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.notificationAdmin);
    }

    @FXML
    void goToUsersAdmin(ActionEvent event) {
        System.out.println("Page Utilisateurs non définie dans l'enum Screen pour le moment");
    }

    @FXML 
    void onSettingsClicked(ActionEvent event) { 
        System.out.println("Aller aux Paramètres");
        ScreenManager.getInstance().navigateTo(Screen.parametresAdmin);
    }
    
    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("Déconnexion en cours...");
        ScreenManager.getInstance().navigateAndReplace(Screen.login);
    }
}