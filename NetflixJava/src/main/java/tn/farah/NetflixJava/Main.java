package tn.farah.NetflixJava;
import javafx.application.Application;

import javafx.stage.Stage;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
            ScreenManager nav = ScreenManager.getInstance();
           
            nav.init(primaryStage);
            primaryStage.setTitle("RakchaNet");
            primaryStage.setWidth(1280);
            primaryStage.setHeight(720);

         
          
           
            nav.register(Screen.oublie,          "/tn/farah/NetflixJava/oublie.fxml");
            nav.register(Screen.ResetPassword,   "/tn/farah/NetflixJava/ResetPassword.fxml");
            nav.register(Screen.pofiles,         "/tn/farah/NetflixJava/profiles.fxml");
            nav.register(Screen.addProfile,      "/tn/farah/NetflixJava/addProfile.fxml");
            nav.register(Screen.ManageProfiles,  "/tn/farah/NetflixJava/ManageProfiles.fxml");
            
            nav.register(Screen.signup1,         "/tn/farah/NetflixJava/signup1.fxml");
            nav.register(Screen.signup2,         "/tn/farah/NetflixJava/signup2.fxml");
            nav.register(Screen.signup3,         "/tn/farah/NetflixJava/signup3.fxml");

            nav.register(Screen.mainView,        "/tn/farah/NetflixJava/MainView.fxml");
            nav.register(Screen.home,            "/tn/farah/NetflixJava/Home.fxml");
            nav.register(Screen.films,           "/tn/farah/NetflixJava/Films.fxml");
            nav.register(Screen.series,          "/tn/farah/NetflixJava/Series.fxml");
            nav.register(Screen.search,          "/tn/farah/NetflixJava/Search.fxml");
            nav.register(Screen.myList,          "/tn/farah/NetflixJava/MyListView.fxml");
            nav.register(Screen.notification,    "/tn/farah/NetflixJava/notification.fxml");

            nav.register(Screen.AdminDashboard,  "/tn/farah/NetflixJava/AdminDashboard.fxml");
            nav.register(Screen.admin_main,      "/tn/farah/NetflixJava/admin_main.fxml");
            nav.register(Screen.ManageSeries,    "/tn/farah/NetflixJava/ManageSeries.fxml");
            nav.register(Screen.manageUsers,     "/tn/farah/NetflixJava/UserManagement.fxml");
            nav.register(Screen.CommentaireAdmin, "/tn/farah/NetflixJava/CommentaireAdmin.fxml");
            nav.register(Screen.notificationAdmin,"/tn/farah/NetflixJava/notificationAdmin.fxml");
            
            primaryStage.centerOnScreen();
           
            
            primaryStage.show();
            System.out.println("✅ Application RakchaNet lancée avec succès.");
           nav.register(Screen.notification,  "/tn/farah/NetflixJava/notification.fxml");
      
       

        nav.register(Screen.oublie,  "/tn/farah/NetflixJava/oublie.fxml");

        nav.register(Screen.ResetPassword,  "/tn/farah/NetflixJava/ResetPassword.fxml");

        nav.register(Screen.CommentaireAdmin,  "/tn/farah/NetflixJava/CommentaireAdmin.fxml");

       
        nav.register(Screen.find,  "/tn/farah/NetflixJava/SearchResults.fxml");

      
 
        nav.register(Screen.admin_main, "/tn/farah/NetflixJava/admin_main.fxml");
        nav.register(Screen.editProfiles, "/tn/farah/NetflixJava/ManageProfiles.fxml");
        nav.register(Screen.addFilm, "/tn/farah/NetflixJava/AddFilmView.fxml");

        nav.register(Screen.parametresAdmin, "/tn/farah/NetflixJava/parametres.fxml");
        nav.register(Screen.addEpisode, "/tn/farah/NetflixJava/addEpisode.fxml");
        nav.register(Screen.addSerie, "/tn/farah/NetflixJava/addSerie.fxml");
        nav.register(Screen.Player, "/tn/farah/NetflixJava/UniversalPlayer.fxml");
        nav.register(Screen.addSaison, "/tn/farah/NetflixJava/addSaisonn.fxml");

        nav.register(Screen.MediaView, "/tn/farah/NetflixJava/MediaView.fxml");

        nav.register(Screen.ajouterAdmin, "/tn/farah/NetflixJava/ajouterAdmin.fxml");
        nav.register(Screen.login, "/tn/farah/NetflixJava/login.fxml");

        




        primaryStage.setTitle("RakchaNet");

       nav.navigateTo(Screen.mainView);

   

        primaryStage.setWidth(1280);   
        primaryStage.setHeight(720);   
        primaryStage.centerOnScreen();

    }

    public static void main(String[] args) {
        launch(args);
    }
}