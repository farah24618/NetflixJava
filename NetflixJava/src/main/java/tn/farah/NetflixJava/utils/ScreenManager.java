

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
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Serie;
import javafx.scene.image.Image;

/**
 * ScreenManager : gestionnaire de navigation entre les interfaces JavaFX.
 */
public class ScreenManager {

    private static ScreenManager instance;

    private Stage primaryStage;
    private final Map<Screen, String> routes = new HashMap<>();
 
    private final Stack<HistoryNode> history = new Stack<>();
    private Screen current;
    private Episode editingEpisode = null;
    private Saison editingSaison = null;
    
    // ★ Film being edited — null means "Add new film" mode
    private Film editingFilm = null;
 
    /** Call this BEFORE navigating to add_film to enter Edit mode. */
    public void setEditingFilm(Film film) {
        this.editingFilm = film;
    }
 
    /** Called by AddFilmController in initialize() to check the mode. */
    public Film getEditingFilm() {
        return editingFilm;
    }
 
    /** Call this after the edit is saved / cancelled to reset the mode. */
    public void clearEditingFilm() {
        this.editingFilm = null;
    }
    //*****Serie
    // ★ Film being edited — null means "Add new film" mode
    private Serie editingSerie = null;
 
    /** Call this BEFORE navigating to add_film to enter Edit mode. */
    public void setEditingSerie(Serie serie) {
        this.editingSerie = serie;
    }
 
    /** Called by AddFilmController in initialize() to check the mode. */
    public Serie getEditingSerie() {
        return editingSerie;
    }
 
    /** Call this after the edit is saved / cancelled to reset the mode. */
    public void clearEditingSerie() {
        this.editingSerie = null;
    }
    public Episode getEditingEpisode() { return editingEpisode; }
    public void setEditingEpisode(Episode episode) { this.editingEpisode = episode; }
    public Saison getEditingSaison() { return editingSaison; }
    public void setEditingSaison(Saison s) { this.editingSaison = s; }
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
     * Aller vers une page et garder l'ancienne DANS SON ÉTAT EXACT dans l'historique
     */
    public void navigateTo(Screen screen) {
        if (current != null && primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
            history.push(new HistoryNode(current, primaryStage.getScene().getRoot()));
        }
        load(screen);
    }

    /**
     * Naviguer et récupérer le contrôleur (Utilisé quand vous cliquez sur un épisode)
     */
    public <T> T navigateAndGetController(Screen screen) {
        if (current != null && primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
            history.push(new HistoryNode(current, primaryStage.getScene().getRoot()));
        }
        return loadAndGetController(screen);
    }

    /**
     * Aller vers une page sans garder l'historique
     */
    public void navigateAndReplace(Screen screen) {
        history.clear();
        load(screen);
    }
    /**
     * Retour arrière parfait
     */
    public void goBack() {
        if (history.isEmpty()) {
            return;
        }
        HistoryNode prev = history.pop();
        current = prev.screen;
        
        // On NE fait PLUS load(), on réapplique directement la racine JavaFX mise en cache !
        applyScene(prev.root); 
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
   /* public void navigateWithSplash(Screen screen) {
        if (current != null) history.push(current);

       
       Image logoImg = new Image(
        	    ScreenManager.class.getResource("/tn/farah/NetflixJava/ImagesNet/rakchanetLogo.png").toExternalForm()
        	);
        	javafx.scene.image.ImageView logo = new ImageView(logoImg);
        	logo.setFitWidth(700);
        	logo.setPreserveRatio(true);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #E50914;");
        spinner.setPrefSize(50, 50);

        Label loading = new Label("Chargement...");
        loading.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");

        VBox splashRoot = new VBox(30, logo, spinner, loading);
        splashRoot.setAlignment(javafx.geometry.Pos.CENTER);
        splashRoot.setStyle("-fx-background-color: #141414;");
        splashRoot.setPrefSize(1280, 720);

        // 2. Afficher le splash IMMÉDIATEMENT sur le JavaFX thread
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(splashRoot, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(splashRoot);
        }
        primaryStage.show();

        // 3. Charger le FXML dans un thread séparé (ne bloque plus l'UI)
        String path = routes.get(screen);
        Thread loadThread = new Thread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Parent root = loader.load(); // ← chargement lourd ici, hors JavaFX thread

                // 4. Une fois prêt, revenir sur le JavaFX thread pour afficher
                javafx.application.Platform.runLater(() -> {
                    current = screen;
                    applyScene(root);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        loadThread.setDaemon(true); // ← le thread s'arrête quand l'app se ferme
        loadThread.start();
    }*/
 // Ajoutez cette classe interne
    private static class HistoryNode {
        Screen screen;
        Parent root; // Contient toute l'interface (scroll, champs de texte, résultats, etc.)

        public HistoryNode(Screen screen, Parent root) {
            this.screen = screen;
            this.root = root;
        }
    }

    
}

