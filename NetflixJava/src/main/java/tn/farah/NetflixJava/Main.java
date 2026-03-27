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
        nav.register(Screen.search,        "/tn/farah/NetflixJava/SearchResults.fxml");

        
        // 3. Show the first screen
        primaryStage.setTitle("RekchaNet");
        nav.navigateTo(Screen.home);
        primaryStage.setWidth(1280);   // ← largeur que tu veux
        primaryStage.setHeight(720);   // ← hauteur que tu veux
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}