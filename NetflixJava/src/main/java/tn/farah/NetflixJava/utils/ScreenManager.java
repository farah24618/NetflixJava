package tn.farah.NetflixJava.utils;



import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * ScreenManager — Singleton navigator for large JavaFX apps.
 *
 * Setup (call once in Main.java):
 *   ScreenManager.getInstance().init(primaryStage);
 *   ScreenManager.getInstance().register(Screen.LOGIN,    "/view/Login.fxml");
 *   ScreenManager.getInstance().register(Screen.DASHBOARD,"/view/Dashboard.fxml");
 *   ScreenManager.getInstance().navigateTo(Screen.LOGIN);
 *
 * From any controller — no Stage or event needed:
 *   ScreenManager.getInstance().navigateTo(Screen.DASHBOARD);
 *   ScreenManager.getInstance().goBack();
 */
public class ScreenManager {

    // ── Singleton ────────────────────────────────────────────────────────────
    private static ScreenManager instance;

    public static ScreenManager getInstance() {
        if (instance == null) instance = new ScreenManager();
        
        return instance;
    }

    private ScreenManager() {}

    // ── State ────────────────────────────────────────────────────────────────
    private Stage              primaryStage;
    private final Map<Screen, String> routes     = new HashMap<>();
    private final Stack<Screen>       history    = new Stack<>();
    private Screen                    current;

    // ── Init ─────────────────────────────────────────────────────────────────

    /** Call once in Main.start() with the primary stage. */
    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    /** Register a screen name → FXML path mapping. */
    public void register(Screen screen, String fxmlPath) {
        routes.put(screen, fxmlPath);
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    /** Navigate to a screen (adds current to back-stack). */
    public void navigateTo(Screen screen) {
        if (current != null) history.push(current);
        load(screen);
    }

    /** Navigate and replace current (no back entry — useful for Login → Dashboard). */
    public void navigateAndReplace(Screen screen) {
        history.clear();
        load(screen);
    }

    /** Go back to the previous screen. */
    public void goBack() {
        if (history.isEmpty()) return;
        load(history.pop());
    }

    /** Check if a back destination exists. */
    public boolean canGoBack() {
        return !history.isEmpty();
    }

    /** Get the currently active screen. */
    public Screen getCurrent() {
        return current;
    }

    // ── Load with controller access ───────────────────────────────────────────

    /**
     * Navigate and get the new controller so you can pass data.
     *
     * Example:
     *   DashboardController ctrl = ScreenManager.getInstance()
     *       .navigateAndGetController(Screen.DASHBOARD);
     *   ctrl.setUser(loggedInUser);
     */
    public <T> T navigateAndGetController(Screen screen) {
        if (current != null) history.push(current);
        return loadAndGetController(screen);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void load(Screen screen) {
        String path = routes.get(screen);
        if (path == null) throw new IllegalArgumentException("Screen not registered: " + screen);

        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            current = screen;
            applyScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private <T> T loadAndGetController(Screen screen) {
        String path = routes.get(screen);
        if (path == null) throw new IllegalArgumentException("Screen not registered: " + screen);

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
