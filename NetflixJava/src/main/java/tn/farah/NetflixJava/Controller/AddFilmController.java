package tn.farah.NetflixJava.Controller;

import javafx.animation.KeyFrame;


import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.farah.NetflixJava.Entities.*;
import tn.farah.NetflixJava.Service.CategoryService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.SubtitleService;
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
//new
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
public class AddFilmController implements Initializable {

   
    private FilmService     filmService;
    private CategoryService categoryService;
    private WarningService  warningService;
    private SubtitleService subtitleService;

    private Film filmToEdit = null;
    private final Set<Category> selectedCategories = new HashSet<>();
    private final Set<Warning>  selectedWarnings   = new HashSet<>();
    private File filePoster;
    private File fileBanner;
    private File fileTeaser;
    private File fileVideo;
    private static class SubtitleRow {
        String langage  = "";
        String filePath = "";
    }
    private final List<SubtitleRow> subtitleRows = new ArrayList<>();
    @FXML private TextField  txtTitre;
    @FXML private TextArea   txtSynopsis;
    @FXML private TextField  txtCasting;
    @FXML private TextField  txtProducteur;
    @FXML private DatePicker dpDateSortie;
    @FXML private TextField  txtDuree;
    @FXML private ComboBox<AgeRating> cbAgeRating;
    private long videoDurationSeconds = -1;
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
    @FXML private VBox        dropVideo;
    @FXML private Label       lblVideoPlaceholder;
    @FXML private Label       lblVideoName;
    @FXML private ProgressBar pbVideo;
    @FXML private VBox   subtitlesContainer;
    @FXML private Button btnAddSubtitle;
    @FXML private Label  lblFormTitle;
    @FXML private Label  lblStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = ConxDB.getInstance();
        filmService     = new FilmService(connection);
        categoryService = new CategoryService(connection);
        warningService  = new WarningService(connection);
        
        subtitleService = new SubtitleService(connection);

        cbAgeRating.getItems().addAll(AgeRating.values());
        cbAgeRating.getSelectionModel().selectFirst();

        filmToEdit = ScreenManager.getInstance().getEditingFilm();
        ScreenManager.getInstance().clearEditingFilm();

        loadCategoryChips();
        loadWarningChips();

        if (filmToEdit != null) {
            enterEditMode(filmToEdit);
        }
    }
    private void enterEditMode(Film film) {
        if (lblFormTitle != null) lblFormTitle.setText("Modifier le film");
        if (btnSave != null)      btnSave.setText("✓ Enregistrer les modifications");

        txtTitre.setText(nullSafe(film.getTitre()));
        txtSynopsis.setText(nullSafe(film.getSynopsis()));
        txtCasting.setText(nullSafe(film.getCasting()));
        txtProducteur.setText(nullSafe(film.getProducteur()));
        txtDuree.setText(film.getDuree() > 0 ? String.valueOf(film.getDuree()) : "");
        dpDateSortie.setValue(film.getDateSortie());

        if (film.getAgeRating() != null) cbAgeRating.getSelectionModel().select(film.getAgeRating());

        if (film.getGenres() != null)   { selectedCategories.addAll(film.getGenres());  loadCategoryChips(); }
        if (film.getWarnings() != null) { selectedWarnings.addAll(film.getWarnings());  loadWarningChips();  }

        String cover = film.getUrlImageCover();
        if (notEmpty(cover)) {
            try {
                previewPoster.setImage(new Image(cover, true));
                previewPoster.setVisible(true);
                lblPosterPlaceholder.setVisible(false);
                lblPosterName.setText("(image actuelle)");
                activateDropZone(dropPoster);
            } catch (Exception ignored) {}
        }

        String banner = film.getUrlImageBanner();
        if (notEmpty(banner)) {
            try {
                previewBanner.setImage(new Image(banner, true));
                previewBanner.setVisible(true);
                lblBannerPlaceholder.setVisible(false);
                lblBannerName.setText("(bannière actuelle)");
                activateDropZone(dropBanner);
            } catch (Exception ignored) {}
        }

        if (notEmpty(film.getUrlTeaser())) {
            lblTeaserPlaceholder.setText("✅");
            lblTeaserName.setText("(teaser actuel)");
            activateDropZone(dropTeaser);
        }

        if (notEmpty(film.getUrlVedio())) {
            lblVideoPlaceholder.setText("✅");
            lblVideoName.setText("(vidéo actuelle)");
            activateDropZone(dropVideo);
        }
    }
    private void loadCategoryChips() {
        categoriesPane.getChildren().clear();
        try {
            for (Category cat : categoryService.getAllCategoriesSorted())
                categoriesPane.getChildren().add(buildCategoryChip(cat));
        } catch (Exception e) { showError("Impossible de charger les catégories."); }
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

    @FXML
    private void handleConfirmAddCategory() {
        try {
            String name = txtNewCategory.getText().trim();
            if (name.isEmpty()) { showError("Le nom ne peut pas être vide."); return; }
            categoryService.saveCategory(name);
            categoryService.getAllCategoriesSorted().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(name))
                    .findFirst().ifPresent(selectedCategories::add);
            loadCategoryChips();
            showSuccess("Catégorie « " + name + " » ajoutée !");
            txtNewCategory.clear();
            handleCancelAddCategory();
        } catch (Exception ex) { showError("Erreur: " + ex.getMessage()); }
    }

    @FXML private void handleCancelAddCategory() {
        addCategoryBox.setVisible(false);
        addCategoryBox.setManaged(false);
        txtNewCategory.clear();
    }
    private void loadWarningChips() {
        warningsPane.getChildren().clear();
        try {
            for (Warning w : warningService.getAllWarnings())
                warningsPane.getChildren().add(buildWarningChip(w));
        } catch (Exception e) { showError("Impossible de charger les warnings."); }
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

    @FXML
    private void handleConfirmAddWarning() {
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
            fc.setTitle("Choisir un fichier de sous-titres");
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Sous-titres", "*.srt", "*.vtt", "*.ass"));
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
        row.setStyle("-fx-background-color:#0f0f13; -fx-padding:8; " +
                     "-fx-border-color:#2a2a35; -fx-border-radius:6;");

        btnRemove.setOnAction(e -> {
            subtitlesContainer.getChildren().remove(row);
            subtitleRows.remove(sRow);
        });

        subtitlesContainer.getChildren().add(row);
    }

    private void saveSubtitles(int filmId) {
        for (SubtitleRow sr : subtitleRows) {
            if (sr.langage != null && !sr.langage.isEmpty() && !sr.filePath.isEmpty()) {
                Subtitle sub = new Subtitle(sr.langage, filmId, 0, sr.filePath);
                int result = subtitleService.addSubtitle(sub);
                if (result == 0) {
                    System.err.println("❌ Échec enregistrement sous-titre : " + sr.langage);
                }
            }
        }
    }
    @FXML
    private void handlePickPoster() {
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

    @FXML
    private void handlePickBanner() {
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

    @FXML
    private void handlePickTeaser() {
        File file = pickVideoFile("Sélectionner le teaser / bande-annonce");
        if (file != null) {
            fileTeaser = file;
            lblTeaserName.setText(file.getName());
            lblTeaserPlaceholder.setText("✅");
            activateDropZone(dropTeaser);
            simulateProgress(pbTeaser, "Teaser prêt : " + file.getName());
        }
    }

    @FXML
    private void handlePickVideo() {
        File file = pickVideoFile("Sélectionner la vidéo principale du film");
        if (file != null) {
            fileVideo = file;
            lblVideoName.setText(file.getName());
            lblVideoPlaceholder.setText("✅");
            activateDropZone(dropVideo);
            simulateProgress(pbVideo, "Vidéo prête : " + file.getName());
            try {
                javafx.scene.media.Media media = new javafx.scene.media.Media(file.toURI().toString());
                javafx.scene.media.MediaPlayer mp = new javafx.scene.media.MediaPlayer(media);
                mp.setOnReady(() -> {
                    videoDurationSeconds = (long) media.getDuration().toSeconds();
                    int minutes = (int) (videoDurationSeconds / 60);
                    txtDuree.setText(String.valueOf(minutes));
                    mp.dispose();
                    showSuccess("Durée détectée : " + minutes + " min");
                });
                mp.setOnError(() -> {
                    showError("Impossible de lire la durée de la vidéo.");
                    mp.dispose();
                });
            } catch (Exception e) {
                showError("Erreur lecture vidéo : " + e.getMessage());
            }
        }
    }
    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            NotificationService notificationService = new NotificationService(ConxDB.getInstance());
            int userId = SessionManager.getInstance().getCurrentUser().getId();

            if (filmToEdit == null) {
                
                Film film = buildFilmFromForm(0);
                filmService.enregistrerFilm(film);
                saveSubtitles(film.getId());

                Notification n = new Notification(
                    0,
                    userId,
                    "NOUVEAUTE",
                    "Nouveau film ajouté",
                    "Le film \"" + film.getTitre() + "\" vient d'être ajouté à la plateforme.",
                    java.time.LocalDate.now().toString(),
                    false,
                    false
                );
                notificationService.addNotification(n);

                showSuccess("✓ Film « " + film.getTitre() + " » enregistré !");

            } else {
                
                Film film = buildFilmFromForm(filmToEdit.getId());
                filmService.updateFilm(film);
                saveSubtitles(filmToEdit.getId());

                Notification n = new Notification(
                    0,
                    userId,
                    "MISE_A_JOUR",
                    "Film mis à jour",
                    "Le film \"" + film.getTitre() + "\" a été mis à jour.",
                    java.time.LocalDate.now().toString(),
                    false,
                    false
                );
                notificationService.addNotification(n);

                showSuccess("✓ Film « " + film.getTitre() + " » mis à jour !");
                filmToEdit = null;
            }

            clearForm();

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Film buildFilmFromForm(int id) {
        int duree = 0;
        try { duree = Integer.parseInt(txtDuree.getText().trim()); } catch (NumberFormatException ignored) {}

        String urlCover  = filePoster != null ? filePoster.toURI().toString()
                         : (filmToEdit != null ? nullSafe(filmToEdit.getUrlImageCover())  : "");
        String urlBanner = fileBanner != null ? fileBanner.toURI().toString()
                         : (filmToEdit != null ? nullSafe(filmToEdit.getUrlImageBanner()) : "");
        String urlTeaser = fileTeaser != null ? fileTeaser.toURI().toString()
                         : (filmToEdit != null ? nullSafe(filmToEdit.getUrlTeaser())      : "");
        String urlVedio  = fileVideo  != null ? fileVideo.toURI().toString()
                         : (filmToEdit != null ? nullSafe(filmToEdit.getUrlVedio())       : "");

        Film film = new Film(
                id,
                txtTitre.getText().trim(),
                txtSynopsis.getText().trim(),
                txtCasting.getText().trim(),
                dpDateSortie.getValue(),
                urlCover, urlBanner, urlTeaser,
                filmToEdit != null ? filmToEdit.getRatingMoyen() : 0.0,
                cbAgeRating.getValue(),
                TypeMedia.Film,
                LocalDateTime.now(),
                new HashSet<>(selectedCategories),
                new HashSet<>(selectedWarnings),
                urlVedio,
                duree,
                filmToEdit != null ? filmToEdit.getNbreVue() : 0
        );
        film.setProducteur(txtProducteur.getText().trim());
        return film;
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

        boolean hasPoster = filePoster != null || (filmToEdit != null && notEmpty(filmToEdit.getUrlImageCover()));
        boolean hasBanner = fileBanner != null || (filmToEdit != null && notEmpty(filmToEdit.getUrlImageBanner()));
        boolean hasVideo  = fileVideo  != null || (filmToEdit != null && notEmpty(filmToEdit.getUrlVedio()));

        if (!hasPoster) errors.append("• Affiche (poster) obligatoire.\n");
        if (!hasBanner) errors.append("• Bannière obligatoire.\n");
        if (!hasVideo)  errors.append("• Vidéo principale obligatoire.\n");
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
                      .append(" : veuillez choisir un fichier (.srt / .vtt / .ass).\n");
            }
        }

        if (errors.length() > 0) { showError(errors.toString().trim()); return false; }
        return true;
    }
    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Annuler ? Les modifications non sauvegardées seront perdues.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) { filmToEdit = null; clearForm(); }
        });
    }

    private void clearForm() {
        txtTitre.clear(); txtSynopsis.clear();
        txtCasting.clear(); txtProducteur.clear(); txtDuree.clear();
        dpDateSortie.setValue(null);
        cbAgeRating.getSelectionModel().selectFirst();

        selectedCategories.clear(); selectedWarnings.clear();
        loadCategoryChips(); loadWarningChips();

        filePoster = null; fileBanner = null; fileTeaser = null; fileVideo = null;

        previewPoster.setVisible(false); previewBanner.setVisible(false);
        lblPosterPlaceholder.setVisible(true); lblBannerPlaceholder.setVisible(true);
        lblPosterName.setText(""); lblBannerName.setText("");
        lblTeaserName.setText(""); lblVideoName.setText("");
        lblTeaserPlaceholder.setText("🎞"); lblVideoPlaceholder.setText("▶");

        pbTeaser.setVisible(false); pbTeaser.setProgress(0);
        pbVideo.setVisible(false);  pbVideo.setProgress(0);

        subtitlesContainer.getChildren().clear();
        subtitleRows.clear();

        lblStatus.setText("");
        if (lblFormTitle != null) lblFormTitle.setText("Ajouter un nouveau film");
        if (btnSave      != null) btnSave.setText("✓ Enregistrer le film");
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

    private Stage getCurrentStage() { return (Stage) btnSave.getScene().getWindow(); }

    private void showSuccess(String message) {
        lblStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#4caf50; -fx-padding:0 4 0 4;");
        lblStatus.setText(message);
    }

    private void showError(String message) {
        lblStatus.setStyle("-fx-font-size:12px; -fx-text-fill:#e50914; -fx-padding:0 4 0 4;");
        lblStatus.setText(message);
    }

    private String nullSafe(String s) { return s != null ? s : ""; }
    private boolean notEmpty(String s) { return s != null && !s.isEmpty(); }

    @FXML
    private void handleRetour() {
        filmToEdit = null;
        ScreenManager.getInstance().clearEditingFilm();
        ScreenManager.getInstance().goBack();;
    }
}
