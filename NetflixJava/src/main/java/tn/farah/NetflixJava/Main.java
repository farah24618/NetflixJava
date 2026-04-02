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
            nav.init(primaryStage);

            // 1. ENREGISTREMENT DE TOUTES LES INTERFACES
            // Authentification & Profils
            nav.register(Screen.login,           "/tn/farah/NetflixJava/login.fxml");
            nav.register(Screen.logup,           "/tn/farah/NetflixJava/logup.fxml");
            nav.register(Screen.oublie,          "/tn/farah/NetflixJava/oublie.fxml");
            nav.register(Screen.ResetPassword,   "/tn/farah/NetflixJava/ResetPassword.fxml");
            nav.register(Screen.pofiles,         "/tn/farah/NetflixJava/profiles.fxml");
            nav.register(Screen.addProfile,      "/tn/farah/NetflixJava/addProfile.fxml");

            // Navigation Client
            nav.register(Screen.mainView,        "/tn/farah/NetflixJava/MainView.fxml");
            nav.register(Screen.home,            "/tn/farah/NetflixJava/Home.fxml");
            nav.register(Screen.films,           "/tn/farah/NetflixJava/Films.fxml");
            nav.register(Screen.series,          "/tn/farah/NetflixJava/Series.fxml");
            nav.register(Screen.search,          "/tn/farah/NetflixJava/Search.fxml");
            nav.register(Screen.myList,          "/tn/farah/NetflixJava/MyListView.fxml");
            nav.register(Screen.notification,    "/tn/farah/NetflixJava/notification.fxml");

            // Inscription (Steps)
            nav.register(Screen.signup1,         "/tn/farah/NetflixJava/signup1.fxml");
            nav.register(Screen.signup2,         "/tn/farah/NetflixJava/signup2.fxml");
            nav.register(Screen.signup3,         "/tn/farah/NetflixJava/signup3.fxml");

            // Contenu & Lecteur
            nav.register(Screen.detail,          "/tn/farah/NetflixJava/EpisodeView2.fxml");
            nav.register(Screen.episodeComments, "/tn/farah/NetflixJava/Commentaire.fxml");
            nav.register(Screen.video,           "/tn/farah/NetflixJava/video.fxml");
            
            // Administration
            nav.register(Screen.admin_main,      "/tn/farah/NetflixJava/admin_main.fxml");
            nav.register(Screen.comments,      "/tn/farah/NetflixJava/comments.fxml");


            // 2. CONFIGURATION DE LA FENÊTRE
            primaryStage.setTitle("RekchaNet - Mode Test");
            primaryStage.setWidth(1280);
            primaryStage.setHeight(720);
            primaryStage.centerOnScreen();

            // 3. CHOIX DE L'INTERFACE À TESTER
            // Décommente la ligne que tu veux tester et commente les autres :
            
           // nav.navigateTo(Screen.admin_main);      // Test Liste des films (Admin)
            // nav.navigateTo(Screen.video);        // Test Lecteur Vidéo
            // nav.navigateTo(Screen.login);        // Test Login
             nav.navigateTo(Screen.comments); // Test Commentaires

            primaryStage.show();
            System.out.println("✅ Application lancée sur l'écran : " + Screen.admin_main);

        } catch (Exception e) {
            System.err.println("❌ Erreur critique lors de l'enregistrement des écrans :");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}