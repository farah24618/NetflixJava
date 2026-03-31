package tn.farah.NetflixJava.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import java.security.MessageDigest;
import java.sql.*;

public class ResetPasswordController {

    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private Label statusLabel, strengthLabel;
    @FXML private Button saveBtn;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Rectangle strengthBar;

    public static String userEmail; 

    @FXML
    public void initialize() {
        // Initialisation propre
        statusLabel.setVisible(false);
        progressIndicator.setVisible(false);
        strengthBar.setWidth(0);

        // 1. Analyse de la force du mot de passe (Temps réel)
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateStrengthMeter(newVal);
        });

        // 2. Validation de la confirmation (Couleur des bordures)
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                confirmPasswordField.setStyle("-fx-border-color: #555555; -fx-background-color: #333333; -fx-text-fill: white;");
            } else if (!newVal.equals(newPasswordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: #e50914; -fx-background-color: #333333; -fx-text-fill: white;");
            } else {
                confirmPasswordField.setStyle("-fx-border-color: #2ecc71; -fx-background-color: #333333; -fx-text-fill: white;");
            }
        });
    }

    private void updateStrengthMeter(String password) {
        if (password == null || password.isEmpty()) {
            animateBar(0, "#333333");
            strengthLabel.setText("");
            return;
        }

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[!@#$%^&*()].*")) score++;

        double targetWidth;
        String color;
        String text;

        switch (score) {
            case 0: case 1: targetWidth = 80; color = "#e50914"; text = "Weak (Too risky)"; break;
            case 2: targetWidth = 160; color = "#f39c12"; text = "Medium (Better)"; break;
            case 3: targetWidth = 240; color = "#f1c40f"; text = "Strong (Good)"; break;
            case 4: targetWidth = 350; color = "#2ecc71"; text = "Ultra Secure (Perfect)"; break;
            default: targetWidth = 0; color = "#333333"; text = "";
        }

        animateBar(targetWidth, color);
        strengthLabel.setText(text);
        strengthLabel.setTextFill(Color.web(color));
    }

    private void animateBar(double newWidth, String colorCode) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(300), 
                new KeyValue(strengthBar.widthProperty(), newWidth),
                new KeyValue(strengthBar.fillProperty(), Color.web(colorCode))
            )
        );
        timeline.play();
    }

    @FXML
    private void handleSavePassword() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (pass.isEmpty() || !pass.equals(confirm)) {
            showStatus("Passwords must match!", "#e50914");
            return;
        }

        saveBtn.setDisable(true);
        progressIndicator.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (updatePasswordInDB(userEmail, hashSHA256(pass))) {
                showStatus("✅ Success! Redirecting...", "#2ecc71");
                
                // Redirection vers Profiles
                PauseTransition nav = new PauseTransition(Duration.seconds(1.5));
                nav.setOnFinished(e -> ScreenManager.getInstance().navigateTo(Screen.pofiles));
                nav.play();
            } else {
                saveBtn.setDisable(false);
                progressIndicator.setVisible(false);
                showStatus("Error: Database connection failed.", "#e50914");
            }
        });
        delay.play();
    }

    private String hashSHA256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) { sb.append(String.format("%02x", b)); }
            return sb.toString();
        } catch (Exception e) { return data; }
    }

    private boolean updatePasswordInDB(String email, String hashedPass) {
        String url = "jdbc:mysql://localhost:3306/netflix"; 
        // Note: Assure-toi que la colonne s'appelle 'password_hash' comme vu sur ta capture
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, "root", "");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, hashedPass);
            pstmt.setString(2, email);
            
            int affected = pstmt.executeUpdate();
            System.out.println("DEBUG: Row updated: " + affected + " for " + email);
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showStatus(String message, String colorCode) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web(colorCode));
        statusLabel.setVisible(true);
    }
}