package tn.farah.NetflixJava.Controller;


import java.net.URL;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class MainViewController implements Initializable {

    
    @FXML private Button signInButton;

    @FXML private TextField heroEmailField;      

    @FXML private Button heroGetStartedButton;   

    @FXML private TextField faqEmailField;       

    @FXML private Button faqGetStartedButton; 
    @FXML private Region heroBackground;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        signInButton.setOnAction(e -> handleSignIn());
        heroGetStartedButton.setOnAction(e -> handleGetStarted(heroEmailField));
        faqGetStartedButton.setOnAction(e -> handleGetStarted(faqEmailField));
  
        String imagePath = "/tn/farah/NetflixJava/ImagesNet/hero.jpeg";
        URL res = getClass().getResource(imagePath);

        if (res != null) {
            String style = "-fx-background-image: url('" + res.toExternalForm() + "'); " +
                           "-fx-background-size: cover; " +
                           "-fx-background-position: center;";
            
            heroBackground.setStyle(style);
           ;
        }
    }

   
    @FXML
    private void handleSignIn() {
        ScreenManager.getInstance().navigateAndReplace(Screen.signup1);
    }

   
    @FXML
    private void handleHeroGetStarted() {
        handleGetStarted(heroEmailField);
    }

    @FXML
    private void handleFaqGetStarted() {
        handleGetStarted(faqEmailField);
    }

    private void handleGetStarted(TextField emailField) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Adresse e-mail requise", "Veuillez saisir votre adresse e-mail pour continuer.");
            emailField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            showError("Adresse e-mail invalide", "Veuillez saisir une adresse e-mail valide.");
            emailField.requestFocus();
            return;
        }

        LoginController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.login);
        if (ctrl != null) {
            ctrl.prefillEmail(email);
        }
    }

    
    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
