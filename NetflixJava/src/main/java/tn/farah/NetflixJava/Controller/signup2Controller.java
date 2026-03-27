package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionData;
import java.net.URL;
import java.util.ResourceBundle;

public class signup2Controller implements Initializable {

    @FXML private HBox premiumBox;
    @FXML private HBox standardBox;
    @FXML private HBox pubBox;
    @FXML private Button continueButton;

    private HBox selectedBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedBox = null;
    }

    @FXML
    private void selectPremium(MouseEvent event) {
        highlightSelection(premiumBox);
    }

    @FXML
    private void selectStandard(MouseEvent event) {
        highlightSelection(standardBox);
    }

    @FXML
    private void selectStandardPub(MouseEvent event) {
        highlightSelection(pubBox);
    }

    private void highlightSelection(HBox box) {
        // Réinitialiser toutes les bordures
        premiumBox.setStyle("-fx-border-color: #444444; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 15;");
        standardBox.setStyle("-fx-border-color: #444444; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 15;");
        pubBox.setStyle("-fx-border-color: #444444; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 15;");

        // Mettre la bordure rouge sur la sélection
        box.setStyle("-fx-border-color: #E50914; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 15;");

        selectedBox = box;
    }

    @FXML
    private void handleContinuer() {
        if (selectedBox == null) {
            showAlert("Sélectionnez un forfait", "Veuillez choisir un forfait pour continuer.");
            return;
        }

        // ✅ Sauvegarder le forfait choisi dans SessionData
        if (selectedBox == premiumBox) {
            SessionData.setForfait("Premium", "61,16 DT");
        } else if (selectedBox == standardBox) {
            SessionData.setForfait("Standard", "45,86 DT");
        } else if (selectedBox == pubBox) {
            SessionData.setForfait("Standard avec pub", "20,36 DT");
        }

        ScreenManager.getInstance().navigateTo(Screen.signup3);
    }

    @FXML
    private void handleRetour() {
        ScreenManager.getInstance().navigateTo(Screen.signup1);
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}