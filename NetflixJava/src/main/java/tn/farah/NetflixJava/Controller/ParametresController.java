package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ParametresController {

    // ==================== FXML FIELDS ====================
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // ==================== SERVICES ====================
    private UserService userService;
    private User currentUser;

    // ==================== INITIALISATION ====================
    @FXML
    public void initialize() {
        try {
            userService = new UserService(ConxDB.getInstance());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Connexion base de données échouée.");
            return;
        }

        // ✅ Récupérer l'utilisateur connecté depuis la session
        
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            nomField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
            prenomField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        } else {
            showAlert(Alert.AlertType.WARNING, "Session expirée", "Aucun utilisateur connecté.");
        }
    }

    // ==================== ENREGISTRER ====================
    @FXML
    private void onSaveSettings(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté.");
            return;
        }

        String newNom    = nomField.getText().trim();
        String newPrenom = prenomField.getText().trim();
        String newEmail  = emailField.getText().trim();
        String newPassword = passwordField.getText();

        // --- Validations ---
        if (newNom.isEmpty() || newPrenom.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Nom et prénom sont obligatoires.");
            return;
        }

        if (!newEmail.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "Veuillez entrer une adresse email valide.");
            return;
        }

        if (!newPassword.isEmpty() && newPassword.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe trop court", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // --- Mise à jour de l'objet ---
        currentUser.setNom(newNom);
        currentUser.setPrenom(newPrenom);
        currentUser.setEmail(newEmail);

        // Changer le mot de passe seulement s'il est renseigné
        if (!newPassword.isEmpty()) {
            currentUser.setPasswordHash(hashSHA256(newPassword));
        }

        // --- Appel au service ---
        boolean success = userService.updateUser(currentUser);

        if (success) {
            // ✅ Mettre à jour la session avec les nouvelles données
            SessionManager.getInstance().login(currentUser);
            passwordField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Modifications enregistrées avec succès.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder les modifications.");
        }
    }

    // ==================== ANNULER ====================
    @FXML
    private void handleCancel(ActionEvent event) {
        // Recharger les données originales depuis la session
        if (currentUser != null) {
            nomField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
            prenomField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            passwordField.clear();
        }
    }

    // ==================== UTILS ====================
    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponible", e);
        }
    }
    
    @FXML
    private void onDashboardClicked() {
        // On retourne au signup1, les données seront restaurées par son initialize()
        ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    @FXML
    void onFilmsClicked(ActionEvent event) {
        // Utilise VOTRE ScreenManager pour aller vers Films
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
        
        // Note: Vous pouvez aussi utiliser navigateWithSplash(Screen.films) 
        // si la page des films met du temps à charger.
    }

    @FXML
    void onSeriesClicked(ActionEvent event) {
        // Utilise VOTRE ScreenManager pour aller vers Séries
       // ScreenManager.getInstance().navigateTo(Screen.);
    }

    @FXML
    void onCommentsClicked(ActionEvent event) {
        // Utilise VOTRE ScreenManager pour aller vers Commentaires
        ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
    }

    // --- Les autres boutons pour plus tard ---
    @FXML void onNotificationsClicked(ActionEvent event) {
   	 ScreenManager.getInstance().navigateTo(Screen.notificationAdmin);
   }
    @FXML
    void onUsersClicked(ActionEvent event) {
        System.out.println("Page Utilisateurs non définie dans l'enum Screen pour le moment");
    }
    @FXML
    void onSettingsClicked(ActionEvent event) {
        // Optionnel : ne rien faire car on y est déjà
        System.out.println("Déjà sur la page Paramètres");
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    void onLogoutClicked(ActionEvent event) {
        // Fermeture propre de l'application
        System.exit(0);
    }
}