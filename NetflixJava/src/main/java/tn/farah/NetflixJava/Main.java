package tn.farah.NetflixJava;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            ScreenManager nav = ScreenManager.getInstance();
            
            // 1. Configuration de la fenêtre principale
            nav.init(primaryStage);
            primaryStage.setTitle("RakchaNet");
            primaryStage.setWidth(1280);
            primaryStage.setHeight(720);

            // 2. Authentification et Profils
            nav.register(Screen.login,           "/tn/farah/NetflixJava/login.fxml");
            nav.register(Screen.logup,           "/tn/farah/NetflixJava/logup.fxml");
            nav.register(Screen.oublie,          "/tn/farah/NetflixJava/oublie.fxml");
            nav.register(Screen.ResetPassword,   "/tn/farah/NetflixJava/ResetPassword.fxml");
            nav.register(Screen.pofiles,         "/tn/farah/NetflixJava/profiles.fxml");
            nav.register(Screen.addProfile,      "/tn/farah/NetflixJava/addProfile.fxml");
            nav.register(Screen.ManageProfiles,  "/tn/farah/NetflixJava/ManageProfiles.fxml");

            // 3. Inscription par étapes
            nav.register(Screen.signup1,         "/tn/farah/NetflixJava/signup1.fxml");
            nav.register(Screen.signup2,         "/tn/farah/NetflixJava/signup2.fxml");
            nav.register(Screen.signup3,         "/tn/farah/NetflixJava/signup3.fxml");

            // 4. Interface Client et Navigation
            nav.register(Screen.mainView,        "/tn/farah/NetflixJava/MainView.fxml");
            nav.register(Screen.home,            "/tn/farah/NetflixJava/Home.fxml");
            nav.register(Screen.HomePage2,       "/tn/farah/NetflixJava/HomePage 2.fxml");
            nav.register(Screen.films,           "/tn/farah/NetflixJava/Films.fxml");
            nav.register(Screen.series,          "/tn/farah/NetflixJava/Series.fxml");
            nav.register(Screen.search,          "/tn/farah/NetflixJava/Search.fxml");
            nav.register(Screen.myList,          "/tn/farah/NetflixJava/MyListView.fxml");
            nav.register(Screen.notification,    "/tn/farah/NetflixJava/notification.fxml");

            // 5. Lecture et Détails du contenu
            nav.register(Screen.detail,          "/tn/farah/NetflixJava/EpisodeView2.fxml");
            nav.register(Screen.episodeView,     "/tn/farah/NetflixJava/EpisodeView.fxml");
            nav.register(Screen.detailFilm,      "/tn/farah/NetflixJava/FilmView.fxml");
            nav.register(Screen.detailMovie2,    "/tn/farah/NetflixJava/detailMovie2.fxml");
            nav.register(Screen.DetailMedia,     "/tn/farah/NetflixJava/DetailMedia.fxml");
            nav.register(Screen.episodeComments, "/tn/farah/NetflixJava/Commentaire.fxml");
            nav.register(Screen.video,           "/tn/farah/NetflixJava/video.fxml");
            nav.register(Screen.filmPlayer,      "/tn/farah/NetflixJava/FilmPlayer.fxml");
            
            // 6. Administration (Gestion et Dashboard)
            nav.register(Screen.AdminDashboard,  "/tn/farah/NetflixJava/AdminDashboard.fxml");
            nav.register(Screen.admin_main,      "/tn/farah/NetflixJava/admin_main.fxml");
            nav.register(Screen.ManageSeries,    "/tn/farah/NetflixJava/ManageSeries.fxml");
            nav.register(Screen.manageUsers,     "/tn/farah/NetflixJava/UserManagement.fxml");
            nav.register(Screen.CommentaireAdmin, "/tn/farah/NetflixJava/CommentaireAdmin.fxml");
            nav.register(Screen.notificationAdmin,"/tn/farah/NetflixJava/notificationAdmin.fxml");
            
            // 7. Lancement de l'application
            primaryStage.centerOnScreen();
            nav.navigateTo(Screen.ManageSeries); // Écran de départ par défaut
            
            primaryStage.show();
            System.out.println("✅ Application RakchaNet lancée avec succès.");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation du ScreenManager :");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}