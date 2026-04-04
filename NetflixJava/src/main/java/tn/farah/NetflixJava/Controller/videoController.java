package tn.farah.NetflixJava.Controller;

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
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class videoController {

    @FXML private MediaView mediaView;
    @FXML private Button playButton;
    @FXML private Button skipIntroButton;
    @FXML private Button vitesseButton;
    @FXML private Button lockButton;
    @FXML private Slider progressBar;
    @FXML private Label timeLabel;
    @FXML private Label titleLabel;
    @FXML private BorderPane controlsPane;

    private MediaPlayer mediaPlayer;
    private final EpisodeService episodeService = new EpisodeService();
    private Episode currentEpisode;
    private double currentRate = 1.0;
    private boolean locked = false;

    private ChangeListener<Duration> timeListener;
    private ChangeListener<Boolean> valueChangingListener;
    private PauseTransition hideTimer;

    // ===================== INITIALISATION =====================

    public void initEpisode(int episodeId) {
        chargerEpisode(episodeId);
        initialiserHideTimer();
    }

    private void initialiserHideTimer() {
        hideTimer = new PauseTransition(Duration.seconds(3));
        hideTimer.setOnFinished(e -> {
            if (!locked) controlsPane.setVisible(false);
        });

        mediaView.setOnMouseMoved(e -> {
            controlsPane.setVisible(true);
            hideTimer.playFromStart();
        });

        controlsPane.setOnMouseEntered(e -> hideTimer.stop());
        controlsPane.setOnMouseExited(e -> hideTimer.playFromStart());
    }

    private void chargerEpisode(int episodeId) {
        this.currentEpisode = episodeService.findById(episodeId);
        		

        if (currentEpisode != null) {
            Platform.runLater(() -> {
                titleLabel.setText("Saison " + currentEpisode.getSaisonId()
                        + " - Ép " + currentEpisode.getNumeroEpisode()
                        + " : " + currentEpisode.getTitre());
                lancerVideo(currentEpisode.getVideoUrl());
            });
        } else {
            System.err.println("Erreur : Épisode " + episodeId + " introuvable.");
        }
    }

    // ===================== GESTION DU LECTEUR =====================

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
                double totalSeconds = mediaPlayer.getTotalDuration().toSeconds();

                Platform.runLater(() -> {
                    if (!progressBar.isValueChanging()) {
                        progressBar.setValue(totalSeconds > 0 ? (currentSeconds / totalSeconds) * 100 : 0);
                    }
                    timeLabel.setText(formatTime(currentSeconds) + " / " + formatTime(totalSeconds));

                    // Afficher le bouton uniquement pendant l'intro et si non verrouillé
                    boolean dansIntro = currentSeconds >= 0
                            && currentSeconds < currentEpisode.getDurreeIntro()
                            && currentEpisode.getDurreeIntro() > 0;

                    boolean afficher = dansIntro && !locked;
                    skipIntroButton.setVisible(afficher);
                    skipIntroButton.setManaged(afficher); // évite l'espace vide quand caché
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
        if (mediaPlayer != null) {
            double total = mediaPlayer.getTotalDuration().toSeconds();
            double seekTo = (progressBar.getValue() / 100.0) * total;
            mediaPlayer.seek(Duration.seconds(seekTo));
        }
    }

    // ===================== ACTIONS FXML =====================

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
        Episode next = episodeService.getNextEpisode(
                currentEpisode.getSaisonId(),
                currentEpisode.getNumeroEpisode()
        );
        if (next != null) {
            chargerEpisode(next.getId());
        } else {
            System.out.println("Fin de la saison !");
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
        skipIntroButton.setDisable(locked);
        lockButton.setText(locked ? "🔒 Verrouillé" : "Verrouiller");

        // Cacher le bouton skip intro si on verrouille
        if (locked) {
            skipIntroButton.setVisible(false);
            skipIntroButton.setManaged(false);
        }
    }

    @FXML
    private void handleOpenEpisodes() {
        ScreenManager.getInstance().navigateTo(Screen.episodeView);
    }

    @FXML
    private void handleAudioMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();

        MenuItem fr = new MenuItem("Français (Audio)");
        MenuItem en = new MenuItem("Anglais (Audio)");
        MenuItem subOff = new MenuItem("Désactiver les sous-titres");

        fr.setOnAction(e -> System.out.println("Passage en Français"));
        en.setOnAction(e -> System.out.println("Passage en Anglais"));
        subOff.setOnAction(e -> System.out.println("Sous-titres désactivés"));

        menu.getItems().addAll(fr, en, new SeparatorMenuItem(), subOff);

        Button btn = (Button) event.getSource();
        menu.show(btn, Side.TOP, 0, 0);
    }

    // ===================== UTILITAIRES =====================

    private String formatTime(double seconds) {
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) return "00:00";
        int h = (int) seconds / 3600;
        int m = ((int) seconds % 3600) / 60;
        int s = (int) seconds % 60;
        return h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    public void shutdown() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}