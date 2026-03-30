package tn.farah.NetflixJava.Controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class OublieController {

    @FXML private VBox mainContainer, emailSection, codeContainer;
    @FXML private HBox captchaBox;
    @FXML private TextField emailField, code1, code2, code3, code4;
    @FXML private Label instructionLabel;
    @FXML private Button confirmBtn;
    @FXML private CheckBox robotCheckBox;

    private String generatedCode;

    @FXML
    public void initialize() {
        // Configuration de l'auto-focus pour les 4 cases du code
        setupCodeField(code1, code2);
        setupCodeField(code2, code3);
        setupCodeField(code3, code4);
        setupCodeField(code4, null);
    }

    private void setupCodeField(TextField current, TextField next) {
        current.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 1) current.setText(oldV);
            if (newV.length() == 1 && next != null) next.requestFocus();
        });
    }

    @FXML
    private void handleConfirm() {
        // 1. Vérification du reCAPTCHA
        if (!robotCheckBox.isSelected()) {
            triggerVibration(captchaBox);
            return;
        }

        if (generatedCode == null) {
            // ÉTAPE 1 : Générer le code et afficher l'interface de saisie
            generatedCode = String.valueOf((int)(Math.random() * 9000) + 1000);
            
            System.out.println("---------- DEBUG NETFLIX ----------");
            System.out.println("Email : " + emailField.getText());
            System.out.println("CODE DE SÉCURITÉ : " + generatedCode);
            System.out.println("-----------------------------------");

            emailSection.setManaged(false);
            emailSection.setVisible(false);
            captchaBox.setManaged(false);
            captchaBox.setVisible(false);

            codeContainer.setManaged(true);
            codeContainer.setVisible(true);
            
            instructionLabel.setText("Please enter the code shown in your console.");
            confirmBtn.setText("Verify Code");
            
        } else {
            // ÉTAPE 2 : Vérifier le code saisi
            String enteredCode = code1.getText() + code2.getText() + code3.getText() + code4.getText();
            
            if (enteredCode.equals(generatedCode)) {
                System.out.println("✅ Code Correct !");
                
                // 1. On stocke l'email dans le contrôleur suivant pour la mise à jour SQL
                ResetPasswordController.userEmail = emailField.getText();
                
                // 2. On change d'interface vers le nouveau mot de passe
                ScreenManager.getInstance().navigateTo(Screen.ResetPassword);
                
            } else {
                // ❌ ERREUR : Vibration et reset des cases
                triggerVibration(mainContainer);
                code1.clear(); code2.clear(); code3.clear(); code4.clear();
                code1.requestFocus();
            }
        }
    }

    private void triggerVibration(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setToX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        
        node.setStyle(node.getStyle() + "-fx-border-color: #e50914;");
        tt.setOnFinished(e -> node.setStyle(node.getStyle().replace("-fx-border-color: #e50914;", "")));
        
        tt.play();
    }

    @FXML
    private void handleClose() {
        if (mainContainer.getScene() != null) {
            mainContainer.getScene().getWindow().hide();
        }
    }
}