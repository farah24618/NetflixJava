package tn.farah.NetflixJava.Controller;
import java.net.URL;

import java.sql.Connection;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.PreferencesStore;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

public class LoginController implements Initializable{
	@FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        signInButton;
    @FXML private CheckBox      rememberMeCheckBox;
    @FXML private Button        forgotPasswordButton;
    @FXML private Button        signUpButton;
    @FXML private Hyperlink     learnMoreLink;
    private Connection  connection;
    private UserService userservice ;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Connection connection = ConxDB.getInstance();
        if (connection == null) return;
        userservice=new UserService(connection);
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
	    /*if (rememberMeCheckBox.isSelected()) {
	        PreferencesStore.saveEmail(emailField.getText().trim());
	    } else {
	        PreferencesStore.clearEmail();
	    }*/
	}
	@FXML
	private void handleSignIn(ActionEvent event) {
	    String email = emailField.getText().trim();
	    String passwordRaw = passwordField.getText();

	    if (email.isEmpty() || passwordRaw.isEmpty()) {
	        showAlert(Alert.AlertType.WARNING,
	                  "Champs manquants",
	                  "Veuillez entrer votre email et votre mot de passe.");
	        return;
	    }

	    if (!isValidEmail(email)) {
	        showAlert(Alert.AlertType.ERROR,
	                  "Email invalide",
	                  "Veuillez entrer une adresse email valide.");
	        return;
	    }

	    String passwordHashed = hashSHA256(passwordRaw);
	    User authenticated = userservice.loginUser(email, passwordHashed);

	    if (authenticated != null) {
	        SessionManager.getInstance().login(authenticated);

	        // ✅ Vérification du rôle pour la navigation
	        if (authenticated.getRole() == UserRole.ADMIN) {
	            ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
	        } else {
	            ScreenManager.getInstance().navigateTo(Screen.home);
	        }

	        System.out.println("DEBUG: Login réussi pour " + email + " | Rôle: " + authenticated.getRole());

	    } else {
	        showAlert(Alert.AlertType.ERROR,
	                  "Échec de connexion",
	                  "Email ou mot de passe incorrect. Veuillez réessayer.");
	        passwordField.clear();
	        passwordField.requestFocus();
	    }
	}
	    // ── Forgot Password ──────────────────────────────────────────────────────
	    @FXML
	    private void handleForgotPassword(ActionEvent event) {
	    	ScreenManager.getInstance().navigateTo(Screen.oublie);
	    	
	    }
	    @FXML
	    private void handleSignUp(ActionEvent event) {
	    	ScreenManager.getInstance().navigateTo(Screen.signup1);
	    }
	    @FXML
	    private void handleLearnMore(ActionEvent event) {
	        // TODO: Open browser / info dialog about Google reCAPTCHA
	        showAlert(Alert.AlertType.INFORMATION,
	                  "À propos de reCAPTCHA",
	                  "Google reCAPTCHA protège cette page contre les abus automatisés.");
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

	    

	    private void showAlert(Alert.AlertType type, String title, String message) {
	        Alert alert = new Alert(type);
	        alert.setTitle(title);
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    }
		public void prefillEmail(String email) {
			emailField.setText(email);
			
		}
		private String hashSHA256(String data) {
		    try {
		        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
		        byte[] hash = md.digest(data.getBytes());
		        StringBuilder sb = new StringBuilder();
		        for (byte b : hash) { 
		            sb.append(String.format("%02x", b)); 
		        }
		        return sb.toString();
		    } catch (Exception e) { 
		        return data; 
		    }
		}


}