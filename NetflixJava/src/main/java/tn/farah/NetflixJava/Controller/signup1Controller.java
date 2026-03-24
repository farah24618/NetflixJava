package tn.farah.NetflixJava.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
public class signup1Controller {
	

	

	    // Méthode pour le bouton CONTINUER
	    @FXML
	    private void handleContinuer(ActionEvent event) {
	        // Remplace par le chemin exact vers ton fichier
	        changerScene(event, "signup-step2.fxml", "Étape 2 - Création de compte");
	    }
//....
	    //..
	    // Méthode pour le bouton SE CONNECTER (en haut à droite ou en bas)
	    @FXML
	    private void handleSeConnecter(ActionEvent event) {
	        // Remplace par le chemin exact vers ton fichier
	        changerScene(event, "HomePage 2.fxml", "Accueil");
	    }

	    /**
	     * Méthode utilitaire pour changer de vue sans répéter le code
	     */
	    private void changerScene(ActionEvent event, String fxmlFile, String titre) {
	        try {
	        	// Le "/" au début indique qu'on repart de la racine du dossier "resources"
	        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/farah/NetflixJava/" + fxmlFile));
	            Parent root = loader.load();

	            // Récupération de la fenêtre actuelle (Stage)
	            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

	            // Création de la nouvelle scène
	            Scene scene = new Scene(root);
	            
	            stage.setTitle(titre);
	            stage.setScene(scene);
	            stage.show();
	            
	        } catch (IOException e) {
	            System.err.println("Erreur lors du chargement de la vue : " + fxmlFile);
	            e.printStackTrace();
	        }
	    }
	}

