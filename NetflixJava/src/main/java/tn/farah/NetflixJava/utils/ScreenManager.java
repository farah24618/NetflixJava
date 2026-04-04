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
 * Gestionnaire de navigation fluide pour l'application Netflix
 */
public class ScreenManager {

    private static ScreenManager instance;
    private Stage primaryStage;
    private final Map<Screen, String> routes = new HashMap<>();
    private final Stack<Screen> history = new Stack<>();
    private Screen currentScreen;
    
    // Garde en mémoire le dernier loader pour extraire le contrôleur
    private FXMLLoader lastLoader;

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

    /**
     * Enregistre un écran et son chemin FXML associé
     * (Corrige l'erreur dans Main.java)
     */
    public void register(Screen screen, String fxmlPath) {
        routes.put(screen, fxmlPath);
    }

    /**
     * Récupère le contrôleur de la page chargée
     * Le <T> permet un cast automatique vers CommentListController par exemple.
     * (Corrige l'erreur dans AdminMainController)
     */
    @SuppressWarnings("unchecked")
    public <T> T getController() {
        if (lastLoader == null) return null;
        return (T) lastLoader.getController();
    }

    public void navigateTo(Screen screen) {
        if (currentScreen != null) {
            history.push(currentScreen);
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
        if (path == null) {
            System.err.println("❌ Erreur : L'écran " + screen + " n'est pas enregistré !");
            return;
        }

        try {
            // Initialisation du Loader
            lastLoader = new FXMLLoader(getClass().getResource(path));
            Parent root = lastLoader.load();
            
            currentScreen = screen;
            applyScene(root);
            
            System.out.println("🚀 Navigation vers : " + screen);
        } catch (IOException e) {
            System.err.println("❌ Impossible de charger le fichier FXML : " + path);
            e.printStackTrace();
        }
    }

    private void applyScene(Parent root) {
        // Préparation de l'animation
        root.setOpacity(0);
        root.setScaleX(0.95);
        root.setScaleY(0.95);

        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root));
        } else {
            primaryStage.getScene().setRoot(root);
        }

        primaryStage.show();

        // Animation de transition (Style Netflix)
        FadeTransition fade = new FadeTransition(Duration.millis(300), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_IN);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), root);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale).play();
    }
}