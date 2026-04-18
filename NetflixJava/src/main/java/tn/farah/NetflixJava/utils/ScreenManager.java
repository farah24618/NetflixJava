

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


public class ScreenManager {

    private static ScreenManager instance;

    private Stage primaryStage;
    private final Map<Screen, String> routes = new HashMap<>();
 
    private final Stack<HistoryNode> history = new Stack<>();
    private Screen current;
    private Episode editingEpisode = null;
    private Saison editingSaison = null;
 
    private int selectedSaisonId = -1;

    public int getSelectedSaisonId() { return selectedSaisonId; }
    public void setSelectedSaisonId(int id) { this.selectedSaisonId = id; }
    
    private Film editingFilm = null;
 

    public void setEditingFilm(Film film) {
        this.editingFilm = film;
    }
 
    public Film getEditingFilm() {
        return editingFilm;
    }
 

    public void clearEditingFilm() {
        this.editingFilm = null;
    }
   
    private Serie editingSerie = null;
 
    public void setEditingSerie(Serie serie) {
        this.editingSerie = serie;
    }
 
    public Serie getEditingSerie() {
        return editingSerie;
    }
 
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

 
    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    public void register(Screen screen, String fxmlPath) {
        routes.put(screen, fxmlPath);
    }

    public void navigateTo(Screen screen) {
        if (current != null && primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
            history.push(new HistoryNode(current, primaryStage.getScene().getRoot()));
        }
        load(screen);
    }

    public <T> T navigateAndGetController(Screen screen) {
        if (current != null && primaryStage.getScene() != null && primaryStage.getScene().getRoot() != null) {
            history.push(new HistoryNode(current, primaryStage.getScene().getRoot()));
        }
        return loadAndGetController(screen);
    }

    public void navigateAndReplace(Screen screen) {
        history.clear();
        load(screen);
    }
  
    public void goBack() {
        if (history.isEmpty()) {
            return;
        }
        HistoryNode prev = history.pop();
        current = prev.screen;
        
        
        applyScene(prev.root); 
    }
 
    public boolean canGoBack() {
        return !history.isEmpty();
    }

    public Screen getCurrent() {
        return current;
    }

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
  

    private static class HistoryNode {
        Screen screen;
        Parent root; 

        public HistoryNode(Screen screen, Parent root) {
            this.screen = screen;
            this.root = root;
        }
    }

    
}

