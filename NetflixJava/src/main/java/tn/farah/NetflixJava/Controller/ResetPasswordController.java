package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import java.sql.*;

public class ResetPasswordController {

    @FXML private PasswordField newPasswordField, confirmPasswordField;
    @FXML private Label errorLabel;

    // On récupère l'email stocké temporairement (tu peux le passer via un static ou ton ScreenManager)
    public static String userEmail; 

    @FXML
    private void handleSavePassword() {
        String pass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (pass.isEmpty() || pass.length() < 4) {
            showError("Password must be at least 4 characters.");
            return;
        }

        if (!pass.equals(confirm)) {
            showError("Passwords do not match!");
            return;
        }

        // MISE À JOUR BDD
        if (updatePasswordInDB(userEmail, pass)) {
            System.out.println("✅ Password updated successfully!");
            ScreenManager.getInstance().navigateTo(Screen.login);
        }
    }

    private boolean updatePasswordInDB(String email, String newPass) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        // Remplace par ta logique de connexion DB
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/netflix", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPass);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}