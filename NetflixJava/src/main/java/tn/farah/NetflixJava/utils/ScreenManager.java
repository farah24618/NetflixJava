package tn.farah.NetflixJava.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ScreenManager {

    private static ScreenManager instance;
    private Stage primaryStage;
    private final Map<Screen, String> routes = new HashMap<>();
    private final Stack<Screen> history = new Stack<>();
    private Screen current;
    
    // --- AJOUT : Variable pour stocker le contrôleur actuel ---
    private Object currentController;

    private ScreenManager() {}

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    public void register(Screen screen, String fxmlPath) {
        routes.put(screen, fxmlPath);
    }

    /**
     * AJOUT : Permet de récupérer le contrôleur de la page affichée
     */
    public Object getController() {
        return currentController;
    }

    public void navigateTo(Screen screen) {
        if (current != null) {
            history.push(current);
        }
        load(screen);
    }

    public void navigateAndReplace(Screen screen) {
        history.clear();
        load(screen);
    }

    public void goBack() {
        if (history.isEmpty()) return;
        load(history.pop());
    }

    private void load(Screen screen) {
        String path = routes.get(screen);
        if (path == null) throw new IllegalArgumentException("Screen non enregistrée : " + screen);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            
            // --- CRUCIAL : On mémorise le contrôleur chargé ---
            this.currentController = loader.getController();
            
            current = screen;
            applyScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyScene(Parent root) {
        root.setOpacity(0);
        root.setScaleX(0.98);
        root.setScaleY(0.98);

        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root));
        } else {
            primaryStage.getScene().setRoot(root);
        }

        primaryStage.show();
        primaryStage.centerOnScreen();

        FadeTransition fade = new FadeTransition(Duration.millis(200), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), root);
        scale.setFromX(0.98);
        scale.setFromY(0.98);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale).play();
    }
}