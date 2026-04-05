package tn.farah.NetflixJava.Controller;

import java.sql.Connection;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class FilmPlayerController {

    // ─────────────────────────────────────────────
    // INJECTION FXML
    // ─────────────────────────────────────────────
    @FXML private MediaView  mediaView;
    @FXML private Button     playButton;
    @FXML private Button     vitesseButton;
    @FXML private Button     lockButton;
    @FXML private Button     fullscreenButton;
    @FXML private Slider     progressBar;
    @FXML private Label      timeLabel;
    @FXML private Label      titleLabel;
    @FXML private BorderPane controlsPane;
    @FXML private StackPane  rootPane;

    // ─────────────────────────────────────────────
    // ÉTAT INTERNE
    // ─────────────────────────────────────────────
    private MediaPlayer  mediaPlayer;
    private FilmService  filmService;
    private Film         filmActuel;
    private double       currentRate = 1.0;
    private boolean      locked      = false;

    private ChangeListener<Duration> timeListener;
    private ChangeListener<Boolean>  valueChangingListener;
    private PauseTransition          hideTimer;

    // ─────────────────────────────────────────────
    // POINT D'ENTRÉE
    // ─────────────────────────────────────────────
    public void initFilm(Film film) {
        Connection connection = ConxDB.getInstance();
        filmService = new FilmService(connection);

        this.filmActuel = film;
        initialiserHideTimer();
        chargerFilm(film);
    }

    // ─────────────────────────────────────────────
    // HIDE TIMER
    // ─────────────────────────────────────────────
    private void initialiserHideTimer() {

        controlsPane.setVisible(false);

        hideTimer = new PauseTransition(Duration.seconds(3));
        hideTimer.setOnFinished(e -> {
            if (!locked) controlsPane.setVisible(false);
        });

        rootPane.setOnMouseMoved(e -> {
            controlsPane.setVisible(true);
            hideTimer.playFromStart();
        });

        rootPane.setOnMouseClicked(e -> {
            controlsPane.setVisible(true);
            hideTimer.playFromStart();
        });

        controlsPane.setOnMouseEntered(e -> hideTimer.stop());

        controlsPane.setOnMouseExited(e -> {
            if (!locked) hideTimer.playFromStart();
        });
    }

    // ─────────────────────────────────────────────
    // CHARGEMENT DU FILM
    // ─────────────────────────────────────────────
    private void chargerFilm(Film film) {
        if (film == null) {
            System.err.println("Erreur : Film null.");
            return;
        }
        Platform.runLater(() -> {
            titleLabel.setText(film.getTitre());
            String url = film.getUrlVedio();
            if (url != null && !url.isBlank()) {
                // Convertir un chemin relatif en URI absolue si nécessaire
                url = resoudreUrl(url);
                if (url != null) {
                    lancerVideo(url);
                } else {
                    System.err.println("URL vidéo invalide pour : " + film.getTitre());
                }
            } else {
                System.err.println("Erreur : URL vidéo introuvable pour le film " + film.getTitre());
            }
        });
    }

    // ─────────────────────────────────────────────
    // LECTEUR VIDÉO
    // ─────────────────────────────────────────────
    private void lancerVideo(String url) {
        if (mediaPlayer != null) {
            if (timeListener != null)
                mediaPlayer.currentTimeProperty().removeListener(timeListener);
            if (valueChangingListener != null)
                progressBar.valueChangingProperty().removeListener(valueChangingListener);
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            Platform.runLater(() -> {
                if (mediaView.getScene() != null) {
                    mediaView.fitWidthProperty().bind(mediaView.getScene().widthProperty());
                    mediaView.fitHeightProperty().bind(mediaView.getScene().heightProperty());
                    mediaView.setPreserveRatio(false);
                }
            });

            timeListener = (obs, oldTime, newTime) -> {
                double currentSeconds = newTime.toSeconds();
                double totalSeconds   = mediaPlayer.getTotalDuration().toSeconds();

                Platform.runLater(() -> {
                    if (!progressBar.isValueChanging()) {
                        progressBar.setValue(
                                totalSeconds > 0 ? (currentSeconds / totalSeconds) * 100 : 0);
                    }
                    timeLabel.setText(
                            formatTime(currentSeconds) + " / " + formatTime(totalSeconds));
                });
            };
            mediaPlayer.currentTimeProperty().addListener(timeListener);

            valueChangingListener = (obs, wasChanging, isChanging) -> {
                if (!isChanging) seekToPosition();
            };
            progressBar.valueChangingProperty().addListener(valueChangingListener);
            progressBar.setOnMouseClicked(e -> seekToPosition());

            mediaPlayer.setRate(currentRate);
            mediaPlayer.play();
            playButton.setText("⏸");

        } catch (Exception e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
        }
    }

    private void seekToPosition() {
        if (mediaPlayer == null) return;
        double total  = mediaPlayer.getTotalDuration().toSeconds();
        double seekTo = (progressBar.getValue() / 100.0) * total;
        mediaPlayer.seek(Duration.seconds(seekTo));
    }

    // ─────────────────────────────────────────────
    // ACTIONS FXML
    // ─────────────────────────────────────────────

    @FXML
    private void handlePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playButton.setText("▶");
        } else {
            mediaPlayer.play();
            playButton.setText("⏸");
        }
    }

    @FXML
    private void rewind10() {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
    }

    @FXML
    private void forward10() {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
    }

    @FXML
    private void handleVitesse() {
        if (mediaPlayer == null) return;
        double[] vitesses = {0.5, 1.0, 1.25, 1.5, 2.0};
        int idx = 0;
        for (int i = 0; i < vitesses.length; i++) {
            if (Math.abs(vitesses[i] - currentRate) < 0.01) {
                idx = (i + 1) % vitesses.length;
                break;
            }
        }
        currentRate = vitesses[idx];
        mediaPlayer.setRate(currentRate);
        vitesseButton.setText(currentRate == 1.0 ? "Vitesse" : "x" + currentRate);
    }

    @FXML
    private void handleVerrouiller() {
        locked = !locked;
        playButton.setDisable(locked);
        progressBar.setDisable(locked);
        lockButton.setText(locked ? "🔒 Verrouillé" : "🔓 Verrouiller");

        if (locked) {
            hideTimer.stop();
            controlsPane.setVisible(true);
        } else {
            hideTimer.playFromStart();
        }
    }

    @FXML
    private void handleAudioMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem fr     = new MenuItem("🇫🇷  Français (Audio)");
        MenuItem en     = new MenuItem("🇬🇧  Anglais (Audio)");
        MenuItem subOff = new MenuItem("⊘  Désactiver les sous-titres");
        MenuItem subFr  = new MenuItem("📝  Sous-titres Français");

        fr.setOnAction(e     -> System.out.println("Audio : Français"));
        en.setOnAction(e     -> System.out.println("Audio : Anglais"));
        subOff.setOnAction(e -> System.out.println("Sous-titres désactivés"));
        subFr.setOnAction(e  -> System.out.println("Sous-titres : Français"));

        menu.getItems().addAll(fr, en, new SeparatorMenuItem(), subFr, subOff);

        Button btn = (Button) event.getSource();
        menu.show(btn, Side.TOP, 0, 0);
    }

    @FXML
    private void handleFullscreen() {
        if (mediaView.getScene() == null) return;
        Stage stage = (Stage) mediaView.getScene().getWindow();
        boolean isFullscreen = stage.isFullScreen();
        stage.setFullScreen(!isFullscreen);
        fullscreenButton.setText(isFullscreen ? "⛶ Plein écran" : "⊡ Fenêtré");
    }

    /**
     * ✅ FIX : Arrête la vidéo proprement et retourne à la fiche film (detailFilm).
     */
    /**
     * Convertit une URL en URI valide pour JavaFX Media.
     * - Si l'URL commence par http/https → utilisée telle quelle
     * - Si l'URL est un chemin relatif (ex: videos/film.mp4) → converti en file:///...
     * - Si le fichier n'existe pas comme chemin absolu → cherche dans le répertoire courant
     */
    private String resoudreUrl(String url) {
        if (url == null || url.isBlank()) return null;
        // Déjà une URI absolue (http, https, file)
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) {
            return url;
        }
        // Chemin relatif → convertir en file URI
        java.io.File f = new java.io.File(url);
        if (!f.isAbsolute()) {
            // Chercher depuis le répertoire de travail courant
            f = new java.io.File(System.getProperty("user.dir"), url);
        }
        if (f.exists()) {
            return f.toURI().toString();
        }
        // Essayer comme ressource classpath
        java.net.URL resource = getClass().getResource("/" + url);
        if (resource != null) {
            return resource.toExternalForm();
        }
        System.err.println("Fichier vidéo introuvable : " + url);
        return null;
    }

    @FXML
    private void handleRetour() {
        shutdown();
        // ✅ Navigation vers la fiche du film (FilmViewController)
        FilmViewController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.detailFilm);
        if (ctrl != null && filmActuel != null) {
            ctrl.setFilm(filmActuel);
        }
    }

    // ─────────────────────────────────────────────
    // UTILITAIRES
    // ─────────────────────────────────────────────
    private String formatTime(double seconds) {
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) return "00:00";
        int h = (int) seconds / 3600;
        int m = ((int) seconds % 3600) / 60;
        int s = (int) seconds % 60;
        return h > 0
                ? String.format("%02d:%02d:%02d", h, m, s)
                : String.format("%02d:%02d", m, s);
    }

    public void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    public void seekToSeconds(int seconds) {
        if (mediaPlayer == null || seconds <= 0) return;
        mediaPlayer.setOnReady(() -> mediaPlayer.seek(Duration.seconds(seconds)));
    }
}