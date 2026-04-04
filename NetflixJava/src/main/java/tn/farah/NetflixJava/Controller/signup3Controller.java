package tn.farah.NetflixJava.Controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionData;
import javafx.event.ActionEvent;

public class signup3Controller implements Initializable {

    @FXML private Button retourButton;
    @FXML private TextField numeroCarteField;
    @FXML private TextField dateExpirationField;
    @FXML private TextField cvvField;
    @FXML private TextField nomCarteField;
    @FXML private Label forfaitLabel;
    @FXML private Label prixLabel;
    @FXML private Button commencerAbonnementButton;
    @FXML private VBox paiementVBox;
    @FXML private HBox headerBox;

    private UserService userService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService(ConxDB.getInstance());
        
        forfaitLabel.setText(SessionData.getForfaitNom());
        prixLabel.setText(SessionData.getForfaitPrix());

        numeroCarteField.setOnKeyPressed(this::handleEnterKey);
        dateExpirationField.setOnKeyPressed(this::handleEnterKey);
        cvvField.setOnKeyPressed(this::handleEnterKey);
        nomCarteField.setOnKeyPressed(this::handleEnterKey);
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.signup2);
    }

    @FXML
    private void handleCommencerAbonnement(ActionEvent event) {
        String numero   = numeroCarteField.getText().trim();
        String date     = dateExpirationField.getText().trim();
        String cvv      = cvvField.getText().trim();
        String nomCarte = nomCarteField.getText().trim();

        if (numero.isEmpty() || date.isEmpty() || cvv.isEmpty() || nomCarte.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs de paiement.");
            return;
        }

        if (!numero.matches("\\d{16}")) {
            showAlert("Numéro invalide", "Le numéro de carte doit contenir 16 chiffres.");
            return;
        }

        if (!cvv.matches("\\d{3}")) {
            showAlert("CVV invalide", "Le code CVV doit contenir 3 chiffres.");
            return;
        }

        // --- ENREGISTREMENT FINAL EN BASE DE DONNÉES ---
        User currentUser = SessionData.getCurrentUser(); 

        if (currentUser != null) {
            // On valide le statut de paiement dans l'objet
            currentUser.setEstPaye(true);
            
            // On appelle registerUser pour faire l'INSERT en base
            boolean success = userService.registerUser(currentUser);

            if (success) {
                showAlert("Abonnement", "Compte créé et paiement effectué avec succès ! Bienvenue.");
                // Maintenant qu'il est en DB, on peut aller vers les profils
                ScreenManager.getInstance().navigateTo(Screen.addProfile);
            } else {
                showAlert("Erreur DB", "Impossible de créer votre compte. L'email est peut-être déjà utilisé.");
            }
        } else {
            showAlert("Erreur Session", "Données utilisateur perdues. Veuillez recommencer.");
            ScreenManager.getInstance().navigateTo(Screen.signup1);
        }
    }

    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            commencerAbonnementButton.fire();
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}