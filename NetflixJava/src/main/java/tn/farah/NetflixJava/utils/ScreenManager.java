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

/**
 * ScreenManager : gestionnaire de navigation entre les interfaces JavaFX.
 */
public class ScreenManager {

    private static ScreenManager instance;

    private Stage primaryStage;
    private final Map<Screen, String> routes = new HashMap<>();
    private final Stack<Screen> history = new Stack<>();
    private Screen current;

    private ScreenManager() {
    }

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    /**
     * À appeler une seule fois dans Main.java
     */
    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Enregistrer une page avec son chemin FXML
     */
    public void register(Screen screen, String fxmlPath) {
        routes.put(screen, fxmlPath);
    }

    /**
     * Aller vers une page et garder l’ancienne dans l’historique
     */
    public void navigateTo(Screen screen) {
        if (current != null) {
            history.push(current);
        }
        load(screen);
    }

    /**
     * Aller vers une page sans garder l’historique
     */
    public void navigateAndReplace(Screen screen) {
        history.clear();
        load(screen);
    }

    /**
     * Retour arrière
     */
    public void goBack() {
        if (history.isEmpty()) {
            return;
        }
        load(history.pop());
    }

    /**
     * Vérifier s’il y a une page précédente
     */
    public boolean canGoBack() {
        return !history.isEmpty();
    }

    /**
     * Retourne la page actuelle
     */
    public Screen getCurrent() {
        return current;
    }

    /**
     * Naviguer et récupérer le contrôleur de la nouvelle page
     */
    public <T> T navigateAndGetController(Screen screen) {
        if (current != null) {
            history.push(current);
        }
        return loadAndGetController(screen);
    }

    /**
     * Chargement simple
     */
    private void load(Screen screen) {
        String path = routes.get(screen);

        if (path == null) {
            throw new IllegalArgumentException("Screen non enregistrée : " + screen);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            current = screen;
            applyScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chargement avec récupération du contrôleur
     */
    private <T> T loadAndGetController(Screen screen) {
        String path = routes.get(screen);

        if (path == null) {
            throw new IllegalArgumentException("Screen non enregistrée : " + screen);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            current = screen;
            applyScene(root);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Appliquer la nouvelle scène avec une petite animation
     */
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

        ParallelTransition transition = new ParallelTransition(fade, scale);
        transition.play();
    }
}
