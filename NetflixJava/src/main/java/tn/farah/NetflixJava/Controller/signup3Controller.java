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
//...
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
        // ✅ Lire le forfait choisi depuis SessionData
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
        // 1. Récupération des données saisies
        String numero   = numeroCarteField.getText().trim();
        String date     = dateExpirationField.getText().trim();
        String cvv      = cvvField.getText().trim();
        String nomCarte = nomCarteField.getText().trim();

        // 2. Validations graphiques
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

        // 3. Mise à jour de l'utilisateur
        User currentUser = SessionData.getCurrentUser(); // On récupère l'user en session

        if (currentUser != null) {
            // Mise à jour en Base de données
        	System.out.println("Tentative d'update pour l'ID : " + currentUser.getId());
        	
            boolean success = userService.updatePaymentStatus(currentUser.getId(), true);

            if (success) {
                // Mise à jour de l'objet localement pour la session actuelle
                currentUser.setEstPaye(true);
                
                showAlert("Abonnement", "Paiement effectué avec succès ! Bienvenue chez Netflix.");
                
                // 4. Navigation vers l'écran suivant
                ScreenManager.getInstance().navigateTo(Screen.pofiles);
            } else {
                showAlert("Erreur DB", "Impossible de mettre à jour votre statut de paiement.");
            }
        } else {
            showAlert("Erreur Session", "Utilisateur non identifié. Veuillez recommencer.");
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