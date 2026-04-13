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
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Subtitle;
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SubtitleService;
import tn.farah.NetflixJava.DAO.SubtitleDAO;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddEpisodeController implements Initializable {

    // ─── Services ─────────────────────────────────────────────────────────────
    private EpisodeService  episodeService;
    private SaisonService   saisonService;
    private SubtitleService subtitleService;

    // ─── Fichiers sélectionnés ────────────────────────────────────────────────
    private File fileMiniature;
    private File fileVideo;

    // ─── Sous-titres (lignes dynamiques) ─────────────────────────────────────
    private final List<SubtitleRow> subtitleRows = new ArrayList<>();

    // ─── Champs formulaire ────────────────────────────────────────────────────
    @FXML private TextField        txtTitre;
    @FXML private TextArea         txtResume;
    @FXML private ComboBox<Saison> cbSaison;
    @FXML private TextField        txtNumeroEpisode;
    @FXML private TextField        txtDuree;
    @FXML private TextField        txtDureeIntro;

    // ─── Upload – Miniature ───────────────────────────────────────────────────
    @FXML private VBox      dropMiniature;
    @FXML private ImageView previewMiniature;
    @FXML private Label     lblMiniaturePlaceholder;
    @FXML private Label     lblMiniatureName;

    // ─── Upload – Vidéo ──────────────────────────────────────────────────────
    @FXML private VBox        dropVideo;
    @FXML private Label       lblVideoPlaceholder;
    @FXML private Label       lblVideoName;
    @FXML private ProgressBar pbVideo;

    // ─── Sous-titres container ────────────────────────────────────────────────
    @FXML private VBox subtitlesContainer;

    // ─── Status & actions ────────────────────────────────────────────────────
    @FXML private Label  lblStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ═════════════════════════════════════════════════════════════════════════
    //  CLASSE INTERNE — une ligne sous-titre
    // ═════════════════════════════════════════════════════════════════════════
    private static class SubtitleRow {
        String langage  = "";
        String filePath = "";
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = ConxDB.getInstance();
        episodeService  = new EpisodeService(connection);
        saisonService   = new SaisonService(connection);
        // Initialisation du service avec le nouveau DAO
        subtitleService = new SubtitleService(new SubtitleDAO(connection));

        loadSaisons();
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

    // ═════════════════════════════════════════════════════════════════════════
    //  SOUS-TITRES — Gestion Dynamique
    // ═════════════════════════════════════════════════════════════════════════
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

    // ═════════════════════════════════════════════════════════════════════════
    //  SAVE LOGIC
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            // 1. Sauvegarder l'épisode
            Episode episode = buildEpisodeFromForm();
            int episodeId = episodeService.save(episode);

            if (episodeId > 0) {
                // 2. Sauvegarder les sous-titres avec le nouvel ID Episode
                saveSubtitles(episodeId);

                showSuccess("✓ Épisode « " + episode.getTitre() + " » enregistré !");
                clearForm();
            } else {
                showError("Erreur lors de l'enregistrement de l'épisode.");
            }
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSubtitles(int episodeId) {
        for (SubtitleRow sr : subtitleRows) {
            // Plus besoin du if — validateForm() garantit que tout est rempli
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
            saisonId,
            txtTitre.getText().trim(),
            parseIntSafe(txtNumeroEpisode.getText()),
            fileVideo != null ? fileVideo.getAbsolutePath() : "",
            parseIntSafe(txtDuree.getText()),
            txtResume.getText().trim(),
            fileMiniature != null ? fileMiniature.getAbsolutePath() : "",
            parseIntSafe(txtDureeIntro.getText())
        );
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UI HANDLERS & HELPERS
    // ═════════════════════════════════════════════════════════════════════════
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
        }
    }

    private boolean validateForm() {
        if (txtTitre.getText().isEmpty() || cbSaison.getValue() == null || fileVideo == null) {
            showError("Veuillez remplir les champs obligatoires (Titre, Saison, Vidéo).");
            return false;
        }

        // ✅ Validation des sous-titres
        for (SubtitleRow sr : subtitleRows) {
            if (sr.langage == null || sr.langage.isEmpty()) {
                showError("⚠️ Veuillez sélectionner une langue pour chaque sous-titre.");
                return false;
            }
            if (sr.filePath == null || sr.filePath.isEmpty()) {
                showError("⚠️ Veuillez choisir un fichier pour chaque sous-titre.");
                return false;
            }
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        clearForm();
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
    @FXML
    private void handleRetour() {
        
         ScreenManager.getInstance().navigateTo(Screen.ManageSeries); 
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