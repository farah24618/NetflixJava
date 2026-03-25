package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.net.URL;
import java.util.ResourceBundle;
//..
public class signup2Controller implements Initializable {

    @FXML private HBox premiumBox;
    @FXML private HBox standardBox;
    @FXML private HBox pubBox;
    @FXML private Button continueButton;

    private HBox selectedBox; // Pour savoir quel forfait est choisi

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // On peut initialiser la sélection par défaut si nécessaire
        selectedBox = null;
    }

    // ── Sélection des forfaits ───────────────────────────────
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
        // Réinitialise les bordures
        premiumBox.setStyle(premiumBox.getStyle().replace("-fx-border-color: #E50914;", "-fx-border-color: #444444;"));
        standardBox.setStyle(standardBox.getStyle().replace("-fx-border-color: #E50914;", "-fx-border-color: #444444;"));
        pubBox.setStyle(pubBox.getStyle().replace("-fx-border-color: #E50914;", "-fx-border-color: #444444;"));

        // Mettre la bordure rouge pour la sélection
        box.setStyle(box.getStyle().replace("-fx-border-color: #444444;", "-fx-border-color: #E50914;"));
        selectedBox = box;
    }

    // ── Bouton CONTINUER ───────────────────────────────
    @FXML
    private void handleContinuer() {
        if (selectedBox == null) {
            // Aucun forfait sélectionné
            showAlert("Sélectionnez un forfait", "Veuillez choisir un forfait pour continuer.");
            return;
        }

        // TODO: enregistrer le forfait choisi dans l'objet User ou service
        // Exemple :
        // user.setPlan(selectedPlan);

        // Naviguer vers l'étape suivante
        ScreenManager.getInstance().navigateTo(Screen.signup3);
    }

    // ── Bouton RETOUR ───────────────────────────────
    @FXML
    private void handleRetour() {
        ScreenManager.getInstance().navigateTo(Screen.signup1);
    }

    // ── Méthode utilitaire pour afficher une alerte ─────────────
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}