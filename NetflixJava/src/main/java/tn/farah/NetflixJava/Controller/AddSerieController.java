package tn.farah.NetflixJava.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import tn.farah.NetflixJava.Entities.*;
import tn.farah.NetflixJava.Service.CategoryService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.Service.WarningService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;

public class AddSerieController implements Initializable {

    // ─── Services ────────────────────────────────────────────────────────────
    private SerieService    serieService;
    private CategoryService categoryService;
    private WarningService  warningService;

    // ─── State ───────────────────────────────────────────────────────────────
    private final Set<Category> selectedCategories = new HashSet<>();
    private final Set<Warning>  selectedWarnings   = new HashSet<>();
    private File filePoster;
    private File fileBanner;
    private File fileTeaser;

    // ─── Form fields ─────────────────────────────────────────────────────────
    @FXML private TextField  txtTitre;
    @FXML private TextArea   txtSynopsis;
    @FXML private TextField  txtCasting;
    @FXML private TextField  txtProducteur;
    @FXML private DatePicker dpDateSortie;
    @FXML private ComboBox<AgeRating> cbAgeRating;
    @FXML private CheckBox   cbTerminee;

    // ─── Categories ──────────────────────────────────────────────────────────
    @FXML private FlowPane  categoriesPane;
    @FXML private HBox      addCategoryBox;
    @FXML private TextField txtNewCategory;

    // ─── Warnings ────────────────────────────────────────────────────────────
    @FXML private FlowPane  warningsPane;
    @FXML private HBox      addWarningBox;
    @FXML private TextField txtNewWarning;

    // ─── Upload – Poster ─────────────────────────────────────────────────────
    @FXML private VBox      dropPoster;
    @FXML private ImageView previewPoster;
    @FXML private Label     lblPosterPlaceholder;
    @FXML private Label     lblPosterName;

    // ─── Upload – Banner ─────────────────────────────────────────────────────
    @FXML private VBox      dropBanner;
    @FXML private ImageView previewBanner;
    @FXML private Label     lblBannerPlaceholder;
    @FXML private Label     lblBannerName;

    // ─── Upload – Teaser ─────────────────────────────────────────────────────
    @FXML private VBox        dropTeaser;
    @FXML private Label       lblTeaserPlaceholder;
    @FXML private Label       lblTeaserName;
    @FXML private ProgressBar pbTeaser;

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
        serieService    = new SerieService(connection);
        categoryService = new CategoryService(connection);
        warningService  = new WarningService(connection);

        cbAgeRating.getItems().addAll(AgeRating.values());
        cbAgeRating.getSelectionModel().selectFirst();

        loadCategoryChips();
        loadWarningChips();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CATEGORIES
    // ═════════════════════════════════════════════════════════════════════════
    private void loadCategoryChips() {
        categoriesPane.getChildren().clear();
        try {
            List<Category> categories = categoryService.getAllCategoriesSorted();
            for (Category cat : categories)
                categoriesPane.getChildren().add(buildCategoryChip(cat));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger les catégories.");
        }
    }

    private Button buildCategoryChip(Category cat) {
        Button chip = new Button(cat.getName());
        applyChipStyle(chip, selectedCategories.contains(cat));
        chip.setOnAction(e -> {
            boolean selected = selectedCategories.contains(cat);
            if (selected) selectedCategories.remove(cat);
            else          selectedCategories.add(cat);
            applyChipStyle(chip, !selected);
        });
        return chip;
    }

    @FXML private void handleAddCategory() {
        addCategoryBox.setVisible(true);
        addCategoryBox.setManaged(true);
        txtNewCategory.requestFocus();
    }

    @FXML private void handleConfirmAddCategory() {
        try {
            String name = txtNewCategory.getText().trim();
            if (name.isEmpty()) { showError("Le nom ne peut pas être vide."); return; }
            categoryService.saveCategory(name);
            categoryService.getAllCategoriesSorted().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .ifPresent(selectedCategories::add);
            loadCategoryChips();
            showSuccess("Catégorie « " + name + " » ajoutée !");
            txtNewCategory.clear();
            handleCancelAddCategory();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur : " + ex.getMessage());
        }
    }

    @FXML private void handleCancelAddCategory() {
        addCategoryBox.setVisible(false);
        addCategoryBox.setManaged(false);
        txtNewCategory.clear();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  WARNINGS
    // ═════════════════════════════════════════════════════════════════════════
    private void loadWarningChips() {
        warningsPane.getChildren().clear();
        try {
            List<Warning> warnings = warningService.getAllWarnings();
            for (Warning w : warnings)
                warningsPane.getChildren().add(buildWarningChip(w));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Impossible de charger les warnings.");
        }
    }

    private Button buildWarningChip(Warning w) {
        Button chip = new Button(w.getNom());
        applyChipStyle(chip, selectedWarnings.contains(w));
        chip.setOnAction(e -> {
            boolean selected = selectedWarnings.contains(w);
            if (selected) selectedWarnings.remove(w);
            else          selectedWarnings.add(w);
            applyChipStyle(chip, !selected);
        });
        return chip;
    }

    @FXML private void handleAddWarning() {
        addWarningBox.setVisible(true);
        addWarningBox.setManaged(true);
        txtNewWarning.requestFocus();
    }

    @FXML private void handleConfirmAddWarning() {
        String name = txtNewWarning.getText().trim();
        if (name.isEmpty()) { showError("Le nom ne peut pas être vide."); return; }
        try {
            Warning newWarn = new Warning(0, name);
            warningService.save(newWarn);
            selectedWarnings.add(newWarn);
            loadWarningChips();
            showSuccess("Warning « " + name + " » ajouté !");
            txtNewWarning.clear();
            handleCancelAddWarning();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    @FXML private void handleCancelAddWarning() {
        addWarningBox.setVisible(false);
        addWarningBox.setManaged(false);
        txtNewWarning.clear();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FILE UPLOADS
    // ═════════════════════════════════════════════════════════════════════════
    @FXML private void handlePickPoster() {
        File file = pickImageFile("Sélectionner l'affiche (poster)");
        if (file != null) {
            filePoster = file;
            lblPosterName.setText(file.getName());
            lblPosterPlaceholder.setVisible(false);
            previewPoster.setImage(new Image(file.toURI().toString()));
            previewPoster.setVisible(true);
            activateDropZone(dropPoster);
        }
    }

    @FXML private void handlePickBanner() {
        File file = pickImageFile("Sélectionner la bannière (background)");
        if (file != null) {
            fileBanner = file;
            lblBannerName.setText(file.getName());
            lblBannerPlaceholder.setVisible(false);
            previewBanner.setImage(new Image(file.toURI().toString()));
            previewBanner.setVisible(true);
            activateDropZone(dropBanner);
        }
    }

    @FXML private void handlePickTeaser() {
        File file = pickVideoFile("Sélectionner le teaser / bande-annonce");
        if (file != null) {
            fileTeaser = file;
            lblTeaserName.setText(file.getName());
            lblTeaserPlaceholder.setText("✅");
            activateDropZone(dropTeaser);
            simulateProgress(pbTeaser, "Teaser prêt : " + file.getName());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
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
        zone.setStyle(zone.getStyle() + "-fx-border-color:#e50914; -fx-background-color:#e509140a;");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SAVE
    // ═════════════════════════════════════════════════════════════════════════
    @FXML private void handleSave() {
        if (!validateForm()) return;
        try {
            Serie serie = buildSerieFromForm();
            serieService.enregistrerSerie(serie);
            showSuccess("✓  Série « " + serie.getTitre() + " » enregistrée avec succès !");
            clearForm();
        } catch (Exception e) {
            showError("Erreur lors de l'enregistrement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Serie buildSerieFromForm() {
        String urlCover  = filePoster != null ? filePoster.toURI().toString() : "";
        String urlBanner = fileBanner != null ? fileBanner.toURI().toString() : "";
        String urlTeaser = fileTeaser != null ? fileTeaser.toURI().toString() : "";

        Serie serie = new Serie(
                0,
                txtTitre.getText().trim(),
                txtSynopsis.getText().trim(),
                txtCasting.getText().trim(),
                dpDateSortie.getValue(),
                urlCover,
                urlBanner,
                urlTeaser,
                0.0,
                cbAgeRating.getValue(),
                TypeMedia.Serie,
                LocalDateTime.now(),
                new HashSet<>(selectedCategories),
                new HashSet<>(selectedWarnings),
                "",           // genre field (legacy) — categories cover this
                cbTerminee.isSelected()
        );
        serie.setProducteur(txtProducteur.getText().trim());
        return serie;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  VALIDATION
    // ═════════════════════════════════════════════════════════════════════════
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        if (txtTitre.getText().trim().isEmpty())      errors.append("• Titre obligatoire.\n");
        if (txtSynopsis.getText().trim().isEmpty())   errors.append("• Synopsis obligatoire.\n");
        if (txtCasting.getText().trim().isEmpty())    errors.append("• Casting obligatoire.\n");
        if (txtProducteur.getText().trim().isEmpty()) errors.append("• Producteur obligatoire.\n");
        if (dpDateSortie.getValue() == null)          errors.append("• Date de sortie obligatoire.\n");
        if (cbAgeRating.getValue() == null)           errors.append("• Age Rating obligatoire.\n");
        if (filePoster == null)                       errors.append("• Affiche (poster) obligatoire.\n");
        if (fileBanner == null)                       errors.append("• Bannière obligatoire.\n");
        if (selectedCategories.isEmpty())             errors.append("• Au moins une catégorie requise.\n");
        if (errors.length() > 0) { showError(errors.toString().trim()); return false; }
        return true;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CANCEL / RESET
    // ═════════════════════════════════════════════════════════════════════════
    @FXML private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler ? Les données non sauvegardées seront perdues.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> { if (btn == ButtonType.YES) clearForm(); });
    }

    private void clearForm() {
        txtTitre.clear(); txtSynopsis.clear();
        txtCasting.clear(); txtProducteur.clear();
        dpDateSortie.setValue(null);
        cbAgeRating.getSelectionModel().selectFirst();
        cbTerminee.setSelected(false);

        selectedCategories.clear(); selectedWarnings.clear();
        loadCategoryChips(); loadWarningChips();

        filePoster = null; fileBanner = null; fileTeaser = null;

        previewPoster.setVisible(false); previewBanner.setVisible(false);
        lblPosterPlaceholder.setVisible(true); lblBannerPlaceholder.setVisible(true);
        lblPosterName.setText(""); lblBannerName.setText("");
        lblTeaserName.setText("");
        lblTeaserPlaceholder.setText("🎞");

        pbTeaser.setVisible(false); pbTeaser.setProgress(0);

        lblStatus.setText("");
        lblStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#e50914; -fx-padding:0 4 0 4;");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════════════
    private void applyChipStyle(Button chip, boolean selected) {
        if (selected) {
            chip.setStyle("-fx-background-color:#e50914; -fx-background-radius:20; " +
                          "-fx-text-fill:white; -fx-font-size:12px; -fx-font-weight:bold; " +
                          "-fx-padding:5 14 5 14; -fx-cursor:hand;");
        } else {
            chip.setStyle("-fx-background-color:#2a2a35; -fx-background-radius:20; " +
                          "-fx-text-fill:#cccccc; -fx-font-size:12px; " +
                          "-fx-padding:5 14 5 14; -fx-cursor:hand;");
        }
    }

    private Stage getCurrentStage() { return (Stage) btnSave.getScene().getWindow(); }

    private void showSuccess(String message) {
        lblStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#e50914; -fx-padding:0 4 0 4;");
        lblStatus.setText(message);
    }

    private void showError(String message) {
        lblStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#e50914; -fx-padding:0 4 0 4;");
        lblStatus.setText(message);
    }

    @FXML private void handleRetour() {
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
    }
}
