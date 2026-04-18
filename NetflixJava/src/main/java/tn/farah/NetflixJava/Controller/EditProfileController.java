package tn.farah.NetflixJava.Controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;


public class EditProfileController implements Initializable {

    @FXML private TextField pseudoField;
    @FXML private Label     pseudoErrorLabel;   // red validation message
    @FXML private Label     statusLabel;        // green success message
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button    saveBtn;
    @FXML private Button    logoutBtn;

    private UserService userService;
    private Connection  connection;

    private User currentUser;


    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        connection  = ConxDB.getInstance();
        userService = new UserService(connection);

       
        languageCombo.setItems(FXCollections.observableArrayList(
                "Français"));

        int userId = SessionManager.getInstance().getCurrentUserId();
        currentUser = userService.findUserById(userId);

        if (currentUser != null) {
            pseudoField.setText(currentUser.getPseudo() != null
                    ? currentUser.getPseudo() : "");
        }

        pseudoField.textProperty().addListener((obs, oldVal, newVal) -> {
            hideError();
            hideStatus();
        });
    }

    @FXML
    private void onSave() {
        hideError();
        hideStatus();

        final String newPseudo = pseudoField.getText() == null
                ? "" : pseudoField.getText().trim();

     
        if (newPseudo.isEmpty()) {
            showError("Le pseudo ne peut pas être vide.");
            return;
        }

        if (newPseudo.length() < 3) {
            showError("Le pseudo doit contenir au moins 3 caractères.");
            return;
        }

        if (!newPseudo.equals(currentUser.getPseudo())) {
            boolean pseudoTaken = userService.isPseudoTaken(newPseudo, currentUser.getId());
            if (pseudoTaken) {
                showError("Ce pseudo est déjà utilisé. Choisissez-en un autre.");
                pseudoField.requestFocus();
                return;
            }
        }

        currentUser.setPseudo(newPseudo);
        boolean updated = userService.updatePseudo(currentUser.getId(), newPseudo);

        if (updated) {
           
            showStatus("✔  Profil mis à jour avec succès !");
                        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> goHome());
            delay.play();
        } else {
            showError("Erreur lors de la mise à jour. Réessayez.");
        }
    }

    @FXML
    private void onCancel() {
        goHome();
    }
    @FXML
    private void onDeleteProfile() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le profil");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer votre profil ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userService.deleteUser(currentUser.getId());
                logout();
            }
        });
    }

   
    @FXML
    private void onLogout() {
        logout();
    }

    private void logout() {
        
        ScreenManager.getInstance().navigateTo(Screen.mainView);
    }


    private void goHome() {
        ScreenManager.getInstance().goBack();
    }

    
    private void showError(final String msg) {
        pseudoErrorLabel.setText(msg);
        pseudoErrorLabel.setVisible(true);
        pseudoErrorLabel.setManaged(true);

        javafx.animation.TranslateTransition shake =
                new javafx.animation.TranslateTransition(Duration.millis(60), pseudoField);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void hideError() {
        pseudoErrorLabel.setVisible(false);
        pseudoErrorLabel.setManaged(false);
        pseudoErrorLabel.setText("");
    }

    private void showStatus(final String msg) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.millis(400), statusLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        statusLabel.setText("");
    }
    
    @FXML
    private void ModifierMotDePasse() {
    		ResetPasswordController.userEmail = currentUser.getEmail(); 
              ScreenManager.getInstance().navigateTo(Screen.ResetPassword);
    }
}
