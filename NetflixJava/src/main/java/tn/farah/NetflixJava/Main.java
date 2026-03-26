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
        // 3. Show the first screen
        primaryStage.setTitle("RekchaNet");
        nav.navigateTo(Screen.login);
    }

    public static void main(String[] args) {
        launch(args);
    }
}