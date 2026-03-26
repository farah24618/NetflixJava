package tn.farah.NetflixJava.Controller;


import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class MainViewController implements Initializable {

    // ── FXML Injections ───────────────────────────────────────────────────────

    @FXML private Button signInButton;

    @FXML private TextField heroEmailField;      // Hero section email input

    @FXML private Button heroGetStartedButton;   // Hero section "Get Started ›"

    @FXML private TextField faqEmailField;       // FAQ section email input (bottom CTA)

    @FXML private Button faqGetStartedButton;    // FAQ section "Get Started ›"

    // ── Initializable ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        signInButton.setOnAction(e -> handleSignIn());
        heroGetStartedButton.setOnAction(e -> handleGetStarted(heroEmailField));
        faqGetStartedButton.setOnAction(e -> handleGetStarted(faqEmailField));
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    /**
     * "Sign In" button — goes to Login screen.
     * navigateAndReplace so the landing page is NOT kept in the back-stack.
     */
    @FXML
    private void handleSignIn() {
        ScreenManager.getInstance().navigateAndReplace(Screen.signup1);
    }

    /**
     * "Get Started ›" — validates email format then navigates to Register,
     * passing the typed email to pre-fill the form.
     */
    @FXML
    private void handleHeroGetStarted() {
        handleGetStarted(heroEmailField);
    }

    @FXML
    private void handleFaqGetStarted() {
        handleGetStarted(faqEmailField);
    }

    private void handleGetStarted(TextField emailField) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Email Required", "Please enter your email address to continue.");
            emailField.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            showError("Invalid Email", "Please enter a valid email address.");
            emailField.requestFocus();
            return;
        }

        // Pass the email to the Register screen so it's pre-filled
        /*RegisterController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.logup);
        if (ctrl != null) {
            ctrl.prefillEmail(email);
        }*/
    }

    // ── Nav link handlers (optional — wire via fx:onMouseClicked in FXML) ─────



    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
