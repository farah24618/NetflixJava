package tn.farah.NetflixJava;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // CORRECTION ICI : On met le bon chemin vers votre package View !
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/tn/farah/NetflixJava/View/home.fxml") 
        );
        
        // Si votre fichier s'appelle encore login.fxml, mettez plutôt :
        // getClass().getResource("/tn/farah/NetflixJava/View/login.fxml")
        
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setTitle("StreamVibe - Accueil");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}