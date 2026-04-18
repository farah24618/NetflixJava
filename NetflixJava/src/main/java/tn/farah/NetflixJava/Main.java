package tn.farah.NetflixJava;
import javafx.application.Application;

import javafx.stage.Stage;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
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
            nav.register(Screen.films,           "/tn/farah/NetflixJava/Films.fxml");
            nav.register(Screen.series,          "/tn/farah/NetflixJava/Series.fxml");
            nav.register(Screen.search,          "/tn/farah/NetflixJava/Search.fxml");
            nav.register(Screen.myList,          "/tn/farah/NetflixJava/MyListView.fxml");
            nav.register(Screen.notification,    "/tn/farah/NetflixJava/notification.fxml");


            // 5. Lecture et Détails du contenu
            nav.register(Screen.detail,          "/tn/farah/NetflixJava/EpisodeView2.fxml");
          
            nav.register(Screen.detailFilm,      "/tn/farah/NetflixJava/FilmView.fxml");
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
            //nav.navigateTo(Screen.ManageSeries); // Écran de départ par défaut
            
            primaryStage.show();
            System.out.println("✅ Application RakchaNet lancée avec succès.");
nav.register(Screen.notification,  "/tn/farah/NetflixJava/notification.fxml");
        nav.register(Screen.video,  "/tn/farah/NetflixJava/video.fxml");

        nav.register(Screen.detailFilm,        "/tn/farah/NetflixJava/FilmView.fxml");

        nav.register(Screen.oublie,  "/tn/farah/NetflixJava/oublie.fxml");

        nav.register(Screen.ResetPassword,  "/tn/farah/NetflixJava/ResetPassword.fxml");

        nav.register(Screen.CommentaireAdmin,  "/tn/farah/NetflixJava/CommentaireAdmin.fxml");

        nav.register(Screen.filmPlayer,  "/tn/farah/NetflixJava/FilmPlayer.fxml");
        nav.register(Screen.find,  "/tn/farah/NetflixJava/SearchResults.fxml");

      
 
        nav.register(Screen.admin_main, "/tn/farah/NetflixJava/admin_main.fxml");
        nav.register(Screen.editProfiles, "/tn/farah/NetflixJava/ManageProfiles.fxml");
        nav.register(Screen.addFilm, "/tn/farah/NetflixJava/AddFilmView.fxml");

        nav.register(Screen.parametresAdmin, "/tn/farah/NetflixJava/parametres.fxml");
        nav.register(Screen.addEpisode, "/tn/farah/NetflixJava/addEpisode.fxml");
        nav.register(Screen.addSerie, "/tn/farah/NetflixJava/addSerie.fxml");
        nav.register(Screen.Player, "/tn/farah/NetflixJava/UniversalPlayer.fxml");
        nav.register(Screen.addSaison, "/tn/farah/NetflixJava/addSaisonn.fxml");
        nav.register(Screen.ajouterAdmin, "/tn/farah/NetflixJava/ajouterAdmin.fxml");
        





        primaryStage.setTitle("RakchaNet");
     
        //nav.navigateTo(Screen.addProfile);
        //nav.navigateTo(Screen.episodeComments);


        //nav.navigateTo(Screen.login);


     //  nav.navigateTo(Screen.admin_main);
      // nav.navigateTo(Screen.addEpisode);

       


       //nav.navigateTo(Screen.parametresAdmin);


      //nav.navigateTo(Screen.admin_main);

     // nav.navigateTo(Screen.admin_main);


       nav.navigateTo(Screen.addSerie);


   

        primaryStage.setWidth(1280);   // ← largeur que tu veux
        primaryStage.setHeight(720);   // ← hauteur que tu veux
        primaryStage.centerOnScreen();

    }

    public static void main(String[] args) {
        launch(args);
    }
}