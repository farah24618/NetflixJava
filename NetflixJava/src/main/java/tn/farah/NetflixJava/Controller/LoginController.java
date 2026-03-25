package tn.farah.NetflixJava.Controller;

import java.awt.Button;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.PreferencesStore;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class LoginController implements Initializable{
	@FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        signInButton;
    @FXML private CheckBox      rememberMeCheckBox;
    @FXML private Button        forgotPasswordButton;
    @FXML private Button        signUpButton;
    @FXML private Hyperlink     learnMoreLink;
    private Connection  connection;
    private UserService userservice = new UserService(connection);


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		emailField.setOnKeyPressed(this::handleEnterKey);
        passwordField.setOnKeyPressed(this::handleEnterKey);

        // Restore "Remember me" email if previously saved
        String savedEmail = PreferencesStore.getSavedEmail();
        if (savedEmail != null && !savedEmail.isEmpty()) {
            emailField.setText(savedEmail);
            rememberMeCheckBox.setSelected(true);
        }
	}
	@FXML
	private void handleRememberMe(ActionEvent event) {
	    if (rememberMeCheckBox.isSelected()) {
	        PreferencesStore.saveEmail(emailField.getText().trim());
	    } else {
	        PreferencesStore.clearEmail();
	    }
	}
	 @FXML
	    private void handleSignIn(ActionEvent event) {
	        String email    = emailField.getText().trim();
	        String password = passwordField.getText();

	        // Basic validation
	        if (email.isEmpty() || password.isEmpty()) {
	            showAlert(Alert.AlertType.WARNING,
	                      "Missing Fields",
	                      "Please enter both your email / phone number and password.");
	            return;
	        }

	        if (!isValidEmail(email)) {
	            showAlert(Alert.AlertType.ERROR,
	                      "Invalid Email",
	                      "Please enter a valid email address or phone number.");
	            return;
	        }

	        // Persist email when "Remember me" is checked
	        if (rememberMeCheckBox.isSelected()) {
	            PreferencesStore.saveEmail(email);
	        } else {
	            PreferencesStore.clearEmail();
	        }

	        // TODO: Replace this block with your real authentication logic
	       User authenticated = userservice.loginUser(email, password);

	        if (authenticated !=null) {
	            navigateToDashboard();
	            ScreenManager.getInstance().navigateTo(Screen.pofiles);
	        } else {
	            showAlert(Alert.AlertType.ERROR,
	                      "Sign In Failed",
	                      "Incorrect email or password. Please try again.");
	            passwordField.clear();
	            passwordField.requestFocus();
	        }
	    }

	    // ── Forgot Password ──────────────────────────────────────────────────────
	    @FXML
	    private void handleForgotPassword(ActionEvent event) {

	    	//ScreenManager.getInstance().navigateTo();
	    }
	    @FXML
	    private void handleSignUp(ActionEvent event) {
	    	ScreenManager.getInstance().navigateTo(Screen.logup);
	    }
	    @FXML
	    private void handleLearnMore(ActionEvent event) {
	        // TODO: Open browser / info dialog about Google reCAPTCHA
	        showAlert(Alert.AlertType.INFORMATION,
	                  "About reCAPTCHA",
	                  "Google reCAPTCHA protects this page from automated abuse.");
	    }
	    @FXML
	    private void handleEnterKey(KeyEvent event) {
	    	if (event.getCode() == KeyCode.ENTER) {
				signInButton.fire();
			}
	    }
	    private boolean isValidEmail(String input) {
	        // Accepts e-mail format or a numeric phone number (7–15 digits)
	        return input.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")
	            || input.matches("^\\+?[0-9]{7,15}$");
	    }

	    private void navigateToDashboard() {
	        // TODO: Load Dashboard.fxml and switch scenes
	        showAlert(Alert.AlertType.INFORMATION,
	                  "Welcome!",
	                  "Login successful. Loading dashboard…");
	    }

	    private void showAlert(Alert.AlertType type, String title, String message) {
	        Alert alert = new Alert(type);
	        alert.setTitle(title);
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    }


}
