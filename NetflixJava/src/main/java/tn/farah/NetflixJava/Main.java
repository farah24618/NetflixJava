package tn.farah.NetflixJava;

import javafx.application.Application;



import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class Main extends Application {

	@Override
    public void start(Stage primaryStage) {

        ScreenManager nav = ScreenManager.getInstance();

        // 1. Give the manager the stage (once only)
        nav.init(primaryStage);

        // 2. Register every screen
        nav.register(Screen.login,          "/tn/farah/NetflixJava/login.fxml");
        nav.register(Screen.pofiles,        "/tn/farah/NetflixJava/profiles.fxml");
        nav.register(Screen.logup,        "/tn/farah/NetflixJava/logup.fxml");
        nav.register(Screen.mainView,        "/tn/farah/NetflixJava/MainView.fxml");
        nav.register(Screen.home,        "/tn/farah/NetflixJava/Home.fxml");
        nav.register(Screen.films,        "/tn/farah/NetflixJava/Films.fxml");
        nav.register(Screen.series,        "/tn/farah/NetflixJava/Series.fxml");
        nav.register(Screen.search,        "/tn/farah/NetflixJava/Search.fxml");
        nav.register(Screen.signup1,        "/tn/farah/NetflixJava/signup1.fxml");
        nav.register(Screen.signup2,        "/tn/farah/NetflixJava/signup2.fxml");
        nav.register(Screen.signup3,        "/tn/farah/NetflixJava/signup3.fxml");
        nav.register(Screen.detail,        "/tn/farah/NetflixJava/EpisodeView2.fxml");
        nav.register(Screen.myList,"/tn/farah/NetflixJava/MyListView.fxml");
        nav.register(Screen.addProfile, "/tn/farah/NetflixJava/addProfile.fxml");
        nav.register(Screen.episodeComments, "/tn/farah/NetflixJava/Commentaire.fxml");
        nav.register(Screen.notification,  "/tn/farah/NetflixJava/notification.fxml");
        nav.register(Screen.video,  "/tn/farah/NetflixJava/video.fxml");
 
        nav.register(Screen.oublie,  "/tn/farah/NetflixJava/oublie.fxml");
        nav.register(Screen.ResetPassword,  "/tn/farah/NetflixJava/ResetPassword.fxml");


        primaryStage.setTitle("RekchaNet");
        //nav.navigateTo(Screen.addProfile);
        //nav.navigateTo(Screen.episodeComments);
        


        nav.navigateTo(Screen.video);

        primaryStage.setWidth(1280);   // ← largeur que tu veux
        primaryStage.setHeight(720);   // ← hauteur que tu veux
        primaryStage.centerOnScreen();

    }

    public static void main(String[] args) {
        launch(args);
    }
}