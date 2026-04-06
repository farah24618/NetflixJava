package tn.farah.NetflixJava.Controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

/**
 * Controller for EditProfile.fxml
 *
 * Features:
 *  - Load current user's pseudo into the field on init
 *  - Save: validate pseudo is non-empty AND unique in DB (excluding current user)
 *  - Cancel: navigate back to Home without saving
 *  - Delete Profile: clears session + goes to Login
 *  - Logout button: clears session + goes to Login
 */
public class EditProfileController implements Initializable {

    // ── FXML ────────────────────────────────────────────────────────────────
    @FXML private TextField pseudoField;
    @FXML private Label     pseudoErrorLabel;   // red validation message
    @FXML private Label     statusLabel;        // green success message
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button    saveBtn;
    @FXML private Button    logoutBtn;

    // ── Services ─────────────────────────────────────────────────────────────
    private UserService userService;
    private Connection  connection;

    // ── Current user (loaded from session) ──────────────────────────────────
    private User currentUser;

    // ════════════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        connection  = ConxDB.getInstance();
        userService = new UserService(connection);

        // Load language options
        languageCombo.setItems(FXCollections.observableArrayList(
                "Français", "English", "العربية", "Deutsch", "Español"));

        // Load current user from session
        int userId = SessionManager.getInstance().getCurrentUserId();
        currentUser = userService.findUserById(userId);

        if (currentUser != null) {
            pseudoField.setText(currentUser.getPseudo() != null
                    ? currentUser.getPseudo() : "");
        }

        // Live validation: clear error as soon as user types
        pseudoField.textProperty().addListener((obs, oldVal, newVal) -> {
            hideError();
            hideStatus();
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SAVE  — validates uniqueness then persists
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onSave() {
        hideError();
        hideStatus();

        final String newPseudo = pseudoField.getText() == null
                ? "" : pseudoField.getText().trim();

        // 1. Non-empty check
        if (newPseudo.isEmpty()) {
            showError("Le pseudo ne peut pas être vide.");
            return;
        }

        // 2. Min length
        if (newPseudo.length() < 3) {
            showError("Le pseudo doit contenir au moins 3 caractères.");
            return;
        }

        // 3. Uniqueness check (skip if unchanged)
        if (!newPseudo.equals(currentUser.getPseudo())) {
            boolean pseudoTaken = userService.isPseudoTaken(newPseudo, currentUser.getId());
            if (pseudoTaken) {
                showError("Ce pseudo est déjà utilisé. Choisissez-en un autre.");
                pseudoField.requestFocus();
                return;
            }
        }

        // 4. Persist
        currentUser.setPseudo(newPseudo);
        boolean updated = userService.updatePseudo(currentUser.getId(), newPseudo);

        if (updated) {
            // Update session cache if your SessionManager holds the pseudo
         
            showStatus("✔  Profil mis à jour avec succès !");
            // Auto-navigate back to Home after 1.5 s
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> goHome());
            delay.play();
        } else {
            showError("Erreur lors de la mise à jour. Réessayez.");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CANCEL  — back to Home, no changes saved
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onCancel() {
        goHome();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DELETE PROFILE
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onDeleteProfile() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le profil");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer votre profil ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userService.deleteUser(currentUser.getId());
                logout();
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOGOUT
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void onLogout() {
        logout();
    }

    /** Clears session and redirects to the Login screen. */
    private void logout() {
        //SessionManager.getInstance().clear();
        ScreenManager.getInstance().navigateTo(Screen.mainView);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ════════════════════════════════════════════════════════════════════════

    private void goHome() {
        ScreenManager.getInstance().navigateTo(Screen.home);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private void showError(final String msg) {
        pseudoErrorLabel.setText(msg);
        pseudoErrorLabel.setVisible(true);
        pseudoErrorLabel.setManaged(true);

        // Shake animation on the TextField
        javafx.animation.TranslateTransition shake =
                new javafx.animation.TranslateTransition(Duration.millis(60), pseudoField);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void hideError() {
        pseudoErrorLabel.setVisible(false);
        pseudoErrorLabel.setManaged(false);
        pseudoErrorLabel.setText("");
    }

    private void showStatus(final String msg) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.millis(400), statusLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void hideStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        statusLabel.setText("");
    }
}
