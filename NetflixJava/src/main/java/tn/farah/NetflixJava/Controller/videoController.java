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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.Service.HistoryService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class videoController {

    // ─────────────────────────────────────────────
    // INJECTION FXML
    // ─────────────────────────────────────────────
    @FXML private MediaView  mediaView;
    @FXML private Button     playButton;
    @FXML private Button     skipIntroButton;
    @FXML private Button     vitesseButton;
    @FXML private Button     lockButton;
    @FXML private Button     fullscreenButton;
    @FXML private Slider     progressBar;
    @FXML private Label      timeLabel;
    @FXML private Label      titleLabel;
    @FXML private BorderPane controlsPane;

    // ─────────────────────────────────────────────
    // ÉTAT INTERNE
    // ─────────────────────────────────────────────
    private MediaPlayer    mediaPlayer;
    private EpisodeService episodeService;
    private SerieService   serieService;
    private Episode        currentEpisode;
    private double         currentRate = 1.0;
    private boolean        locked      = false;

    private ChangeListener<Duration> timeListener;
    private ChangeListener<Boolean>  valueChangingListener;
    private PauseTransition          hideTimer;
    private HistoryService historyService;
    private int userId;
    private javafx.animation.Timeline autoSaveTimeline;


    // ─────────────────────────────────────────────
    // POINT D'ENTRÉE
    // ─────────────────────────────────────────────
    public void initEpisode(int episodeId,int userId) {
        Connection connection = ConxDB.getInstance();
        episodeService = new EpisodeService(connection);
        serieService   = new SerieService(connection);
        historyService = new HistoryService(connection);  // ← AJOUT
        this.userId    = userId;     
        initialiserHideTimer();
        chargerEpisode(episodeId);
    }

    // ─────────────────────────────────────────────
    // AUTO-MASQUAGE DES CONTRÔLES
    // ─────────────────────────────────────────────
    private void initialiserHideTimer() {

        controlsPane.setVisible(false);

        hideTimer = new PauseTransition(Duration.seconds(3));
        hideTimer.setOnFinished(e -> {
            if (!locked) controlsPane.setVisible(false);
        });

        mediaView.setOnMouseMoved(e -> {
            controlsPane.setVisible(true);
            hideTimer.playFromStart();
        });

        controlsPane.setOnMouseEntered(e -> hideTimer.stop());

        controlsPane.setOnMouseExited(e -> {
            if (!locked) hideTimer.playFromStart();
        });
    }

    // ─────────────────────────────────────────────
    // CHARGEMENT ÉPISODE
    // ─────────────────────────────────────────────
    private void chargerEpisode(int episodeId) {
        this.currentEpisode = episodeService.findById(episodeId);
        		

        if (currentEpisode != null) {
            Platform.runLater(() -> {
                titleLabel.setText(
                        "Saison " + currentEpisode.getSaisonId()
                        + " – Ép " + currentEpisode.getNumeroEpisode()
                        + " : " + currentEpisode.getTitre());
                String url = currentEpisode.getVideoUrl();
                if (url != null && !url.isBlank()) {
                    url = resoudreUrl(url);
                    if (url != null) lancerVideo(url);
                    else System.err.println("URL vidéo invalide pour épisode " + currentEpisode.getId());
                } else {
                    System.err.println("Pas d'URL vidéo pour l'épisode " + currentEpisode.getId());
                }
            });
        } else {
            System.err.println("Erreur : Épisode " + episodeId + " introuvable.");
        }
        
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
                    timeLabel.setText(formatTime(currentSeconds) + " / " + formatTime(totalSeconds));

                    boolean dansIntro = currentSeconds >= 0
                            && currentSeconds < currentEpisode.getDurreeIntro()
                            && currentEpisode.getDurreeIntro() > 0;
                    boolean afficher = dansIntro && !locked;
                    skipIntroButton.setVisible(afficher);
                    skipIntroButton.setManaged(afficher);
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
            demarrerAutoSave();  

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
    private void demarrerAutoSave() {
        if (autoSaveTimeline != null) autoSaveTimeline.stop();

        autoSaveTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                Duration.seconds(10),  // sauvegarde toutes les 10 secondes
                e -> sauvegarderProgression(false)
            )
        );
        autoSaveTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoSaveTimeline.play();
    }
    private void sauvegarderProgression(boolean forcer) {
        if (mediaPlayer == null || currentEpisode == null || userId <= 0) return;

        MediaPlayer.Status status = mediaPlayer.getStatus();
        if (!forcer && status == MediaPlayer.Status.UNKNOWN) return;

        int secondesActuelles = (int) mediaPlayer.getCurrentTime().toSeconds();
        int secondesTotales   = (int) mediaPlayer.getTotalDuration().toSeconds();

        // Terminé si 95%+ regardé
        boolean estTermine = secondesTotales > 0
                && secondesActuelles >= secondesTotales * 0.95;

        historyService.saveProgressionEpisode(
                userId,
                currentEpisode.getId(),
                secondesActuelles,
                estTermine
        );

        System.out.println("✅ Épisode sauvegardé : " + secondesActuelles + "s / " + secondesTotales + "s");
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
    private void handleSkipIntro() {
        if (mediaPlayer != null && currentEpisode != null) {
            mediaPlayer.seek(Duration.seconds(currentEpisode.getDurreeIntro()));
            skipIntroButton.setVisible(false);
            skipIntroButton.setManaged(false);
        }
    }

    @FXML
    private void handleNextEpisode() {
        if (currentEpisode == null) return;
        
        sauvegarderProgression(true);
        Episode next = episodeService.getNextEpisode(
                currentEpisode.getSaisonId(),
                currentEpisode.getNumeroEpisode());
        if (next != null) {
            chargerEpisode(next.getId());
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin de saison");
            alert.setHeaderText(null);
            alert.setContentText("Vous avez terminé tous les épisodes de cette saison !");
            alert.showAndWait();
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
        vitesseButton.setText(currentRate == 1.0 ? "⚡ Vitesse" : "⚡ x" + currentRate);
    }

    @FXML
    private void handleVerrouiller() {
        locked = !locked;
        playButton.setDisable(locked);
        progressBar.setDisable(locked);
        skipIntroButton.setDisable(locked);
        lockButton.setText(locked ? "🔒 Verrouillé" : "🔓 Verrouiller");

        if (locked) {
            hideTimer.stop();
            controlsPane.setVisible(true);
        } else {
            hideTimer.playFromStart();
        }
    }

    /**
     * ✅ FIX : Retourne à la fiche de la série (EpisodeViewController)
     * en rechargeant la série correspondante à l'épisode courant.
     */
    @FXML
    private void handleOpenEpisodes() {
    	sauvegarderProgression(true);
        shutdown();
        retournerALaSerie();
    }

    /**
     * ✅ FIX : Bouton retour — arrête la vidéo et retourne à la fiche série.
     */
    /**
     * Convertit une URL en URI valide pour JavaFX Media.
     */
    private String resoudreUrl(String url) {
        if (url == null || url.isBlank()) return null;
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) {
            return url;
        }
        java.io.File f = new java.io.File(url);
        if (!f.isAbsolute()) {
            f = new java.io.File(System.getProperty("user.dir"), url);
        }
        if (f.exists()) {
            return f.toURI().toString();
        }
        java.net.URL resource = getClass().getResource("/" + url);
        if (resource != null) {
            return resource.toExternalForm();
        }
        System.err.println("Fichier vidéo introuvable : " + url);
        return null;
    }

    @FXML
    private void handleRetour() {
    	sauvegarderProgression(true);
        shutdown();
        retournerALaSerie();
    }

    /**
     * Navigation vers EpisodeViewController avec la série de l'épisode courant.
     */
    private void retournerALaSerie() {
        if (currentEpisode == null) {
            ScreenManager.getInstance().navigateTo(Screen.series);
            return;
        }
        // Récupérer la série via la saison de l'épisode
        Serie serie = serieService != null
            ? serieService.findByEpisodeId(currentEpisode.getId())
            : null;

        EpisodeViewController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.detail);

        if (ctrl != null && serie != null) {
            ctrl.setSerie(serie);
        } else if (ctrl == null) {
            // fallback si navigateAndGetController retourne null
            ScreenManager.getInstance().navigateTo(Screen.detail);
        }
    }

    @FXML
    private void handleFullscreen() {
        if (mediaView.getScene() == null) return;
        Stage stage = (Stage) mediaView.getScene().getWindow();
        boolean isFullscreen = stage.isFullScreen();
        stage.setFullScreen(!isFullscreen);
        fullscreenButton.setText(isFullscreen ? "⛶ Plein écran" : "⊡ Fenêtré");
    }

    @FXML
    private void handleAudioMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem fr     = new MenuItem("🇫🇷  Français (Audio)");
        MenuItem en     = new MenuItem("🇬🇧  Anglais (Audio)");
        MenuItem subFr  = new MenuItem("📝  Sous-titres Français");
        MenuItem subOff = new MenuItem("⊘  Désactiver les sous-titres");

        fr.setOnAction(e     -> System.out.println("Audio : Français"));
        en.setOnAction(e     -> System.out.println("Audio : Anglais"));
        subFr.setOnAction(e  -> System.out.println("Sous-titres : Français"));
        subOff.setOnAction(e -> System.out.println("Sous-titres désactivés"));

        menu.getItems().addAll(fr, en, new SeparatorMenuItem(), subFr, subOff);

        Button btn = (Button) event.getSource();
        menu.show(btn, Side.TOP, 0, 0);
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
    	if (autoSaveTimeline != null) autoSaveTimeline.stop(); 
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