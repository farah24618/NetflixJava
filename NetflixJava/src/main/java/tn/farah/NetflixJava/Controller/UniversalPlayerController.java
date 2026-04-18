package tn.farah.NetflixJava.Controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
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

import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Subtitle;
import tn.farah.NetflixJava.Service.*;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;


public class UniversalPlayerController {

    
    public enum Mode { FILM, EPISODE, TEASER }

    @FXML private StackPane  rootPane;
    @FXML private MediaView  mediaView;
    @FXML private BorderPane controlsPane;
    @FXML private Button     playButton;
    @FXML private Button     vitesseButton;
    @FXML private Button     lockButton;
    @FXML private Button     fullscreenButton;
    @FXML private Button     skipIntroButton;     // episode only
    @FXML private Button     episodesButton;      // episode only
    @FXML private Button     nextEpisodeButton;   // episode only
    @FXML private Button     ficheButton;         // film / teaser only
    @FXML private Slider     progressBar;
    @FXML private Label      timeLabel;
    @FXML private Label      titleLabel;
    @FXML private Label      subtitleLabel;

    private Mode           mode;
    private MediaPlayer    mediaPlayer;
    private double         currentRate = 1.0;
    private boolean        locked      = false;
    private int            userId;

    private Film    filmActuel;
    private Episode currentEpisode;
    private FilmService     filmService;
    private EpisodeService  episodeService;
    private SerieService    serieService;
    private SaisonService   saisonService;
    private HistoryService  historyService;
    private SubtitleService subtitleService;

    private ChangeListener<Duration> timeListener;
    private ChangeListener<Boolean>  valueChangingListener;
    private PauseTransition          hideTimer;
    private Timeline                 autoSaveTimeline;
    private Timeline                 subtitleTimeline;
    private List<SubtitleCue>        subtitleCues = new ArrayList<>();

    private static class SubtitleCue {
        final double startSeconds, endSeconds;
        final String text;
        SubtitleCue(double s, double e, String t) {
            startSeconds = s; endSeconds = e; text = t;
        }
    }

    public void initFilm(Film film, int userId) {
        this.mode       = Mode.FILM;
        this.userId     = userId;
        this.filmActuel = film;
        initServices();
        configureToolbar();
        initialiserHideTimer();
        chargerFilm(film);
    }

    public void initEpisode(int episodeId, int userId) {
        this.mode   = Mode.EPISODE;
        this.userId = userId;
        initServices();
        configureToolbar();
        initialiserHideTimer();
        chargerEpisode(episodeId);
    }

    public void initTeaser(String videoUrl, String titre) {
        this.mode   = Mode.TEASER;
        this.userId = -1;
        initServices();
        configureToolbar();
        initialiserHideTimer();
        Platform.runLater(() -> {
            titleLabel.setText("🎞 Teaser — " + titre);
            String url = resoudreUrl(videoUrl);
            if (url != null) lancerVideo(url);
            else System.err.println("URL teaser invalide : " + videoUrl);
        });
    }

    private void configureToolbar() {
        boolean isEpisode = (mode == Mode.EPISODE);
        boolean isFilmOrTeaser = !isEpisode;

        setButton(skipIntroButton,    false, false); // shown later by time listener
        setButton(episodesButton,     isEpisode, isEpisode);
        setButton(nextEpisodeButton,  isEpisode, isEpisode);

        setButton(ficheButton, isFilmOrTeaser, isFilmOrTeaser);

        if (mode == Mode.TEASER) {
            setButton(lockButton, false, false);
        }
    }

    private void setButton(Button btn, boolean visible, boolean managed) {
        if (btn == null) return;
        btn.setVisible(visible);
        btn.setManaged(managed);
    }

    private void initServices() {
        Connection connection = ConxDB.getInstance();
        historyService  = new HistoryService(connection);
        subtitleService = new SubtitleService(connection);

        if (mode == Mode.FILM || mode == Mode.TEASER) {
            filmService = new FilmService(connection);
        }
        if (mode == Mode.EPISODE) {
            episodeService = new EpisodeService(connection);
            serieService   = new SerieService(connection);
            saisonService  = new SaisonService(connection);
        }
    }

    private void initialiserHideTimer() {
        controlsPane.setVisible(false);
        hideTimer = new PauseTransition(Duration.seconds(3));
        hideTimer.setOnFinished(e -> { if (!locked) controlsPane.setVisible(false); });

        rootPane.setOnMouseMoved(e -> {
            if (!locked) {
                controlsPane.setVisible(true);
                hideTimer.playFromStart();
            }
        });
    }

    private void chargerFilm(Film film) {
        if (film == null) { System.err.println("Film null."); return; }
        filmService.incrementVues(film.getId());
        Platform.runLater(() -> {
            titleLabel.setText(film.getTitre());
            String url = resoudreUrl(film.getUrlVedio());
            if (url != null) lancerVideo(url);
            else System.err.println("URL film invalide : " + film.getTitre());
        });
    }

    private void chargerEpisode(int episodeId) {
        this.currentEpisode = episodeService.findById(episodeId);
        if (currentEpisode == null) {
            System.err.println("Épisode introuvable : " + episodeId);
            
            return;}
            episodeService.incrementerVues(episodeId);
        
        Platform.runLater(() -> {
            titleLabel.setText(
                "Saison " + saisonService.findById(currentEpisode.getSaisonId()).getNumeroSaison()
                + " – Ép " + currentEpisode.getNumeroEpisode()
                + " : " + currentEpisode.getTitre());
            String url = resoudreUrl(currentEpisode.getVideoUrl());
            if (url != null) lancerVideo(url);
            else System.err.println("URL épisode invalide : " + currentEpisode.getId());
        });
    }

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
            mediaPlayer = new MediaPlayer(new Media(url));
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

                    // Skip-intro button (episode only)
                    if (mode == Mode.EPISODE && currentEpisode != null) {
                        boolean dansIntro = cur >= 0
                            && cur < currentEpisode.getDurreeIntro()
                            && currentEpisode.getDurreeIntro() > 0;
                        boolean show = dansIntro && !locked;
                        skipIntroButton.setVisible(show);
                        skipIntroButton.setManaged(show);
                    }
                });
            };
            mediaPlayer.currentTimeProperty().addListener(timeListener);

            valueChangingListener = (obs, was, is) -> { if (!is) seekToPosition(); };
            progressBar.valueChangingProperty().addListener(valueChangingListener);
            progressBar.setOnMouseClicked(e -> seekToPosition());

            mediaPlayer.setRate(currentRate);
            mediaPlayer.play();
            playButton.setText("⏸");

            if (mode != Mode.TEASER) demarrerAutoSave();

        } catch (Exception e) {
            System.err.println("Erreur lecture : " + e.getMessage());
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
        autoSaveTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), e -> sauvegarderProgression(false)));
        autoSaveTimeline.setCycleCount(Animation.INDEFINITE);
        autoSaveTimeline.play();
    }

    private void sauvegarderProgression(boolean forcer) {
        if (mediaPlayer == null || userId <= 0) return;
        if (!forcer && mediaPlayer.getStatus() == MediaPlayer.Status.UNKNOWN) return;

        int cur = (int) mediaPlayer.getCurrentTime().toSeconds();
        int tot = (int) mediaPlayer.getTotalDuration().toSeconds();
        boolean done = tot > 0 && cur >= tot * 0.95;

        if (mode == Mode.FILM && filmActuel != null) {
            historyService.saveProgressionFilm(userId, filmActuel.getId(), cur, done);
        } else if (mode == Mode.EPISODE && currentEpisode != null) {
            historyService.saveProgressionEpisode(userId, currentEpisode.getId(), cur, done);
        }
    }

    private List<SubtitleCue> parseSrt(String url) {
        List<SubtitleCue> cues = new ArrayList<>();
        try {
            java.io.InputStream is;
            if (url.startsWith("http")) {
                is = new java.net.URL(url).openStream();
            } else {
                String path = url.replaceFirst("^file:/+", "/").replace("%20", " ");
                if (path.matches("^/[A-Za-z]:/.*")) path = path.substring(1);
                is = new java.io.FileInputStream(new java.io.File(path));
            }
            java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(is, "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\uFEFF")) line = line.substring(1);
                if (line.isEmpty() || line.matches("\\d+")) continue;
                if (line.contains("-->")) {
                    String[] parts = line.split("-->");
                    double start = parseSrtTime(parts[0].trim());
                    double end   = parseSrtTime(parts[1].trim());
                    StringBuilder text = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) break;
                        if (text.length() > 0) text.append("\n");
                        text.append(line.replaceAll("<[^>]+>", ""));
                    }
                    if (text.length() > 0) cues.add(new SubtitleCue(start, end, text.toString()));
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("❌ SRT parse error: " + e.getMessage());
        }
        return cues;
    }

    private double parseSrtTime(String t) {
        t = t.replace(',', '.');
        String[] p = t.split(":");
        return Double.parseDouble(p[0]) * 3600
             + Double.parseDouble(p[1]) * 60
             + Double.parseDouble(p[2]);
    }

    private void startSubtitleTicker(List<SubtitleCue> cues) {
        if (subtitleTimeline != null) { subtitleTimeline.stop(); subtitleTimeline = null; }
        this.subtitleCues = cues;
        if (this.subtitleCues.isEmpty()) return;

        subtitleTimeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            if (mediaPlayer == null || subtitleCues.isEmpty()) return;
            double now = mediaPlayer.getCurrentTime().toSeconds();
            subtitleCues.stream()
                .filter(c -> now >= c.startSeconds && now <= c.endSeconds)
                .findFirst()
                .ifPresentOrElse(
                    c -> Platform.runLater(() -> { subtitleLabel.setText(c.text); subtitleLabel.setVisible(true); }),
                    ()  -> Platform.runLater(() -> subtitleLabel.setVisible(false))
                );
        }));
        subtitleTimeline.setCycleCount(Animation.INDEFINITE);
        subtitleTimeline.play();
    }

    private void stopSubtitleTicker() {
        if (subtitleTimeline != null) { subtitleTimeline.stop(); subtitleTimeline = null; }
        subtitleCues = new ArrayList<>();
        if (subtitleLabel != null) Platform.runLater(() -> subtitleLabel.setVisible(false));
    }


    @FXML
    private void handlePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause(); playButton.setText("▶");
        } else {
            mediaPlayer.play();  playButton.setText("⏸");
        }
    }

    @FXML private void rewind10() {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
    }

    @FXML private void forward10() {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
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
            currentEpisode.getSaisonId(), currentEpisode.getNumeroEpisode());
        if (next != null) {
            stopSubtitleTicker();
            chargerEpisode(next.getId());
        } else {
            new Alert(Alert.AlertType.INFORMATION,
                "Vous avez terminé tous les épisodes de cette saison !")
                .showAndWait();
        }
    }

    @FXML
    private void handleVitesse() {
        if (mediaPlayer == null) return;
        double[] speeds = {0.5, 1.0, 1.25, 1.5, 2.0};
        int idx = 0;
        for (int i = 0; i < speeds.length; i++) {
            if (Math.abs(speeds[i] - currentRate) < 0.01) { idx = (i + 1) % speeds.length; break; }
        }
        currentRate = speeds[idx];
        mediaPlayer.setRate(currentRate);
        vitesseButton.setText(currentRate == 1.0 ? "⚡ Vitesse" : "⚡ x" + currentRate);
    }

    @FXML
    private void handleVerrouiller() {
        locked = !locked;
        playButton.setDisable(locked);
        progressBar.setDisable(locked);
        if (mode == Mode.EPISODE) skipIntroButton.setDisable(locked);
        lockButton.setText(locked ? "🔒 Verrouillé" : "🔓 Verrouiller");
        if (locked) { hideTimer.stop(); controlsPane.setVisible(true); }
        else hideTimer.playFromStart();
    }

    @FXML
    private void handleAudioMenu(ActionEvent event) {
        ContextMenu menu = new ContextMenu();
        try {
            List<Subtitle> subtitles = mode == Mode.EPISODE
                ? subtitleService.getSubtitlesForEpisode(currentEpisode.getId())
                : subtitleService.getSubtitlesForFilm(filmActuel != null ? filmActuel.getId() : -1);

            if (subtitles.isEmpty()) {
                MenuItem none = new MenuItem("Aucun sous-titre disponible");
                none.setDisable(true);
                menu.getItems().add(none);
            } else {
                for (Subtitle sub : subtitles) {
                    MenuItem item = new MenuItem("📝  Sous-titres — " + sub.getLangage());
                    item.setOnAction(e -> {
                        List<SubtitleCue> cues = parseSrt(sub.getUrl());
                        if (!cues.isEmpty()) startSubtitleTicker(cues);
                        else System.err.println("Aucun cue : " + sub.getUrl());
                    });
                    menu.getItems().add(item);
                }
            }
        } catch (Exception e) {
            MenuItem err = new MenuItem("⚠ Erreur de chargement");
            err.setDisable(true);
            menu.getItems().add(err);
        }
        menu.getItems().add(new SeparatorMenuItem());
        MenuItem off = new MenuItem("⊘  Désactiver les sous-titres");
        off.setOnAction(e -> stopSubtitleTicker());
        menu.getItems().add(off);

        menu.show((Button) event.getSource(), Side.TOP, 0, 0);
    }

    @FXML
    private void handleOpenEpisodes() {
        sauvegarderProgression(true);
        shutdown();
        retournerALaSerie();
    }

    @FXML
    private void handleFullscreen() {
        if (mediaView.getScene() == null) return;
        Stage stage = (Stage) mediaView.getScene().getWindow();
        boolean fs = stage.isFullScreen();
        stage.setFullScreen(!fs);
        fullscreenButton.setText(fs ? "⛶ Plein écran" : "⊡ Fenêtré");
    }

    @FXML
    private void handleRetour() {
        sauvegarderProgression(true);
        shutdown();
        switch (mode) {
            case FILM -> {
                MediaViewController ctrl = ScreenManager.getInstance()
                    .navigateAndGetController(Screen.MediaView);
                if (ctrl != null && filmActuel != null) ctrl.setFilm(filmActuel);
            }
            case EPISODE -> retournerALaSerie();
            case TEASER  -> ScreenManager.getInstance().navigateTo(Screen.home);
        }
    }

    private void retournerALaSerie() {
        if (currentEpisode == null) { ScreenManager.getInstance().navigateTo(Screen.series); return; }
        Serie serie = serieService != null ? serieService.findByEpisodeId(currentEpisode.getId()) : null;
        MediaViewController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.MediaView);
        if (ctrl != null && serie != null) ctrl.setSerie(serie);
        else ScreenManager.getInstance().navigateTo(Screen.MediaView);
    }

    private String resoudreUrl(String url) {
        if (url == null || url.isBlank()) return null;
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:")) return url;
        java.io.File f = new java.io.File(url);
        if (!f.isAbsolute()) f = new java.io.File(System.getProperty("user.dir"), url);
        if (f.exists()) return f.toURI().toString();
        java.net.URL res = getClass().getResource("/" + url);
        return res != null ? res.toExternalForm() : null;
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
}
