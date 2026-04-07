package tn.farah.NetflixJava.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.utils.ConxDB;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

public class AddEpisodeController implements Initializable {

    // ─── Services ────────────────────────────────────────────────────────────
    private EpisodeService episodeService;
    private SaisonService  saisonService;   // pour remplir le ComboBox des saisons

    // ─── Fichiers sélectionnés ────────────────────────────────────────────────
    private File fileMiniature;
    private File fileVideo;

    // ─── Champs formulaire ────────────────────────────────────────────────────
    @FXML private TextField           txtTitre;
    @FXML private TextArea            txtResume;
    @FXML private ComboBox<Saison>    cbSaison;          // lié à saisonId
    @FXML private TextField           txtNumeroEpisode;
    @FXML private TextField           txtDuree;
    @FXML private TextField           txtDureeIntro;     // durreeIntro

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

    // ─── Status & actions ────────────────────────────────────────────────────
    @FXML private Label  lblStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ═════════════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = ConxDB.getInstance();
        episodeService = new EpisodeService(connection);
        saisonService  = new SaisonService(connection);

        loadSaisons();
    }

    // ─── Chargement des saisons ───────────────────────────────────────────────
    private void loadSaisons() {
        try {
            List<Saison> saisons = saisonService.findAll();
            cbSaison.getItems().addAll(saisons);
            System.out.println("Saisons chargées : " + saisons.size());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger les saisons : " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UPLOAD – MINIATURE
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void handlePickMiniature() {
        File file = pickImageFile("Sélectionner la miniature de l'épisode");
        if (file != null) {
            fileMiniature = file;
            lblMiniatureName.setText(file.getName());
            lblMiniaturePlaceholder.setVisible(false);
            previewMiniature.setImage(new Image(file.toURI().toString()));
            previewMiniature.setVisible(true);
            activateDropZone(dropMiniature);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UPLOAD – VIDÉO
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void handlePickVideo() {
        File file = pickVideoFile("Sélectionner la vidéo de l'épisode");
        if (file != null) {
            fileVideo = file;
            lblVideoName.setText(file.getName());
            lblVideoPlaceholder.setText("✅");
            activateDropZone(dropVideo);
            simulateProgress(pbVideo, "Vidéo prête : " + file.getName());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SAVE
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            Episode episode = buildEpisodeFromForm();
            int result = episodeService.save(episode);
            if (result > 0) {
                showSuccess("✓  Épisode « " + episode.getTitre() + " » enregistré !");
                clearForm();
            } else {
                showError("Enregistrement refusé — vérifiez les règles métier.");
            }
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Episode buildEpisodeFromForm() {
        // Récupération des valeurs saisies
        int saisonId     = cbSaison.getValue() != null ? cbSaison.getValue().getId() : 0;
        int numero       = parseIntSafe(txtNumeroEpisode.getText());
        int duree        = parseIntSafe(txtDuree.getText());
        int dureeIntro   = parseIntSafe(txtDureeIntro.getText());

        String miniatureUrl = fileMiniature != null ? fileMiniature.toURI().toString() : "";
        String videoUrl     = fileVideo     != null ? fileVideo.toURI().toString()     : "";

        // Constructeur sans id (nouvel épisode)
        Episode episode = new Episode(
            saisonId,
            txtTitre.getText().trim(),
            numero,
            videoUrl,
            duree,
            txtResume.getText().trim(),
            miniatureUrl,
            dureeIntro
        );
        return episode;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  VALIDATION
    // ═════════════════════════════════════════════════════════════════════════
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtTitre.getText().trim().isEmpty())
            errors.append("• Titre obligatoire.\n");
        if (txtResume.getText().trim().isEmpty())
            errors.append("• Résumé obligatoire.\n");
        if (cbSaison.getValue() == null)
            errors.append("• Veuillez sélectionner une saison.\n");
        if (txtNumeroEpisode.getText().trim().isEmpty() || parseIntSafe(txtNumeroEpisode.getText()) <= 0)
            errors.append("• Numéro d'épisode invalide (doit être > 0).\n");
        if (txtDuree.getText().trim().isEmpty() || parseIntSafe(txtDuree.getText()) <= 0)
            errors.append("• Durée invalide (doit être > 0).\n");
        if (fileMiniature == null)
            errors.append("• Miniature obligatoire.\n");
        if (fileVideo == null)
            errors.append("• Vidéo obligatoire.\n");

        if (errors.length() > 0) {
            showError(errors.toString().trim());
            return false;
        }
        return true;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ANNULER / RÉINITIALISER
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler ? Les données non sauvegardées seront perdues.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> { if (btn == ButtonType.YES) clearForm(); });
    }

    private void clearForm() {
        txtTitre.clear();
        txtResume.clear();
        txtNumeroEpisode.clear();
        txtDuree.clear();
        txtDureeIntro.clear();
        cbSaison.getSelectionModel().clearSelection();

        fileMiniature = null;
        fileVideo     = null;

        previewMiniature.setVisible(false);
        lblMiniaturePlaceholder.setVisible(true);
        lblMiniatureName.setText("");
        lblVideoName.setText("");
        lblVideoPlaceholder.setText("▶");

        pbVideo.setVisible(false);
        pbVideo.setProgress(0);

        lblStatus.setText("");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════════════
    private File pickImageFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.gif"));
        return chooser.showOpenDialog(getCurrentStage());
    }

    private File pickVideoFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mkv", "*.avi", "*.mov", "*.webm"));
        return chooser.showOpenDialog(getCurrentStage());
    }

    private void simulateProgress(ProgressBar pb, String successMessage) {
        pb.setVisible(true);
        pb.setProgress(0);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(30), e ->
                        pb.setProgress(Math.min(pb.getProgress() + 0.02, 1.0))));
        tl.setCycleCount(50);
        tl.setOnFinished(e -> showSuccess(successMessage));
        tl.play();
    }

    private void activateDropZone(VBox zone) {
        zone.setStyle(zone.getStyle()
                + "-fx-border-color:#e50914; -fx-background-color:#e509140a;");
    }

    private int parseIntSafe(String value) {
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private Stage getCurrentStage() {
        return (Stage) btnSave.getScene().getWindow();
    }

    private void showSuccess(String message) {
        lblStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#4caf50; -fx-padding:0 4 0 4;");
        lblStatus.setText(message);
    }

    private void showError(String message) {
        lblStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#e50914; -fx-padding:0 4 0 4;");
        lblStatus.setText(message);
    }
}