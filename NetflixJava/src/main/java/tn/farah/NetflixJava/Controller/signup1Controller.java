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
    @FXML private Button seConnecterButton;

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
        String password  = passwordField.getText();
        String phone     = phoneField.getText().trim();

        Integer day   = dayComboBox.getValue();
        String month  = monthComboBox.getValue();
        Integer year  = yearComboBox.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
            password.isEmpty() || phone.isEmpty() || day == null || month == null || year == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Email invalide", "Veuillez entrer un email valide.");
            return;
        }

        LocalDate birthDate;
        try {
            birthDate = LocalDate.of(year, monthToNumber(month), day);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Date invalide", "La date de naissance est invalide.");
            return;
        }

        User newUser = new User();
        newUser.setPrenom(firstName);
        newUser.setNom(lastName);
        newUser.setEmail(email);
        newUser.setPasswordHash(password);
        newUser.setRole(UserRole.USER);
        newUser.setBirthDate(birthDate);
        newUser.setActive(true);

        // ✅ Try/catch ajouté ici
        boolean created = false;
        try {
            created = userService.registerUser(newUser);
            System.out.println(">>> registerUser retourne : " + created);
        } catch (Exception e) {
            System.out.println(">>> EXCEPTION : " + e.getMessage());
            e.printStackTrace();
        }

        if (created) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé avec succès !");
            ScreenManager.getInstance().navigateTo(Screen.signup2);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le compte. Vérifiez vos informations.");
        }
    }

    @FXML
    private void handleSeConnecter(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.mainView);
    }

    private boolean isValidEmail(String input) {
        return input.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private int monthToNumber(String month) {
        return switch (month) {
            case "Janvier"   -> 1;
            case "Février"   -> 2;
            case "Mars"      -> 3;
            case "Avril"     -> 4;
            case "Mai"       -> 5;
            case "Juin"      -> 6;
            case "Juillet"   -> 7;
            case "Août"      -> 8;
            case "Septembre" -> 9;
            case "Octobre"   -> 10;
            case "Novembre"  -> 11;
            case "Décembre"  -> 12;
            default          -> 1;
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
}