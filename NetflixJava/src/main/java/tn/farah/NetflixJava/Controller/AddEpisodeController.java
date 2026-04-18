package tn.farah.NetflixJava.Controller;

import javafx.animation.KeyFrame;


import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Subtitle;
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.Service.SubtitleService;
import tn.farah.NetflixJava.DAO.SubtitleDAO;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddEpisodeController implements Initializable {

    
    private EpisodeService  episodeService;
    private SaisonService   saisonService;
    private SubtitleService subtitleService;
    private NotificationService notificationService;

    
    private File fileMiniature;
    private File fileVideo;
    private long videoDurationSeconds = -1;
    private final List<SubtitleRow> subtitleRows = new ArrayList<>();
    @FXML private TextField        txtTitre;
    @FXML private TextArea         txtResume;
    @FXML private ComboBox<Saison> cbSaison;
    @FXML private TextField        txtNumeroEpisode;
    @FXML private TextField        txtDuree;
    @FXML private TextField        txtDureeIntro;
    @FXML private VBox      dropMiniature;
    @FXML private ImageView previewMiniature;
    @FXML private Label     lblMiniaturePlaceholder;
    @FXML private Label     lblMiniatureName;
    @FXML private VBox        dropVideo;
    @FXML private Label       lblVideoPlaceholder;
    @FXML private Label       lblVideoName;
    @FXML private ProgressBar pbVideo;
    @FXML private VBox subtitlesContainer;
    @FXML private Label  lblStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    private int editingEpisodeId = -1;
    private static class SubtitleRow {
        String langage  = "";
        String filePath = "";
    }
    private void populateForm(Episode ep) {
        txtTitre.setText(ep.getTitre());
        txtResume.setText(ep.getResume());
        txtNumeroEpisode.setText(String.valueOf(ep.getNumeroEpisode()));
        txtDuree.setText(String.valueOf(ep.getDuree()));
        txtDureeIntro.setText(String.valueOf(ep.getDurreeIntro()));
        for (Saison s : cbSaison.getItems()) {
            if (s.getId() == ep.getSaisonId()) {
                cbSaison.setValue(s);
                break;
            }
        }

        // Afficher miniature si elle existe
        if (ep.getMiniatureUrl() != null && !ep.getMiniatureUrl().isEmpty()) {
            try {
                String url = ep.getMiniatureUrl().startsWith("http") || ep.getMiniatureUrl().startsWith("file:")
                    ? ep.getMiniatureUrl() : "file:" + ep.getMiniatureUrl();
                previewMiniature.setImage(new Image(url));
                previewMiniature.setVisible(true);
                lblMiniaturePlaceholder.setVisible(false);
                lblMiniatureName.setText("Image existante");
            } catch (Exception ignored) {}
        }
        this.editingEpisodeId = ep.getId();
        txtDuree.setEditable(false);
        txtDuree.setStyle("-fx-background-color:#1a1a22; -fx-text-fill:#888888;");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = ConxDB.getInstance();
        episodeService      = new EpisodeService(connection);
        saisonService       = new SaisonService(connection);
        subtitleService     = new SubtitleService(connection);
        notificationService = new NotificationService(connection);

        loadSaisons();

        int saisonId = ScreenManager.getInstance().getSelectedSaisonId();
        if (saisonId != -1) {
            for (Saison s : cbSaison.getItems()) {
                if (s.getId() == saisonId) {
                    cbSaison.setValue(s);
                    break;
                }
            }
            cbSaison.setDisable(true); // ✅ saison fixe, non modifiable
        }

        Episode ep = ScreenManager.getInstance().getEditingEpisode();
        if (ep != null) {
            populateForm(ep);
            btnSave.setText("💾 Modifier");
            cbSaison.setDisable(false); // ✅ en mode edit, on peut changer la saison
        }
        
    }

    private void loadSaisons() {
        try {
            List<Saison> saisons = saisonService.findAll();
            cbSaison.getItems().addAll(saisons);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger les saisons.");
        }
    }
    @FXML
    private void handleAddSubtitle() {
        SubtitleRow sRow = new SubtitleRow();
        subtitleRows.add(sRow);

        ComboBox<String> cbLang = new ComboBox<>();
        cbLang.getItems().addAll("FR", "EN", "AR", "ES", "DE", "IT");
        cbLang.setPromptText("Langue");
        cbLang.setPrefWidth(90);
        cbLang.setStyle("-fx-background-color:#0f0f13; -fx-border-color:#2a2a35; -fx-border-radius:6;");
        cbLang.setOnAction(e -> sRow.langage = cbLang.getValue() != null ? cbLang.getValue() : "");

        Label lblFile = new Label("Aucun fichier");
        lblFile.setStyle("-fx-text-fill:#888888; -fx-font-size:11px;");
        HBox.setHgrow(lblFile, Priority.ALWAYS);

        Button btnBrowse = new Button("📁 Parcourir");
        btnBrowse.setStyle("-fx-background-color:#2a2a35; -fx-text-fill:#cccccc; -fx-cursor:hand;");
        btnBrowse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sous-titres", "*.srt", "*.vtt"));
            File f = fc.showOpenDialog(getCurrentStage());
            if (f != null) {
                sRow.filePath = f.getAbsolutePath();
                lblFile.setText(f.getName());
                lblFile.setStyle("-fx-text-fill:#e50914; -fx-font-weight:bold;");
            }
        });

        Button btnRemove = new Button("✕");
        btnRemove.setStyle("-fx-background-color:transparent; -fx-text-fill:#888888; -fx-cursor:hand;");
        
        HBox row = new HBox(8, cbLang, lblFile, btnBrowse, btnRemove);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#0f0f13; -fx-padding:8; -fx-border-color:#2a2a35; -fx-border-radius:6;");

        btnRemove.setOnAction(e -> {
            subtitlesContainer.getChildren().remove(row);
            subtitleRows.remove(sRow);
        });

        subtitlesContainer.getChildren().add(row);
    }
    
    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            Episode episode = buildEpisodeFromForm();
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            SerieService serieService = new SerieService(ConxDB.getInstance());
            int serieId = saisonService.getSerieIdBySaison(episode.getSaisonId());
            Serie serie = serieService.findById(serieId);
            String serieTitle = (serie != null) ? serie.getTitre() : "Inconnue";

            if (editingEpisodeId > 0) {
               
                episode.setId(editingEpisodeId);
                episode.setNbreVue(episodeService.findById(editingEpisodeId).getNbreVue());
                episodeService.update(episode);

                Notification n = new Notification(
                    0,
                    userId,
                    "MISE_A_JOUR",
                    "Épisode modifié",
                    "L'épisode \"" + episode.getTitre() + "\" de la série \"" + serieTitle + "\" a été mis à jour.",
                    java.time.LocalDate.now().toString(),
                    false,
                    false
                );
                notificationService.addNotification(n);

                showSuccess("✓ Épisode « " + episode.getTitre() + " » modifié !");

            } else {
      
                int episodeId = episodeService.save(episode);
                if (episodeId > 0) {
                    saveSubtitles(episodeId);

                    Notification n = new Notification(
                        0,
                        userId,
                        "NOUVEAUTE",
                        "Nouvel épisode ajouté",
                        "Un nouvel épisode \"" + episode.getTitre() + "\" a été ajouté à la série \"" + serieTitle + "\".",
                        java.time.LocalDate.now().toString(),
                        false,
                        false
                    );
                    notificationService.addNotification(n);

                    showSuccess("✓ Épisode « " + episode.getTitre() + " » enregistré !");
                } else {
                    showError("Erreur lors de l'enregistrement.");
                    return;
                }
            }

            ScreenManager.getInstance().setEditingEpisode(null);
            editingEpisodeId = -1;
            clearForm();

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void saveSubtitles(int episodeId) {
        for (SubtitleRow sr : subtitleRows) {
            
            Subtitle sub = new Subtitle(sr.langage, 0, episodeId, sr.filePath);
            int result = subtitleService.addSubtitle(sub);
            if (result == 0) {
                System.err.println("❌ Échec enregistrement sous-titre : " + sr.langage);
            }
        }
    }

    private Episode buildEpisodeFromForm() {
        int saisonId = cbSaison.getValue() != null ? cbSaison.getValue().getId() : 0;
        return new Episode(
            saisonId, txtTitre.getText().trim(),
            parseIntSafe(txtNumeroEpisode.getText()),
            fileVideo != null ? fileVideo.getAbsolutePath() : "",
            parseIntSafe(txtDuree.getText()),
            txtResume.getText().trim(),
            fileMiniature != null ? fileMiniature.getAbsolutePath() : "",
            parseIntSafe(txtDureeIntro.getText()),
            0
        );
    }

    @FXML
    private void handlePickMiniature() {
        File file = pickImageFile("Miniature de l'épisode");
        if (file != null) {
            fileMiniature = file;
            lblMiniatureName.setText(file.getName());
            lblMiniaturePlaceholder.setVisible(false);
            previewMiniature.setImage(new Image(file.toURI().toString()));
            previewMiniature.setVisible(true);
            activateDropZone(dropMiniature);
        }
    }
    @FXML
    private void handlePickVideo() {
        File file = pickVideoFile("Vidéo de l'épisode");
        if (file != null) {
            fileVideo = file;
            lblVideoName.setText(file.getName());
            lblVideoPlaceholder.setText("✅");
            activateDropZone(dropVideo);
            simulateProgress(pbVideo, "Vidéo sélectionnée");

            // ✅ Thread séparé — même solution que AddFilmController
            new Thread(() -> {
                try {
                    String uri = file.toURI().toString();
                    javafx.scene.media.Media media = new javafx.scene.media.Media(uri);
                    javafx.scene.media.MediaPlayer mp = new javafx.scene.media.MediaPlayer(media);

                    // Attendre max 10 secondes que la durée soit disponible
                    long timeout = System.currentTimeMillis() + 10000;
                    while (System.currentTimeMillis() < timeout) {
                        javafx.util.Duration d = media.getDuration();
                        if (d != null && !d.isUnknown() && !d.isIndefinite()
                                && d.greaterThan(javafx.util.Duration.ZERO)) {
                            mp.dispose();
                            long durationSeconds = (long) d.toSeconds();
                            int minutes = (int) (durationSeconds / 60);

                            javafx.application.Platform.runLater(() -> {
                                videoDurationSeconds = durationSeconds;
                                txtDuree.setText(String.valueOf(minutes));
                                showSuccess("✅ Durée détectée : " + minutes + " min");
                            });
                            return;
                        }
                        Thread.sleep(100);
                    }

                    // Timeout — durée non détectée
                    mp.dispose();
                    javafx.application.Platform.runLater(() ->
                        showError("⚠ Durée non détectée, entrez-la manuellement."));

                } catch (Exception e) {
                    javafx.application.Platform.runLater(() ->
                        showError("Erreur durée : " + e.getMessage()));
                }
            }).start();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtTitre.getText().trim().isEmpty())
            errors.append("• Titre obligatoire.\n");

        if (cbSaison.getValue() == null)
            errors.append("• Saison obligatoire.\n");

        if (txtNumeroEpisode.getText().trim().isEmpty()) {
            errors.append("• Numéro d'épisode obligatoire.\n");
        } else {
            try {
                int num = Integer.parseInt(txtNumeroEpisode.getText().trim());
                if (num <= 0) errors.append("• Le numéro d'épisode doit être supérieur à 0.\n");
            } catch (NumberFormatException e) {
                errors.append("• Le numéro d'épisode doit être un entier.\n");
            }
        }

       
        if (txtDuree.getText().trim().isEmpty()) {
            errors.append("• Durée obligatoire.\n");
        } else {
            try {
                int dureeSaisie = Integer.parseInt(txtDuree.getText().trim());
                if (dureeSaisie <= 0) {
                    errors.append("• La durée doit être supérieure à 0.\n");
                } else if (videoDurationSeconds > 0) {
                    long dureeVideoMin = videoDurationSeconds / 60;
                    if (dureeSaisie != dureeVideoMin) {
                        errors.append("• La durée saisie (").append(dureeSaisie)
                              .append(" min) ne correspond pas à la durée réelle de la vidéo (")
                              .append(dureeVideoMin).append(" min).\n");
                    }
                }
            } catch (NumberFormatException e) {
                errors.append("• La durée doit être un nombre entier (en minutes).\n");
            }
        }

        if (fileVideo == null && editingEpisodeId <= 0)
            errors.append("• Vidéo obligatoire.\n");

        
        for (int i = 0; i < subtitleRows.size(); i++) {
            SubtitleRow sr = subtitleRows.get(i);
            boolean hasLang = sr.langage != null && !sr.langage.isEmpty();
            boolean hasFile = sr.filePath != null && !sr.filePath.isEmpty();

            if (!hasLang && !hasFile) {
                errors.append("• Sous-titre #").append(i + 1)
                      .append(" : veuillez sélectionner une langue et un fichier.\n");
            } else if (!hasLang) {
                errors.append("• Sous-titre #").append(i + 1)
                      .append(" : veuillez sélectionner une langue.\n");
            } else if (!hasFile) {
                errors.append("• Sous-titre #").append(i + 1)
                      .append(" : veuillez choisir un fichier (.srt / .vtt).\n");
            }
        }

        if (errors.length() > 0) {
            showError(errors.toString().trim());
            return false;
        }
        return true;
    }

    /*@FXML
    private void handleCancel() {
        ScreenManager.getInstance().setEditingEpisode(null);
        editingEpisodeId = -1;
        clearForm();
    }*/
    @FXML
    private void handleCancel() {
        ScreenManager.getInstance().setEditingEpisode(null);
        ScreenManager.getInstance().setSelectedSaisonId(-1); // ✅ reset
        editingEpisodeId = -1;
        clearForm();
    }

    /*@FXML
    private void handleRetour() {
        ScreenManager.getInstance().setEditingEpisode(null);
        editingEpisodeId = -1;
        ScreenManager.getInstance().navigateTo(Screen.ManageSeries);
    }*/
    @FXML
    private void handleRetour() {
        ScreenManager.getInstance().setEditingEpisode(null);
        ScreenManager.getInstance().setSelectedSaisonId(-1); // ✅ reset
        editingEpisodeId = -1;
        ScreenManager.getInstance().navigateTo(Screen.ManageSeries);
    }

    private void clearForm() {
        txtTitre.clear();
        txtResume.clear();
        txtNumeroEpisode.clear();
        txtDuree.clear();
        txtDureeIntro.clear();
        cbSaison.getSelectionModel().clearSelection();
        fileMiniature = null;
        fileVideo = null;
        previewMiniature.setVisible(false);
        lblMiniaturePlaceholder.setVisible(true);
        subtitlesContainer.getChildren().clear();
        subtitleRows.clear();
        lblStatus.setText("");
    }

    private File pickImageFile(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        return fc.showOpenDialog(getCurrentStage());
    }
    
    private File pickVideoFile(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mkv"));
        return fc.showOpenDialog(getCurrentStage());
    }

    private void simulateProgress(ProgressBar pb, String msg) {
        pb.setVisible(true);
        pb.setProgress(0);
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(20), e -> pb.setProgress(pb.getProgress() + 0.05)));
        tl.setCycleCount(20);
        tl.setOnFinished(e -> showSuccess(msg));
        tl.play();
    }

    private void activateDropZone(VBox zone) {
        zone.setStyle("-fx-border-color:#e50914; -fx-background-color:#e509140a; -fx-border-style: dashed;");
    }

    private int parseIntSafe(String val) {
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return 0; }
    }

    private Stage getCurrentStage() {
        return (Stage) btnSave.getScene().getWindow();
    }

    private void showSuccess(String message) {
        lblStatus.setStyle("-fx-text-fill:#4caf50;");
        lblStatus.setText(message);
    }

    private void showError(String message) {
        lblStatus.setStyle("-fx-text-fill:#e50914;");
        lblStatus.setText(message);
    }
   
}