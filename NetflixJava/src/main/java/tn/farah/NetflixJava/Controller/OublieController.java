package tn.farah.NetflixJava.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.sql.*;

public class OublieController {

    // ── FXML Nodes ─────────────────────────────────────────────────────────
    @FXML private VBox  mainContainer, emailSection, codeContainer;
    @FXML private HBox  captchaBox;
    @FXML private TextField emailField, code1, code2, code3, code4;
    @FXML private Label instructionLabel, emailFeedbackLabel, emailValidIcon, resendBtn;
    @FXML private Button confirmBtn;
    @FXML private CheckBox robotCheckBox;

    // ── State ───────────────────────────────────────────────────────────────
    private String generatedCode;

    // ── Styles constants ────────────────────────────────────────────────────
    private static final String BASE_FIELD =
        "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
        "-fx-prompt-text-fill: #777777; -fx-font-size: 14px; " +
        "-fx-padding: 15 44 15 16; -fx-background-radius: 6; " +
        "-fx-border-radius: 6; -fx-border-width: 1.5;";

    private static final String FIELD_DEFAULT  = BASE_FIELD + "-fx-border-color: #555555;";
    private static final String FIELD_ERROR    = BASE_FIELD + "-fx-border-color: #E50914;";
    private static final String FIELD_SUCCESS  = BASE_FIELD + "-fx-border-color: #2ecc71;";

    private static final String BASE_CODE_FIELD =
        "-fx-background-color: #2a2a2a; -fx-text-fill: white; " +
        "-fx-font-size: 22px; -fx-font-weight: bold; " +
        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1.5;";

    private static final String CODE_DEFAULT = BASE_CODE_FIELD + "-fx-border-color: #555555;";
    private static final String CODE_FOCUSED = BASE_CODE_FIELD + "-fx-border-color: #E50914;";
    private static final String CODE_ERROR   = BASE_CODE_FIELD + "-fx-border-color: #E50914;";

    // ═══════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {

        // ── Validation email en temps réel ──────────────────────────────
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                applyEmailStyle(FIELD_DEFAULT, "", "#aaaaaa", false);
            } else if (!isValidEmail(newVal)) {
                applyEmailStyle(FIELD_ERROR,
                    "Adresse email invalide.",
                    "#E50914", false);
            } else {
                applyEmailStyle(FIELD_SUCCESS,
                    "Format valide",
                    "#2ecc71", true);
            }
        });

        // ── Focus border sur les champs code ─────────────────────────────
        setupCodeBorder(code1);
        setupCodeBorder(code2);
        setupCodeBorder(code3);
        setupCodeBorder(code4);

        // ── Auto-focus suivant dans les cases PIN ─────────────────────────
        setupCodeAutoFocus(code1, code2);
        setupCodeAutoFocus(code2, code3);
        setupCodeAutoFocus(code3, code4);
        setupCodeAutoFocus(code4, null);

        // ── Hover sur le bouton ───────────────────────────────────────────
        confirmBtn.setOnMouseEntered(e ->
            confirmBtn.setStyle(confirmBtn.getStyle()
                .replace("#E50914", "#c40812")));
        confirmBtn.setOnMouseExited(e ->
            confirmBtn.setStyle(confirmBtn.getStyle()
                .replace("#c40812", "#E50914")));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACTION PRINCIPALE
    // ═══════════════════════════════════════════════════════════════════════
    @FXML
    private void handleConfirm() {
        String email = emailField.getText().trim();

        // 1. Vérification captcha
        if (!robotCheckBox.isSelected()) {
            triggerVibration(captchaBox);
            showFeedback(instructionLabel,
                "Veuillez confirmer que vous n'êtes pas un robot.",
                "#E50914");
            return;
        }

        // 2. Vérification format email
        if (!isValidEmail(email)) {
            triggerVibration(emailField);
            return;
        }

        if (generatedCode == null) {
            // 3. Vérification en base
            if (!checkEmailExistsInDB(email)) {
                applyEmailStyle(FIELD_ERROR,
                    "Aucun compte trouvé avec cet email.",
                    "#E50914", false);
                triggerVibration(emailField);
                return;
            }

            // 4. Génération du code et passage à l'étape 2
            generatedCode = String.valueOf((int)(Math.random() * 9000) + 1000);
            System.out.println("[DEV] Code Netflix : " + generatedCode);
            switchToCodeStep();

        } else {
            // 5. Vérification du code PIN entré
            verifyPinCode(email);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // LOGIQUE ÉTAPES
    // ═══════════════════════════════════════════════════════════════════════
    private void switchToCodeStep() {
        // Cacher étape 1
        emailSection.setManaged(false);
        emailSection.setVisible(false);
        captchaBox.setManaged(false);
        captchaBox.setVisible(false);

        // Afficher étape 2
        codeContainer.setManaged(true);
        codeContainer.setVisible(true);

        // Màj textes
        instructionLabel.setText("Entrez le code à 4 chiffres envoyé à votre email.");
        instructionLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px; -fx-padding: 0 0 24 0;");
        confirmBtn.setText("VÉRIFIER LE CODE");

        // Focus sur le 1er champ
        code1.requestFocus();
    }

    private void verifyPinCode(String email) {
        String entered = code1.getText() + code2.getText()
                       + code3.getText() + code4.getText();

        if (entered.length() < 4) {
            triggerVibration(codeContainer);
            showFeedback(instructionLabel, "Veuillez remplir les 4 cases.", "#E50914");
            return;
        }

        if (entered.equals(generatedCode)) {
            // Succès : colorer les cases en vert
            highlightCodeFields("#2ecc71");
            ResetPasswordController.userEmail = email;
            // Petit délai visuel avant navigation
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(400),
                e -> ScreenManager.getInstance().navigateTo(Screen.ResetPassword)));
            delay.play();
        } else {
            // Erreur : secouer + border rouge
            highlightCodeFields("#E50914");
            triggerVibration(codeContainer);
            clearCodeFields();
            code1.requestFocus();
        }
    }

    @FXML
    private void handleResend() {
        generatedCode = String.valueOf((int)(Math.random() * 9000) + 1000);
        System.out.println("[DEV] Nouveau code : " + generatedCode);
        clearCodeFields();
        code1.requestFocus();
        showFeedback(instructionLabel, "Un nouveau code a été généré.", "#2ecc71");
    }

    @FXML
    private void handleGoToLogin() {
        ScreenManager.getInstance().navigateTo(Screen.login);
    }

    @FXML
    private void handleClose() {
        if (mainContainer.getScene() != null)
            mainContainer.getScene().getWindow().hide();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════════════

    /** Applique le style + feedback + icône check sur le champ email */
    private void applyEmailStyle(String fieldStyle, String feedbackText,
                                  String feedbackColor, boolean showCheck) {
        emailField.setStyle(fieldStyle);
        emailFeedbackLabel.setText(feedbackText);
        emailFeedbackLabel.setStyle("-fx-font-size: 12px; -fx-padding: 0 0 0 4; -fx-text-fill: " + feedbackColor + ";");
        emailValidIcon.setVisible(showCheck);
    }

    /** Feedback sur n'importe quel Label */
    private void showFeedback(Label label, String text, String color) {
        label.setText(text);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-padding: 0 0 24 0;");
    }

    /** Highlight les 4 cases de code avec une couleur de bordure */
    private void highlightCodeFields(String color) {
        String style = BASE_CODE_FIELD + "-fx-border-color: " + color + ";";
        code1.setStyle(style); code2.setStyle(style);
        code3.setStyle(style); code4.setStyle(style);
    }

    private void clearCodeFields() {
        code1.clear(); code2.clear(); code3.clear(); code4.clear();
        String def = CODE_DEFAULT;
        code1.setStyle(def); code2.setStyle(def);
        code3.setStyle(def); code4.setStyle(def);
    }

    /** Border rouge au focus sur champs code */
    private void setupCodeBorder(TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                field.setStyle(CODE_FOCUSED);
            } else if (field.getText().isEmpty()) {
                field.setStyle(CODE_DEFAULT);
            }
        });
    }

    /** Passe le focus au champ suivant après 1 chiffre */
    private void setupCodeAutoFocus(TextField current, TextField next) {
        current.textProperty().addListener((obs, oldV, newV) -> {
            // Autoriser uniquement les chiffres
            if (!newV.matches("[0-9]*")) {
                current.setText(oldV);
                return;
            }
            if (newV.length() > 1) {
                current.setText(String.valueOf(newV.charAt(0)));
                return;
            }
            if (newV.length() == 1 && next != null) {
                next.requestFocus();
            }
        });
    }

    /** Animation de vibration sur un Node */
    private void triggerVibration(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(0); tt.setToX(8);
        tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.play();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BASE DE DONNÉES
    // ═══════════════════════════════════════════════════════════════════════
    private boolean checkEmailExistsInDB(String email) {
        String url = "jdbc:mysql://localhost:3306/netflix";
        try (Connection conn = DriverManager.getConnection(url, "root", "");
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM users WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Vérifie le format email avec regex */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}