package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class AddProfileController {

    @FXML
    private TextField nameField;

    @FXML
    private CheckBox kidCheckBox;

    @FXML
    public void handleContinue() {
        // Ici plus tard tu peux sauvegarder le profil si tu veux
        ScreenManager.getInstance().navigateTo(Screen.home);
    }

    @FXML
    public void handleCancel() {
        ScreenManager.getInstance().navigateTo(Screen.pofiles);
    }
}