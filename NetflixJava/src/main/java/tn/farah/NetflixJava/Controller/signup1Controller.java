package tn.farah.NetflixJava.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionData;
import tn.farah.NetflixJava.utils.ConxDB;

public class signup1Controller implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<Integer> dayComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private Button continueButton;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService(ConxDB.getInstance());

        // Configuration des ComboBox
        dayComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 31).boxed().toList()));
        monthComboBox.setItems(FXCollections.observableArrayList("Janvier","Février","Mars","Avril","Mai","Juin","Juillet","Août","Septembre","Octobre","Novembre","Décembre"));
        int currentYear = LocalDate.now().getYear();
        yearComboBox.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(currentYear - 100, currentYear).boxed().sorted((a, b) -> b - a).toList()));

        // --- RÉCUPÉRATION DES DONNÉES SI RETOUR ---
        User existingUser = SessionData.getCurrentUser();
        if (existingUser != null) {
            firstNameField.setText(existingUser.getPrenom());
            lastNameField.setText(existingUser.getNom());
            emailField.setText(existingUser.getEmail());
            phoneField.setText(existingUser.getPhone());
            
            if (existingUser.getBirthDate() != null) {
                dayComboBox.setValue(existingUser.getBirthDate().getDayOfMonth());
                monthComboBox.setValue(numberToMonth(existingUser.getBirthDate().getMonthValue()));
                yearComboBox.setValue(existingUser.getBirthDate().getYear());
            }
            // Note: Le mot de passe reste vide par sécurité pour éviter de modifier le hash existant par erreur
        }

        // Handlers
        firstNameField.setOnKeyPressed(this::handleEnterKey);
        lastNameField.setOnKeyPressed(this::handleEnterKey);
        emailField.setOnKeyPressed(this::handleEnterKey);
        passwordField.setOnKeyPressed(this::handleEnterKey);
        phoneField.setOnKeyPressed(this::handleEnterKey);
    }

    @FXML
    private void handleContinuer(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();
        String passwordRaw = passwordField.getText();
        String phone     = phoneField.getText().trim();

        // 1. Vérification des champs vides
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || 
            dayComboBox.getValue() == null || monthComboBox.getValue() == null || yearComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        // 2. Validation du Prénom et Nom (min 3 caractères)
        if (firstName.length() < 3 || lastName.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Format Nom/Prénom", "Le nom et le prénom doivent contenir au moins 3 caractères.");
            return;
        }

        // 3. Validation de l'Email (min 3 car. avant @gmail.com)
        if (!email.matches("^[\\w.]{3,}@gmail\\.com$")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "L'email doit contenir au moins 3 caractères avant '@gmail.com'.");
            return;
        }

        // 4. Validation du Téléphone (Exactement 8 chiffres)
        if (!phone.matches("^\\d{8}$")) {
            showAlert(Alert.AlertType.WARNING, "Téléphone invalide", "Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }

        // 5. Validation du Password (min 6 caractères)
        if (!passwordRaw.isEmpty() && passwordRaw.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Sécurité faible", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // --- SI TOUT EST VALIDE : TRAITEMENT ---
        User user = (SessionData.getCurrentUser() != null) ? SessionData.getCurrentUser() : new User();
        
        user.setPrenom(firstName);
        user.setNom(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setBirthDate(LocalDate.of(yearComboBox.getValue(), monthToNumber(monthComboBox.getValue()), dayComboBox.getValue()));
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setEstPaye(false);

        if (!passwordRaw.isEmpty()) {
            user.setPasswordHash(hashSHA256(passwordRaw));
        } else if (user.getPasswordHash() == null) {
            showAlert(Alert.AlertType.WARNING, "Sécurité", "Veuillez saisir un mot de passe.");
            return;
        }

        if (user.getPseudo() == null) {
            user.setPseudo(firstName.toLowerCase() + (int)(Math.random() * 1000));
        }

        SessionData.setCurrentUser(user);
        ScreenManager.getInstance().navigateTo(Screen.signup2);
    }
    private String numberToMonth(int n) {
        String[] months = {"Janvier","Février","Mars","Avril","Mai","Juin","Juillet","Août","Septembre","Octobre","Novembre","Décembre"};
        return (n >= 1 && n <= 12) ? months[n-1] : "Janvier";
    }

    private int monthToNumber(String month) {
        return switch (month) {
            case "Janvier" -> 1; case "Février" -> 2; case "Mars" -> 3; case "Avril" -> 4;
            case "Mai" -> 5; case "Juin" -> 6; case "Juillet" -> 7; case "Août" -> 8;
            case "Septembre" -> 9; case "Octobre" -> 10; case "Novembre" -> 11; case "Décembre" -> 12;
            default -> 1;
        };
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) continueButton.fire();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String hashSHA256(String data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return data; }
    }
    
    @FXML private void handleSeConnecter(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.mainView);
    }
}