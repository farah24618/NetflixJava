package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionData;

import java.net.URL;
import java.util.ResourceBundle;

public class AddProfileController implements Initializable {

    @FXML
    private TextField nameField; // C'est ton champ "pseudo" dans le FXML

    @FXML
    private CheckBox kidCheckBox;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService(ConxDB.getInstance());

        // 1. On récupère l'utilisateur qui vient de s'inscrire via la Session
        User currentUser = SessionData.getCurrentUser();

        if (currentUser != null && currentUser.getPseudo() != null) {
            // 2. On pré-remplit le champ avec le pseudo de la BD
            nameField.setText(currentUser.getPseudo());
        }
    }

    @FXML
    public void handleConfirmer() {
        String newPseudo = nameField.getText().trim();
        User currentUser = SessionData.getCurrentUser();

        if (currentUser != null && !newPseudo.isEmpty()) {
            currentUser.setPseudo(newPseudo); // On met à jour le pseudo dans l'objet
            
            // On enregistre en BD
            boolean success = userService.updateUser(currentUser); 
            
            if (success) {
                ScreenManager.getInstance().navigateTo(Screen.home);
            } else {
                // Afficher une alerte d'erreur
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}