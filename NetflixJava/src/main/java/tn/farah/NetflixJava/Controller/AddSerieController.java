package tn.farah.NetflixJava.Controller;

import javafx.animation.KeyFrame;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
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
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.Service.WarningService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;

public class AddSerieController implements Initializable {

	private Serie serieToEdit = null;

    private SerieService    serieService;
    private CategoryService categoryService;
    private WarningService  warningService;

    private final Set<Category> selectedCategories = new HashSet<>();
    private final Set<Warning>  selectedWarnings   = new HashSet<>();
    private File filePoster;
    private File fileBanner;
    private File fileTeaser;

    @FXML private TextField  txtTitre;
    @FXML private TextArea   txtSynopsis;
    @FXML private TextField  txtCasting;
    @FXML private TextField  txtProducteur;
    @FXML private DatePicker dpDateSortie;
    @FXML private ComboBox<AgeRating> cbAgeRating;
    @FXML private CheckBox   cbTerminee;
    
    @FXML private FlowPane  categoriesPane;
    @FXML private HBox      addCategoryBox;
    @FXML private TextField txtNewCategory;

    @FXML private FlowPane  warningsPane;
    @FXML private HBox      addWarningBox;
    @FXML private TextField txtNewWarning;
    @FXML private VBox      dropPoster;
    @FXML private ImageView previewPoster;
    @FXML private Label     lblPosterPlaceholder;
    @FXML private Label     lblPosterName;

    @FXML private VBox      dropBanner;
    @FXML private ImageView previewBanner;
    @FXML private Label     lblBannerPlaceholder;
    @FXML private Label     lblBannerName;

    @FXML private VBox        dropTeaser;
    @FXML private Label       lblTeaserPlaceholder;
    @FXML private Label       lblTeaserName;
    @FXML private ProgressBar pbTeaser;
    @FXML private Label  lblStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = ConxDB.getInstance();
        serieService    = new SerieService(connection);
        categoryService = new CategoryService(connection);
        warningService  = new WarningService(connection);

        cbAgeRating.getItems().addAll(AgeRating.values());
        cbAgeRating.getSelectionModel().selectFirst();

       
        serieToEdit = ScreenManager.getInstance().getEditingSerie();
        ScreenManager.getInstance().clearEditingSerie();

        loadCategoryChips();
        loadWarningChips();

        if (serieToEdit != null) {
            enterEditMode(serieToEdit);
        }
    }
    private void enterEditMode(Serie serie) {
       
        if (lblStatus != null) lblStatus.setText("Mode modification");
        if (btnSave != null)   btnSave.setText("✓ Enregistrer les modifications");

        txtTitre.setText(serie.getTitre() != null ? serie.getTitre() : "");
        txtSynopsis.setText(serie.getSynopsis() != null ? serie.getSynopsis() : "");
        txtCasting.setText(serie.getCasting() != null ? serie.getCasting() : "");
        txtProducteur.setText(serie.getProducteur() != null ? serie.getProducteur() : "");
        dpDateSortie.setValue(serie.getDateSortie());
        cbTerminee.setSelected(serie.isTerminee());

        if (serie.getAgeRating() != null)
            cbAgeRating.getSelectionModel().select(serie.getAgeRating());

        if (serie.getGenres() != null) {
            selectedCategories.addAll(serie.getGenres());
            loadCategoryChips(); 
        }
        if (serie.getWarnings() != null) {
            selectedWarnings.addAll(serie.getWarnings());
            loadWarningChips();
        }

        // Afficher le poster actuel
        String cover = serie.getUrlImageCover();
        if (cover != null && !cover.isEmpty()) {
            try {
                previewPoster.setImage(new Image(cover, true));
                previewPoster.setVisible(true);
                lblPosterPlaceholder.setVisible(false);
                lblPosterName.setText("(image actuelle)");
                activateDropZone(dropPoster);
            } catch (Exception ignored) {}
        }

        String banner = serie.getUrlImageBanner();
        if (banner != null && !banner.isEmpty()) {
            try {
                previewBanner.setImage(new Image(banner, true));
                previewBanner.setVisible(true);
                lblBannerPlaceholder.setVisible(false);
                lblBannerName.setText("(bannière actuelle)");
                activateDropZone(dropBanner);
            } catch (Exception ignored) {}
        }

        String teaser = serie.getUrlTeaser();
        if (teaser != null && !teaser.isEmpty()) {
            lblTeaserPlaceholder.setText("✅");
            lblTeaserName.setText("(teaser actuel)");
            activateDropZone(dropTeaser);
        }
    }

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
        boolean isSelected = selectedCategories.stream().anyMatch(c -> c.getId() == cat.getId());
        Button chip = new Button(cat.getName());
        applyChipStyle(chip, isSelected);

        chip.setOnAction(e -> {
            boolean sel = selectedCategories.stream().anyMatch(c -> c.getId() == cat.getId());
            if (sel) selectedCategories.removeIf(c -> c.getId() == cat.getId());
            else     selectedCategories.add(cat);
            applyChipStyle(chip, !sel);
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333;");

        MenuItem itemModifier = new MenuItem("✏ Modifier");
        itemModifier.setStyle("-fx-text-fill: white; -fx-background-color: #1a1a1a;");
        itemModifier.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(cat.getName());
            dialog.setTitle("Modifier la catégorie");
            dialog.setHeaderText(null);
            dialog.setContentText("Nouveau nom :");
            dialog.getDialogPane().setStyle("-fx-background-color: #111;");
            dialog.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.trim().isEmpty()) {
                    try {
                        cat.setName(newName.trim());
                        categoryService.updateCategory(cat);
                        loadCategoryChips();
                        showSuccess("Catégorie renommée en « " + newName.trim() + " »");
                    } catch (Exception ex) {
                        showError("Erreur : " + ex.getMessage());
                    }
                }
            });
        });

        MenuItem itemSupprimer = new MenuItem("🗑 Supprimer");
        itemSupprimer.setStyle("-fx-text-fill: #e50914; -fx-background-color: #1a1a1a;");
        itemSupprimer.setOnAction(e -> {
            boolean estSelectionnee = selectedCategories.stream().anyMatch(c -> c.getId() == cat.getId());
            
            String message = estSelectionnee
                ? "La catégorie « " + cat.getName() + " » est actuellement sélectionnée.\nElle sera désélectionnée ET supprimée définitivement. Confirmer ?"
                : "Supprimer « " + cat.getName() + " » définitivement ?";

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Supprimer la catégorie");
            confirm.setHeaderText(estSelectionnee ? "⚠ Catégorie active !" : null);
            confirm.setContentText(message);
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    try {
                        selectedCategories.removeIf(c -> c.getId() == cat.getId());
                        categoryService.deleteCategory(cat.getId());
                        loadCategoryChips();
                        showSuccess("Catégorie « " + cat.getName() + " » supprimée.");
                    } catch (Exception ex) {
                        showError("Erreur : " + ex.getMessage());
                    }
                }
            });
        });
        contextMenu.getItems().addAll(itemModifier, itemSupprimer);
        chip.setOnContextMenuRequested(e ->
            contextMenu.show(chip, e.getScreenX(), e.getScreenY()));

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
        boolean isSelected = selectedWarnings.stream().anyMatch(sw -> sw.getId() == w.getId());
        Button chip = new Button(w.getNom());
        applyChipStyle(chip, isSelected);

        chip.setOnAction(e -> {
            boolean sel = selectedWarnings.stream().anyMatch(sw -> sw.getId() == w.getId());
            if (sel) selectedWarnings.removeIf(sw -> sw.getId() == w.getId());
            else     selectedWarnings.add(w);
            applyChipStyle(chip, !sel);
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333;");

        MenuItem itemModifier = new MenuItem("✏ Modifier");
        itemModifier.setStyle("-fx-text-fill: white; -fx-background-color: #1a1a1a;");
        itemModifier.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(w.getNom());
            dialog.setTitle("Modifier le warning");
            dialog.setHeaderText(null);
            dialog.setContentText("Nouveau nom :");
            dialog.getDialogPane().setStyle("-fx-background-color: #111;");
            dialog.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.trim().isEmpty()) {
                    try {
                        w.setNom(newName.trim());
                        warningService.updateWarning(w);
                        loadWarningChips();
                        showSuccess("Warning renommé en « " + newName.trim() + " »");
                    } catch (Exception ex) {
                        showError("Erreur : " + ex.getMessage());
                    }
                }
            });
        });

        MenuItem itemSupprimer = new MenuItem("🗑 Supprimer");
        itemSupprimer.setStyle("-fx-text-fill: #e50914; -fx-background-color: #1a1a1a;");
        itemSupprimer.setOnAction(e -> {
            boolean estSelectionne = selectedWarnings.stream().anyMatch(sw -> sw.getId() == w.getId());

            String message = estSelectionne
                ? "Le warning « " + w.getNom() + " » est actuellement sélectionné.\nIl sera désélectionné ET supprimé définitivement. Confirmer ?"
                : "Supprimer « " + w.getNom() + " » définitivement ?";

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Supprimer le warning");
            confirm.setHeaderText(estSelectionne ? "⚠ Warning actif !" : null);
            confirm.setContentText(message);
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    try {
                        selectedWarnings.removeIf(sw -> sw.getId() == w.getId());
                        warningService.deleteWarning(w.getId());
                        loadWarningChips();
                        showSuccess("Warning « " + w.getNom() + " » supprimé.");
                    } catch (Exception ex) {
                        showError("Erreur : " + ex.getMessage());
                    }
                }
            });
        });

        contextMenu.getItems().addAll(itemModifier, itemSupprimer);
        chip.setOnContextMenuRequested(e ->
            contextMenu.show(chip, e.getScreenX(), e.getScreenY()));

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

    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            NotificationService notificationService = new NotificationService(ConxDB.getInstance());
            int userId = SessionManager.getInstance().getCurrentUser().getId();

            if (serieToEdit == null) {
                // ── MODE CRÉATION ──
                Serie serie = buildSerieFromForm(0);
                serieService.enregistrerSerie(serie);

                Notification n = new Notification(
                    0,
                    userId,
                    "NOUVEAUTE",
                    "Nouvelle série ajoutée",
                    "La série \"" + serie.getTitre() + "\" vient d'être ajoutée à la plateforme.",
                    java.time.LocalDate.now().toString(),
                    false,
                    false
                );
                notificationService.addNotification(n);

                showSuccess("✓ Série « " + serie.getTitre() + " » enregistrée !");
                clearForm();

            } else {
          
                Serie serie = buildSerieFromForm(serieToEdit.getId());
                serieService.updateSerie(serie);

                Notification n = new Notification(
                    0,
                    userId,
                    "MISE_A_JOUR",
                    "Série mise à jour",
                    "La série \"" + serie.getTitre() + "\" a été mise à jour.",
                    java.time.LocalDate.now().toString(),
                    false,
                    false
                );
                notificationService.addNotification(n);

                showSuccess("✓ Série « " + serie.getTitre() + " » mise à jour !");
                serieToEdit = null;
                clearForm();
            }

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String nullSafe(String s) { return s != null ? s : ""; }
    private Serie buildSerieFromForm(int id) {
        String urlCover  = filePoster != null ? filePoster.toURI().toString()
                         : (serieToEdit != null ? nullSafe(serieToEdit.getUrlImageCover()) : "");
        String urlBanner = fileBanner != null ? fileBanner.toURI().toString()
                         : (serieToEdit != null ? nullSafe(serieToEdit.getUrlImageBanner()) : "");
        String urlTeaser = fileTeaser != null ? fileTeaser.toURI().toString()
                         : (serieToEdit != null ? nullSafe(serieToEdit.getUrlTeaser()) : "");

        Serie serie = new Serie(
                id, 
                txtTitre.getText().trim(),
                txtSynopsis.getText().trim(),
                txtCasting.getText().trim(),
                dpDateSortie.getValue(),
                urlCover,
                urlBanner,
                urlTeaser,
                serieToEdit != null ? serieToEdit.getRatingMoyen() : 0.0,
                cbAgeRating.getValue(),
                TypeMedia.Serie,
                LocalDateTime.now(),
                new HashSet<>(selectedCategories),
                new HashSet<>(selectedWarnings),
                
                cbTerminee.isSelected()
        );
        serie.setProducteur(txtProducteur.getText().trim());
        return serie;
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
                
                cbTerminee.isSelected()
        );
        serie.setProducteur(txtProducteur.getText().trim());
        return serie;
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        if (txtTitre.getText().trim().isEmpty())      errors.append("• Titre obligatoire.\n");
        if (txtSynopsis.getText().trim().isEmpty())   errors.append("• Synopsis obligatoire.\n");
        if (txtCasting.getText().trim().isEmpty())    errors.append("• Casting obligatoire.\n");
        if (txtProducteur.getText().trim().isEmpty()) errors.append("• Producteur obligatoire.\n");
        if (dpDateSortie.getValue() == null)          errors.append("• Date de sortie obligatoire.\n");
        if (cbAgeRating.getValue() == null)           errors.append("• Age Rating obligatoire.\n");
        if (selectedCategories.isEmpty())             errors.append("• Au moins une catégorie requise.\n");

        boolean hasPoster = filePoster != null || (serieToEdit != null && !nullSafe(serieToEdit.getUrlImageCover()).isEmpty());
        boolean hasBanner = fileBanner != null || (serieToEdit != null && !nullSafe(serieToEdit.getUrlImageBanner()).isEmpty());

        if (!hasPoster) errors.append("• Affiche (poster) obligatoire.\n");
        if (!hasBanner) errors.append("• Bannière obligatoire.\n");

        if (errors.length() > 0) { showError(errors.toString().trim()); return false; }
        return true;
    }

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
        ScreenManager.getInstance().goBack();
    }
}
