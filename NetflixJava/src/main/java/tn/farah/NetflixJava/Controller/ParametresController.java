package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;


public class ParametresController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private UserService userService;
    private User currentUser;

    @FXML
    public void initialize() {
        // Initialisation propre
        userService = new UserService(ConxDB.getInstance());
        
        // Simulation : Dans un vrai projet, utilisez une classe SessionManager
        loadUserData(1); 
    }

    private void loadUserData(int userId) {
        currentUser = userService.findUserById(userId);
        if (currentUser != null) {
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
        }
    }

    @FXML
    void onSaveSettings(ActionEvent event) {
        if (currentUser == null) return;

        // Validation simple
        if (nomField.getText().isEmpty() || emailField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", "Le nom et l'email sont obligatoires.");
            return;
        }

        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setEmail(emailField.getText());

        boolean passUpdateSuccess = true;
        String nouveauPass = passwordField.getText();
        
        if (nouveauPass != null && !nouveauPass.trim().isEmpty()) {
            passUpdateSuccess = userService.updatePassword(currentUser.getEmail(), nouveauPass);
        }

        boolean userUpdateSuccess = userService.updateUser(currentUser);

        if (userUpdateSuccess && passUpdateSuccess) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Paramètres mis à jour !");
            passwordField.clear(); // Effacer le champ mdp après succès
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour en base de données.");
        }
    }



    @FXML void onDashboardClicked(ActionEvent event) {
    	// ScreenManager.getInstance().navigateTo(Screen.mainView);
    }
    @FXML void onFilmsClicked(ActionEvent event) {
    	 ScreenManager.getInstance().navigateTo(Screen.admin_main);
    }
    @FXML void onSeriesClicked(ActionEvent event) {
    	 //ScreenManager.getInstance().navigateTo(Screen.series);
    }
    @FXML void onUsersClicked(ActionEvent event) { 
    	// ScreenManager.getInstance().navigateTo(Screen.);
    }
    @FXML void onNotificationsClicked(ActionEvent event) {
    	 ScreenManager.getInstance().navigateTo(Screen.notificationAdmin);
    }
    @FXML void onCommentsClicked(ActionEvent event) {
    	ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
    }
    
    
    @FXML
    void onSettingsClicked(ActionEvent event) {
        // Recharger les données pour annuler les modifications non enregistrées
        loadUserData(currentUser.getId()); 
        passwordField.clear();
    }

    @FXML
    void onLogoutClicked(ActionEvent event) {
        // Idéalement, rediriger vers la page de login au lieu de quitter
        System.exit(0);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}