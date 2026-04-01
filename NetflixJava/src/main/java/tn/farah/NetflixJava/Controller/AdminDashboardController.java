package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class AdminDashboardController {

    @FXML
    private Label lblAdminName;

    @FXML
    private Label lblNbFilms;

    @FXML
    private Label lblNbSeries;

    @FXML
    private Label lblNbUsers;

    @FXML
    private Label lblNbComments;

    @FXML
    public void initialize() {
        lblAdminName.setText("Bienvenue Admin");

        // Valeurs de test pour commencer
        lblNbFilms.setText("10");
        lblNbSeries.setText("10");
        lblNbUsers.setText("25");
        lblNbComments.setText("120");
    }

    public void chargerStatistiques(int nbFilms, int nbSeries, int nbUsers, int nbComments) {
        lblNbFilms.setText(String.valueOf(nbFilms));
        lblNbSeries.setText(String.valueOf(nbSeries));
        lblNbUsers.setText(String.valueOf(nbUsers));
        lblNbComments.setText(String.valueOf(nbComments));
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
        System.out.println("Aller vers page épisodes admin");
    }

    @FXML
    public void goToUsersAdmin() {
        System.out.println("Aller vers page utilisateurs admin");
    }

    @FXML
    public void goToCommentsAdmin() {
        System.out.println("Aller vers page commentaires admin");
    }

    @FXML
    public void handleLogout() {
        ScreenManager.getInstance().navigateTo(Screen.login);
    }
}