package tn.farah.NetflixJava.Controller;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.io.IOException;

import org.w3c.dom.Node;

import javafx.fxml.FXML;
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
//...
	    //...
	    /**
	     * Action du bouton "retour" (Rouge en haut à droite)
	     * Retourne à l'étape de sélection du forfait (Step 2)
	     */
	    @FXML
	    void handleRetour(ActionEvent event) {
	        changerScene(event, "/tn/farah/NetflixJava/signup-step2.fxml", "Étape 2 - Choisir un forfait");
	    }

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




