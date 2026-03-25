package tn.farah.NetflixJava.Controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class signup2Controller {
	
//..
	//...
	

	    private String selectedForfait = "Premium"; // Valeur par défaut
	    private String selectedPrix = "61,16 DT";

	    // Méthodes pour détecter le clic sur les conteneurs (VBox/HBox) des forfaits
	    @FXML
	    void selectPremium(MouseEvent event) {
	        selectedForfait = "Premium";
	        selectedPrix = "61,16 DT";
	        System.out.println("Sélection : " + selectedForfait);
	    }

	    @FXML
	    void selectStandard(MouseEvent event) {
	        selectedForfait = "Standard";
	        selectedPrix = "45,86 DT";
	        System.out.println("Sélection : " + selectedForfait);
	    }

	    @FXML
	    void selectStandardPub(MouseEvent event) {
	        selectedForfait = "Standard avec pub";
	        selectedPrix = "20,36 DT";
	        System.out.println("Sélection : " + selectedForfait);
	    }

	    @FXML
	    void handleContinuer(ActionEvent event) {
	        try {
	            // 1. Charger le FXML de l'étape 3
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/farah/NetflixJava/signup-step3.fxml"));
	            Parent root = loader.load();

	            // 2. Récupérer le contrôleur de la page 3
	            signup3Controller nextController = loader.getController();
	            
	            // 3. Lui envoyer les données sélectionnées
	            nextController.setForfaitDetails(selectedForfait, selectedPrix);

	            // 4. Changer de scène
	            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	            stage.setScene(new Scene(root));
	            stage.show();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    @FXML
	    void handleRetour(ActionEvent event) {
	        changerScene(event, "/tn/farah/NetflixJava/signup-step1.fxml");
	    }

	    private void changerScene(ActionEvent event, String fxmlPath) {
	        try {
	            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
	            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	            stage.setScene(new Scene(root));
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}

