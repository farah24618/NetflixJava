package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class AddSaisonnController implements Initializable {

    @FXML private Button btnRetour;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    @FXML private ComboBox<Serie> cbSerie;
    @FXML private TextField txtNumeroSaison;
    @FXML private TextField txtNom;          
    @FXML private DatePicker dpDateSortie;   
    @FXML private Label lblStatus;

    private SaisonService saisonService;
    private SerieService serieService;
    private int editingSaisonId = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Connection connection = ConxDB.getInstance();
        saisonService = new SaisonService(connection);
        serieService  = new SerieService(connection);

        loadSeries();

        Saison saison = ScreenManager.getInstance().getEditingSaison();
        if (saison != null) {
            populateForm(saison);
        }
    }

    private void loadSeries() {
        try {
            List<Serie> series = serieService.getAllSeries();
            cbSerie.getItems().addAll(series);
            cbSerie.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Serie s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : s.getTitre());
                }
            });
            cbSerie.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Serie s, boolean empty) {
                    super.updateItem(s, empty);
                    setText(empty || s == null ? null : s.getTitre());
                }
            });
        } catch (Exception e) {
            showError("Impossible de charger les séries.");
        }
    }

    private void populateForm(Saison saison) {
        txtNumeroSaison.setText(String.valueOf(saison.getNumeroSaison()));

    
        if (saison.getNom() != null && !saison.getNom().isEmpty()) {
            txtNom.setText(saison.getNom());
        }

        if (saison.getDateSortie() != null && dpDateSortie != null) {
            dpDateSortie.setValue(saison.getDateSortie().toLocalDate());
        }

        for (Serie s : cbSerie.getItems()) {
            if (s.getId() == saison.getIdSerie()) {
                cbSerie.setValue(s);
                break;
            }
        }

        if (saison.getId() > 0) {
            editingSaisonId = saison.getId();
            btnSave.setText("💾 Modifier");
            cbSerie.setDisable(true);
            txtNumeroSaison.setDisable(true); 
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (cbSerie.getValue() == null || txtNumeroSaison.getText().isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            int numSaison = Integer.parseInt(txtNumeroSaison.getText().trim());
            int serieId   = cbSerie.getValue().getId();
            String serieTitre = cbSerie.getValue().getTitre();

            String nom = (txtNom != null && !txtNom.getText().trim().isEmpty())
                    ? txtNom.getText().trim()
                    : "Saison " + numSaison;

            LocalDateTime dateSortie = (dpDateSortie != null && dpDateSortie.getValue() != null)
                    ? dpDateSortie.getValue().atStartOfDay()
                    : LocalDateTime.now();

            NotificationService notificationService = new NotificationService(ConxDB.getInstance());
            int userId = SessionManager.getInstance().getCurrentUser().getId();

            if (editingSaisonId > 0) {
               
                Saison updated = new Saison(editingSaisonId, serieId, numSaison, nom, dateSortie);
                saisonService.update(updated);

                Notification n = new Notification(
                    0,
                    userId,
                    "MISE_A_JOUR",
                    "Saison mise à jour",
                    "La saison " + numSaison + " de la série \"" + serieTitre + "\" a été mise à jour.",
                    java.time.LocalDate.now().toString(),
                    false,
                    false
                );
                notificationService.addNotification(n);

                showSuccess("✓ Saison " + numSaison + " modifiée !");

            } else {
    
                Saison newSaison = new Saison(serieId, numSaison, nom, dateSortie);
                int result = saisonService.save(newSaison);
                if (result > 0) {
                    Notification n = new Notification(
                        0,
                        userId,
                        "NOUVEAUTE",
                        "Nouvelle saison ajoutée",
                        "La saison " + numSaison + " a été ajoutée à la série \"" + serieTitre + "\".",
                        java.time.LocalDate.now().toString(),
                        false,
                        false
                    );
                    notificationService.addNotification(n);

                    showSuccess("✓ Saison " + numSaison + " ajoutée !");
                } else {
                    showError("Cette saison existe déjà ou numéro invalide.");
                    return;
                }
            }

            ScreenManager.getInstance().setEditingSaison(null);
            editingSaisonId = -1;
            clearForm();

        } catch (NumberFormatException e) {
            showError("Le numéro de saison doit être un nombre valide.");
        }
    }
    @FXML
    void handleCancel(ActionEvent event) {
        ScreenManager.getInstance().setEditingSaison(null);
        editingSaisonId = -1;
        clearForm();
    }

    @FXML
    void handleRetour(ActionEvent event) {
        ScreenManager.getInstance().setEditingSaison(null);
        editingSaisonId = -1;
        ScreenManager.getInstance().navigateTo(Screen.ManageSeries);
    }

    private void clearForm() {
        txtNumeroSaison.clear();
        txtNumeroSaison.setDisable(false);
        if (txtNom != null) txtNom.clear();
        if (dpDateSortie != null) dpDateSortie.setValue(null);
        cbSerie.getSelectionModel().clearSelection();
        cbSerie.setDisable(false);
        lblStatus.setText("");
        btnSave.setText("✓  Enregistrer la saison");
    }

    private void showSuccess(String msg) {
        lblStatus.setStyle("-fx-text-fill: #4caf50;");
        lblStatus.setText(msg);
    }

    private void showError(String msg) {
        lblStatus.setStyle("-fx-text-fill: #e50914;");
        lblStatus.setText(msg);
    }
}