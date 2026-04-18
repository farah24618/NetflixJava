package tn.farah.NetflixJava.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionData;

public class AjouterAdminController implements Initializable {

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

        dayComboBox.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 31).boxed().toList()));
        monthComboBox.setItems(FXCollections.observableArrayList(
                "Janvier","Février","Mars","Avril","Mai","Juin",
                "Juillet","Août","Septembre","Octobre","Novembre","Décembre"));
        int currentYear = LocalDate.now().getYear();
        yearComboBox.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(currentYear - 100, currentYear)
                         .boxed()
                         .sorted((a, b) -> b - a)
                         .toList()));
        firstNameField.setOnKeyPressed(e -> { if (e.getCode().toString().equals("ENTER")) handleContinuer(null); });
        lastNameField.setOnKeyPressed(e  -> { if (e.getCode().toString().equals("ENTER")) handleContinuer(null); });
        emailField.setOnKeyPressed(e     -> { if (e.getCode().toString().equals("ENTER")) handleContinuer(null); });
        passwordField.setOnKeyPressed(e  -> { if (e.getCode().toString().equals("ENTER")) handleContinuer(null); });
        phoneField.setOnKeyPressed(e     -> { if (e.getCode().toString().equals("ENTER")) handleContinuer(null); });
    }

    @FXML
    private void handleContinuer(ActionEvent event) {
        String firstName   = firstNameField.getText().trim();
        String lastName    = lastNameField.getText().trim();
        String email       = emailField.getText().trim();
        String passwordRaw = passwordField.getText();
        String phone       = phoneField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()
                || dayComboBox.getValue() == null
                || monthComboBox.getValue() == null
                || yearComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (firstName.length() < 3 || lastName.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Format Nom/Prénom",
                    "Le nom et le prénom doivent contenir au moins 3 caractères.");
            return;
        }

        if (!email.matches("^[\\w.]{3,}@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide",
                    "L'email doit contenir au moins 3 caractères avant l'arobase et avoir un domaine valide.");
            return;
        }
        if (userService.findByEmail(email) != null) {
            showAlert(Alert.AlertType.WARNING, "Email déjà utilisé",
                    "Cet email est déjà associé à un compte existant.");
            return;
        }

        if (!phone.matches("^\\d{8}$")) {
            showAlert(Alert.AlertType.WARNING, "Téléphone invalide",
                    "Le numéro de téléphone doit contenir exactement 8 chiffres.");
            return;
        }


        if (passwordRaw.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe manquant",
                    "Veuillez saisir un mot de passe.");
            return;
        }
        if (passwordRaw.length() < 8) {
            showAlert(Alert.AlertType.WARNING, "Sécurité faible",
                    "Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

    
        User admin = new User();
        admin.setPrenom(firstName);
        admin.setNom(lastName);
        admin.setEmail(email);
        admin.setPhone(phone);
        admin.setBirthDate(LocalDate.of(
                yearComboBox.getValue(),
                monthToNumber(monthComboBox.getValue()),
                dayComboBox.getValue()));

        admin.setRole(UserRole.ADMIN);


        admin.setEstPaye(false);

        admin.setActive(true);
        admin.setPasswordHash(hashSHA256(passwordRaw));


        admin.setPseudo(firstName.toLowerCase() + (int)(Math.random() * 1000));

       
        boolean success = userService.registerUser(admin);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Le compte administrateur a été créé avec succès.");
           
            ScreenManager.getInstance().navigateTo(Screen.mainView);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la création du compte admin.");
        }
    }


    private int monthToNumber(String month) {
        return switch (month) {
            case "Janvier"   -> 1;  case "Février"   -> 2;
            case "Mars"      -> 3;  case "Avril"      -> 4;
            case "Mai"       -> 5;  case "Juin"       -> 6;
            case "Juillet"   -> 7;  case "Août"       -> 8;
            case "Septembre" -> 9;  case "Octobre"    -> 10;
            case "Novembre"  -> 11; case "Décembre"   -> 12;
            default          -> 1;
        };
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
        } catch (Exception e) {
            return data;
        }
    }
}