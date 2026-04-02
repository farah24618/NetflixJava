package tn.farah.NetflixJava.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.Entities.AdminStats;
import tn.farah.NetflixJava.Service.AdminDashboardService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.sql.Connection;

public class AdminDashboardController {

    @FXML
    private Label lblAdminName;

    @FXML
    private Label lblNbFilms;

    @FXML
    private Label lblNbSeries;

    @FXML
    private Label lblNbEpisodes;

    @FXML
    private Label lblNbUsers;

    @FXML
    private Label lblNbComments;

    @FXML
    private Label lblSummary;

    @FXML
    private PieChart contentPieChart;

    @FXML
    private LineChart<String, Number> contentByYearChart;

    @FXML
    private BarChart<String, Number> commentsByTypeChart;

    private final Connection connection = ConxDB.getInstance();
    private final AdminDashboardService adminDashboardService = new AdminDashboardService(connection);

    @FXML
    public void initialize() {
        lblAdminName.setText("Bienvenue Admin");
        chargerStatistiques();
        chargerGraphiquesBase();
    }

    public void chargerStatistiques() {
        AdminStats stats = adminDashboardService.getDashboardStats();

        if (stats != null) {
            lblNbFilms.setText(String.valueOf(stats.getNbFilms()));
            lblNbSeries.setText(String.valueOf(stats.getNbSeries()));
            lblNbEpisodes.setText(String.valueOf(stats.getNbEpisodes()));
            lblNbUsers.setText(String.valueOf(stats.getNbUsers()));
            lblNbComments.setText(String.valueOf(stats.getNbComments()));

            lblSummary.setText(
                    "La plateforme contient " +
                            stats.getNbFilms() + " films, " +
                            stats.getNbSeries() + " séries, " +
                            stats.getNbEpisodes() + " épisodes, " +
                            stats.getNbUsers() + " utilisateurs et " +
                            stats.getNbComments() + " commentaires."
            );
        } else {
            lblNbFilms.setText("0");
            lblNbSeries.setText("0");
            lblNbEpisodes.setText("0");
            lblNbUsers.setText("0");
            lblNbComments.setText("0");
            lblSummary.setText("Impossible de charger les statistiques.");
        }
    }

    private void chargerGraphiquesBase() {
        AdminStats stats = adminDashboardService.getDashboardStats();

        if (stats == null) {
            return;
        }

        // ===== PieChart : répartition du contenu =====
        contentPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Films", stats.getNbFilms()),
                new PieChart.Data("Séries", stats.getNbSeries()),
                new PieChart.Data("Épisodes", stats.getNbEpisodes())
        ));

        // ===== LineChart : exemple évolutif =====
        XYChart.Series<String, Number> contentSeries = new XYChart.Series<>();
        contentSeries.setName("Contenu");

        // Valeurs de démonstration pour commencer
        contentSeries.getData().add(new XYChart.Data<>("2021", 5));
        contentSeries.getData().add(new XYChart.Data<>("2022", 10));
        contentSeries.getData().add(new XYChart.Data<>("2023", 18));
        contentSeries.getData().add(new XYChart.Data<>("2024", 25));
        contentSeries.getData().add(new XYChart.Data<>("2025", stats.getNbFilms() + stats.getNbSeries()));

        contentByYearChart.getData().clear();
        contentByYearChart.getData().add(contentSeries);

        // ===== BarChart : commentaires par type =====
        XYChart.Series<String, Number> commentsSeries = new XYChart.Series<>();
        commentsSeries.setName("Commentaires");

        // Pour l’instant estimation simple
        commentsSeries.getData().add(new XYChart.Data<>("Films", stats.getNbComments() / 3));
        commentsSeries.getData().add(new XYChart.Data<>("Séries", stats.getNbComments() / 3));
        commentsSeries.getData().add(new XYChart.Data<>("Épisodes", stats.getNbComments() - 2 * (stats.getNbComments() / 3)));

        commentsByTypeChart.getData().clear();
        commentsByTypeChart.getData().add(commentsSeries);
    }

    @FXML
    public void goToFilmsAdmin() {
        ScreenManager.getInstance().navigateTo(Screen.films);
    }

    @FXML
    public void goToSeriesAdmin() {
        ScreenManager.getInstance().navigateTo(Screen.series);
    }

    @FXML
    public void goToEpisodesAdmin() {
        System.out.println("Aller vers la page admin épisodes");
    }

    @FXML
    public void goToUsersAdmin() {
        System.out.println("Aller vers la page admin utilisateurs");
    }

    @FXML
    public void goToCommentsAdmin() {
        ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
    }

    @FXML
    public void goToFavoritesAdmin() {
        System.out.println("Aller vers la page admin favoris");
    }

    @FXML
    public void handleLogout() {
        ScreenManager.getInstance().navigateTo(Screen.login);
    }

    public void setAdminName(String adminName) {
        lblAdminName.setText("Bienvenue " + adminName);
    }
}