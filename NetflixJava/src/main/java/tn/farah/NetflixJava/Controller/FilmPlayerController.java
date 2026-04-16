/*package tn.farah.NetflixJava.Controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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
import tn.farah.NetflixJava.Entities.Subtitle;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.HistoryService;
import tn.farah.NetflixJava.Service.SubtitleService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class FilmPlayerController {

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
    @FXML private Label      subtitleLabel;
   
    private MediaPlayer     mediaPlayer;
    private FilmService     filmService;
    private SubtitleService subtitleService;
    private HistoryService  historyService;
    private Film            filmActuel;
    private double          currentRate = 1.0;
    private boolean         locked      = false;
    private int             userId;

    private ChangeListener<Duration> timeListener;
    private ChangeListener<Boolean>  valueChangingListener;
    private PauseTransition          hideTimer;

    private javafx.animation.Timeline autoSaveTimeline;
    private javafx.animation.Timeline subtitleTimeline;
    private List<SubtitleCue>         subtitleCues = new ArrayList<>();

    private static class SubtitleCue {
        double startSeconds, endSeconds;
        String text;
        SubtitleCue(double s, double e, String t) {
            startSeconds = s; endSeconds = e; text = t;
        }
    }

    // ─────────────────────────────────────────────
    // POINT D'ENTRÉE
    // ─────────────────────────────────────────────
    public void initFilm(Film film, int userId) {
        Connection connection = ConxDB.getInstance();
        filmService     = new FilmService(connection);
        historyService  = new HistoryService(connection);
        subtitleService = new SubtitleService(connection);
        this.userId     = userId;
        this.filmActuel = film;
        initialiserHideTimer();
        chargerFilm(film);
    }

    private void initialiserHideTimer() {
        controlsPane.setVisible(false);
        hideTimer = new PauseTransition(Duration.seconds(3));
        hideTimer.setOnFinished(e -> { if (!locked) controlsPane.setVisible(false); });
     // 
        rootPane.setOnMouseMoved(e -> {
            if (!locked) {
                controlsPane.setVisible(true);
                hideTimer.playFromStart();
            }
        });
    }

    // ─────────────────────────────────────────────
    // AUTO-SAVE
    // ─────────────────────────────────────────────
    private void demarrerAutoSave() {
        if (autoSaveTimeline != null) autoSaveTimeline.stop();
        autoSaveTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(10), e -> sauvegarderProgression(false))
        );
        autoSaveTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoSaveTimeline.play();
    }

    private void sauvegarderProgression(boolean forcer) {
        if (mediaPlayer == null || filmActuel == null || userId <= 0) return;
        if (!forcer && mediaPlayer.getStatus() == MediaPlayer.Status.UNKNOWN) return;
        int secondesActuelles = (int) mediaPlayer.getCurrentTime().toSeconds();
        int secondesTotales   = (int) mediaPlayer.getTotalDuration().toSeconds();
        boolean estTermine = secondesTotales > 0 && secondesActuelles >= secondesTotales * 0.95;
        historyService.saveProgressionFilm(userId, filmActuel.getId(), secondesActuelles, estTermine);
        System.out.println("✅ Progression sauvegardée : " + secondesActuelles + "s / " + secondesTotales + "s");
    }

    // ─────────────────────────────────────────────
    // CHARGEMENT DU FILM
    // ─────────────────────────────────────────────
    private void chargerFilm(Film film) {
        if (film == null) { System.err.println("Erreur : Film null."); return; }
        filmService.incrementVues(filmActuel.getId());
        Platform.runLater(() -> {
            titleLabel.setText(film.getTitre());
            String url = film.getUrlVedio();
            if (url != null && !url.isBlank()) {
                url = resoudreUrl(url);
                if (url != null) lancerVideo(url);
                else System.err.println("URL vidéo invalide pour : " + film.getTitre());
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
            if (timeListener != null) mediaPlayer.currentTimeProperty().removeListener(timeListener);
            if (valueChangingListener != null) progressBar.valueChangingProperty().removeListener(valueChangingListener);
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
                double cur = newTime.toSeconds();
                double tot = mediaPlayer.getTotalDuration().toSeconds();
                Platform.runLater(() -> {
                    if (!progressBar.isValueChanging())
                        progressBar.setValue(tot > 0 ? (cur / tot) * 100 : 0);
                    timeLabel.setText(formatTime(cur) + " / " + formatTime(tot));
                });
            };
            mediaPlayer.currentTimeProperty().addListener(timeListener);

            valueChangingListener = (obs, wasChanging, isChanging) -> { if (!isChanging) seekToPosition(); };
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

    // ─────────────────────────────────────────────
    // SOUS-TITRES – PARSER SRT
    // ─────────────────────────────────────────────
    private List<SubtitleCue> parseSrt(String url) {
        List<SubtitleCue> cues = new ArrayList<>();
        System.out.println("📂 Chargement SRT depuis : " + url);
        try {
            java.io.InputStream is;
            if (url.startsWith("http")) {
                is = new java.net.URL(url).openStream();
            } else {
                String path = url.replaceFirst("^file:/+", "/");
                if (path.matches("^/[A-Za-z]:/.*")) path = path.substring(1); // fix Windows
                is = new java.io.FileInputStream(path);
            }

            java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(is, "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\uFEFF")) line = line.substring(1); // BOM
                if (line.isEmpty() || line.matches("\\d+")) continue;

                if (line.contains("-->")) {
                    String[] times = line.split("-->");
                    double start = parseSrtTime(times[0].trim());
                    double end   = parseSrtTime(times[1].trim());

                    StringBuilder text = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) break;
                        if (text.length() > 0) text.append("\n");
                        text.append(line.replaceAll("<[^>]+>", "")); // strip HTML tags
                    }
                    if (text.length() > 0) cues.add(new SubtitleCue(start, end, text.toString()));
                }
            }
            br.close();
            System.out.println("✅ SRT parsé : " + cues.size() + " cues chargés");

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing SRT : " + e.getMessage());
        }
        return cues;
    }

    private double parseSrtTime(String t) {
        t = t.replace(',', '.');
        String[] parts = t.split(":");
        return Double.parseDouble(parts[0]) * 3600
             + Double.parseDouble(parts[1]) * 60
             + Double.parseDouble(parts[2]);
    }

    // ─────────────────────────────────────────────
    // SOUS-TITRES – TICKER
    // ✅ FIX : reçoit les cues en paramètre pour éviter que
    // stopSubtitleTicker() vide la liste avant que le ticker démarre
    // ─────────────────────────────────────────────
    private void startSubtitleTicker(List<SubtitleCue> cues) {
        // Stopper l'ancien ticker SANS vider subtitleCues
        if (subtitleTimeline != null) {
            subtitleTimeline.stop();
            subtitleTimeline = null;
        }
        // Assigner les nouveaux cues APRÈS le stop
        this.subtitleCues = cues;
        if (this.subtitleCues.isEmpty()) return;

        subtitleTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.millis(200), e -> {
                if (mediaPlayer == null || subtitleCues.isEmpty()) return;
                double now = mediaPlayer.getCurrentTime().toSeconds();
                subtitleCues.stream()
                    .filter(c -> now >= c.startSeconds && now <= c.endSeconds)
                    .findFirst()
                    .ifPresentOrElse(
                        c -> Platform.runLater(() -> {
                            subtitleLabel.setText(c.text);
                            subtitleLabel.setVisible(true);
                        }),
                        () -> Platform.runLater(() -> subtitleLabel.setVisible(false))
                    );
            })
        );
        subtitleTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        subtitleTimeline.play();
    }

    private void stopSubtitleTicker() {
        if (subtitleTimeline != null) {
            subtitleTimeline.stop();
            subtitleTimeline = null;
        }
        subtitleCues = new ArrayList<>();
        if (subtitleLabel != null) Platform.runLater(() -> subtitleLabel.setVisible(false));
    }

    // ─────────────────────────────────────────────
    // ACTIONS FXML
    // ─────────────────────────────────────────────
    @FXML
    private void handlePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause(); playButton.setText("▶");
        } else {
            mediaPlayer.play(); playButton.setText("⏸");
        }
    }

    @FXML private void rewind10() {
        if (mediaPlayer != null) mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
    }

    @FXML private void forward10() {
        if (mediaPlayer != null) mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
    }

    @FXML
    private void handleVitesse() {
        if (mediaPlayer == null) return;
        double[] vitesses = {0.5, 1.0, 1.25, 1.5, 2.0};
        int idx = 0;
        for (int i = 0; i < vitesses.length; i++) {
            if (Math.abs(vitesses[i] - currentRate) < 0.01) { idx = (i + 1) % vitesses.length; break; }
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
        if (locked) { hideTimer.stop(); controlsPane.setVisible(true); }
        else hideTimer.playFromStart();
    }

    @FXML
    private void handleAudioMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();
        try {
            List<Subtitle> subtitles = subtitleService.getSubtitlesForFilm(filmActuel.getId());
            if (subtitles.isEmpty()) {
                MenuItem none = new MenuItem("Aucun sous-titre disponible");
                none.setDisable(true);
                menu.getItems().add(none);
            } else {
                for (Subtitle sub : subtitles) {
                    MenuItem subItem = new MenuItem("📝  Sous-titres — " + sub.getLangage());
                    subItem.setOnAction(e -> {
                        // ✅ Parser en local, passer directement au ticker
                        List<SubtitleCue> cues = parseSrt(sub.getUrl());
                        if (cues.isEmpty()) {
                            System.err.println("Aucun cue trouvé dans : " + sub.getUrl());
                        } else {
                            startSubtitleTicker(cues);
                            System.out.println("Sous-titres activés : " + sub.getLangage()
                                + " (" + cues.size() + " cues)");
                        }
                    });
                    menu.getItems().add(subItem);
                }
            }
        } catch (Exception e) {
            MenuItem error = new MenuItem("⚠ Erreur de chargement");
            error.setDisable(true);
            menu.getItems().add(error);
            e.printStackTrace();
        }

        menu.getItems().add(new SeparatorMenuItem());
        MenuItem subOff = new MenuItem("⊘  Désactiver les sous-titres");
        subOff.setOnAction(e -> stopSubtitleTicker());
        menu.getItems().add(subOff);

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

    @FXML
    private void handleRetour() {
        sauvegarderProgression(true);
        shutdown();
        FilmViewController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.detailFilm);
        if (ctrl != null && filmActuel != null) ctrl.setFilm(filmActuel);
    }

    // ─────────────────────────────────────────────
    // UTILITAIRES
    // ─────────────────────────────────────────────
    private String resoudreUrl(String url) {
        if (url == null || url.isBlank()) return null;
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) return url;
        java.io.File f = new java.io.File(url);
        if (!f.isAbsolute()) f = new java.io.File(System.getProperty("user.dir"), url);
        if (f.exists()) return f.toURI().toString();
        java.net.URL resource = getClass().getResource("/" + url);
        if (resource != null) return resource.toExternalForm();
        System.err.println("Fichier vidéo introuvable : " + url);
        return null;
    }

    private String formatTime(double seconds) {
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) return "00:00";
        int h = (int) seconds / 3600;
        int m = ((int) seconds % 3600) / 60;
        int s = (int) seconds % 60;
        return h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    public void shutdown() {
        if (autoSaveTimeline != null) autoSaveTimeline.stop();
        stopSubtitleTicker();
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.dispose(); }
    }

    public void seekToSeconds(int seconds) {
        if (mediaPlayer == null || seconds <= 0) return;
        mediaPlayer.setOnReady(() -> mediaPlayer.seek(Duration.seconds(seconds)));
    }
}*/