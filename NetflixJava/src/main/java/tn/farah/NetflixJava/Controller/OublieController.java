package tn.farah.NetflixJava.Controller;



import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OublieController {

    @FXML private TextField emailField;
    @FXML private Button btnConfirmer;
    @FXML private Button btnFermer;

    // Nouveaux composants pour le fake reCAPTCHA
    @FXML private HBox captchaClickZone;
    @FXML private Rectangle captchaBox;
    @FXML private ProgressIndicator captchaProgress;
    @FXML private Label captchaCheck; // Changez en ImageView si vous utilisez une image

    // Variable pour savoir si le captcha est validé
    private boolean isCaptchaValidated = false;

    /**
     * Méthode appelée lors du chargement de la fenêtre
     */
    @FXML
    public void initialize() {
        // Au démarrage, le bouton Confirmer est désactivé
        btnConfirmer.setDisable(true);
        btnConfirmer.setOpacity(0.6); // Petit effet de transparence
    }

    /**
     * Gère le clic sur la zone du reCAPTCHA (case à cocher + texte)
     */
    @FXML
    public void handleCaptchaClick(MouseEvent event) {
        // Si déjà validé, on ne fait rien
        if (isCaptchaValidated) return;

        // 1. Démarrer l'animation de chargement
        // On désactive la zone de clic pour éviter les doubles clics
        captchaClickZone.setDisable(true);
        captchaProgress.setVisible(true); // Afficher le cercle de chargement
        captchaProgress.setProgress(-1);  // Mode indéterminé (tourne en boucle)

        // 2. Simuler une vérification "réseau" d'une seconde et demi
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            // 3. Animation terminée -> Afficher la coche verte
            captchaProgress.setVisible(false); // Cacher le chargement
            captchaCheck.setVisible(true);      // Afficher la coche verte
            
            // On peut ajouter un petit fondu (FadeTransition) ici pour l'élégance

            // 4. Mettre à jour l'état de l'application
            isCaptchaValidated = true;
            
            // 5. Activer le bouton de confirmation rouge
            btnConfirmer.setDisable(false);
            btnConfirmer.setOpacity(1.0); // Rendre le bouton totalement opaque
            btnConfirmer.setCursor(javafx.scene.Cursor.HAND);
        });
        
        delay.play();
    }

    /**
     * Gère le clic sur le bouton rouge "Confirmer"
     */
    @FXML
    public void handleConfirmer(ActionEvent event) {
        // Sécurité supplémentaire même si le bouton est désactivé visuellement
        if (!isCaptchaValidated) {
            afficherMessage(AlertType.WARNING, "reCAPTCHA", "Veuillez cocher la case reCAPTCHA.");
            return;
        }

        String email = emailField.getText();
        
        // ... votre logique de validation d'e-mail précédente ...
        
        System.out.println("reCAPTCHA validé. Demande envoyée pour : " + email);
        afficherMessage(AlertType.INFORMATION, "Succès", "Un e-mail de réinitialisation a été envoyé.");
        fermerFenetre(event);
    }

    // ... méthodes handleFermer, fermerFenetre, afficherMessage précédentes ...
    
    /**
     * Petite méthode utilitaire pour fermer la fenêtre à partir d'un ActionEvent
     */
    private void fermerFenetre(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Petite méthode utilitaire pour afficher des pop-ups de notification
     */
    private void afficherMessage(AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}