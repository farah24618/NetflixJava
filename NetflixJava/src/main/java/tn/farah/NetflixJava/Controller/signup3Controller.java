package tn.farah.NetflixJava.Controller;
<<<<<<< HEAD

import java.net.URL;
import java.util.ResourceBundle;
=======
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.io.IOException;

import org.w3c.dom.Node;
>>>>>>> branch 'master' of https://github.com/farah24618/NetflixJava.git

import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import javafx.event.ActionEvent;
=======
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class signup3Controller {
	    // Identifiants FXML pour les Labels (Prix et Nom du forfait)
	    @FXML private Label forfaitLabel;
	    @FXML private Label prixLabel;

	    // Identifiants pour les champs de saisie (si tu veux sauvegarder les données plus tard)
	    @FXML private TextField numeroCarteField;
	    @FXML private TextField expirationField;
	    @FXML private TextField cvvField;
	    @FXML private TextField nomCarteField;

	    /**
	     * Cette méthode est appelée depuis le signup2Controller
	     * pour transmettre les données du forfait sélectionné.
	     */
	    public void setForfaitDetails(String nom, String prix) {
	        if (forfaitLabel != null && prixLabel != null) {
	            forfaitLabel.setText(nom);
	            prixLabel.setText(prix);
	        }
	    }
>>>>>>> branch 'master' of https://github.com/farah24618/NetflixJava.git
//...
public class signup3Controller implements Initializable {
	//...
		//....
    // ── Header / Navigation ──────────────────────────────────────────────
    @FXML private Button retourButton;

<<<<<<< HEAD
    // ── Paiement / Carte ────────────────────────────────────────────────
    @FXML private TextField numeroCarteField;
    @FXML private TextField dateExpirationField;
    @FXML private TextField cvvField;
    @FXML private TextField nomCarteField;
=======
	    /**
	     * Action du bouton "COMMENCER MON ABONNEMENT"
	     * Dirige vers l'interface des profils
	     */
	    @FXML
	    void handleCommencerAbonnement(ActionEvent event) {
	        // Logique de validation ou de sauvegarde SQL ici si nécessaire
	        System.out.println("Abonnement validé pour le forfait : " + forfaitLabel.getText());

	        changerScene(event, "/tn/farah/NetflixJava/profiles.fxml", "Qui regarde Netflix ?");
	    }
>>>>>>> branch 'master' of https://github.com/farah24618/NetflixJava.git

<<<<<<< HEAD
    @FXML private Label forfaitLabel;
    @FXML private Label prixLabel;

    @FXML private Button commencerAbonnementButton;

    // ── Boxes pour styles ou clics supplémentaires (si nécessaire) ───────
    @FXML private VBox paiementVBox;
    @FXML private HBox headerBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Exemple : initialisation des labels forfait/prix depuis une étape précédente
        forfaitLabel.setText("Standard");
        prixLabel.setText("45,86 DT");

        // Gestion Enter pour valider le paiement rapidement
        numeroCarteField.setOnKeyPressed(this::handleEnterKey);
        dateExpirationField.setOnKeyPressed(this::handleEnterKey);
        cvvField.setOnKeyPressed(this::handleEnterKey);
        nomCarteField.setOnKeyPressed(this::handleEnterKey);
    }

    // ── Bouton Retour ─────────────────────────────────────────────────────
    @FXML
    private void handleRetour(ActionEvent event) {
        // Exemple : retour à l'étape précédente
        ScreenManager.getInstance().navigateTo(Screen.signup2);
    }

    // ── Commencer l'abonnement ───────────────────────────────────────────
    @FXML
    private void handleCommencerAbonnement(ActionEvent event) {
        String numero   = numeroCarteField.getText().trim();
        String date     = dateExpirationField.getText().trim();
        String cvv      = cvvField.getText().trim();
        String nomCarte = nomCarteField.getText().trim();

        // Validation basique
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

        // Ici tu pourrais appeler ton service de paiement ou enregistrer les infos
        showAlert("Abonnement", "Paiement effectué avec succès !");

        // Par exemple : naviguer vers l'écran de profils
        ScreenManager.getInstance().navigateTo(Screen.pofiles);
    }

    // ── Enter pour valider ────────────────────────────────────────────────
    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            commencerAbonnementButton.fire();
        }
    }

    // ── Affichage simple d'alerte ────────────────────────────────────────
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
=======
	    /**
	     * Méthode utilitaire privée pour changer de scène
	     */
	    private void changerScene(ActionEvent event, String fxmlPath, String titre) {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
	            Parent root = loader.load();

	            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	            stage.setTitle(titre);
	            stage.setScene(new Scene(root));
	            stage.show();

	        } catch (IOException e) {
	            System.err.println("Impossible de charger la vue : " + fxmlPath);
	            e.printStackTrace();
	        }
	    }
	}




>>>>>>> branch 'master' of https://github.com/farah24618/NetflixJava.git
