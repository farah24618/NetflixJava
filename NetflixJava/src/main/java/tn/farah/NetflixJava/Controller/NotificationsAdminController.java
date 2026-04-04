package tn.farah.NetflixJava.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.utils.ConxDB;

import java.net.URL;
import java.util.ResourceBundle;

public class NotificationsAdminController implements Initializable {

    @FXML private ImageView logoImage;
    @FXML private TextField searchField;
    
    // Labels pour les compteurs dynamiques
    @FXML private Label unreadBadgeLabel;  // Badge rouge dans la sidebar
    @FXML private Label allCountLabel;     // Chiffre dans le bouton "All"
    @FXML private Label unreadCountLabel;  // Chiffre dans le bouton "Unread"
    @FXML private Label sentCountLabel;    // Chiffre dans le bouton "Sent"

    // TableView
    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, String> messageColumn;
    @FXML private TableColumn<Notification, Integer> userColumn;
    @FXML private TableColumn<Notification, String> dateColumn;
    @FXML private TableColumn<Notification, Boolean> statusColumn;
    @FXML private CheckBox selectAllCheckbox;

    // Service
    private final NotificationService notificationService = new NotificationService(ConxDB.getInstance());
    private ObservableList<Notification> masterData = FXCollections.observableArrayList();
    
    // ID utilisateur (statique pour l'exemple, à lier à votre session)
    private int currentUserId = 1; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        refreshData();
    }

    /**
     * Configure la liaison entre les colonnes du tableau et l'entité Notification
     */
    private void setupTableColumns() {
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        // Formatter visuel pour la colonne Status
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("isRead"));
        statusColumn.setCellFactory(column -> new TableCell<Notification, Boolean>() {
            @Override
            protected void updateItem(Boolean isRead, boolean empty) {
                super.updateItem(isRead, empty);
                if (empty || isRead == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(isRead ? "Lu" : "Non lu");
                    // Gris si lu, Vert (Chartreuse) si non lu
                    setTextFill(isRead ? javafx.scene.paint.Color.GRAY : javafx.scene.paint.Color.CHARTREUSE);
                }
            }
        });
    }

    /**
     * Met à jour le tableau et tous les compteurs de l'interface
     */
    private void refreshData() {
        // 1. Mise à jour de la liste principale du tableau
        masterData.setAll(notificationService.getUserNotifications(currentUserId));
        notificationsTable.setItems(masterData);

        // 2. Récupération des statistiques depuis le service
        int total = notificationService.countAll(currentUserId);
        int unread = notificationService.countUnread(currentUserId);

        // 3. Mise à jour dynamique des éléments UI
        
        // Badge de la barre latérale
        if (unreadBadgeLabel != null) {
            unreadBadgeLabel.setText(String.valueOf(unread));
        }

        // Compteur du bouton "All"
        if (allCountLabel != null) {
            allCountLabel.setText(String.valueOf(total));
        }

        // Compteur du bouton "Unread"
        if (unreadCountLabel != null) {
            unreadCountLabel.setText(String.valueOf(unread));
        }

        // Compteur "Sent" (exemple mis à 0 ou à gérer selon votre logique)
        if (sentCountLabel != null) {
            sentCountLabel.setText("0"); 
        }
    }

    @FXML
    void onSearch(KeyEvent event) {
        String query = searchField.getText().toLowerCase();
        FilteredList<Notification> filteredData = new FilteredList<>(masterData, n -> {
            if (query == null || query.isEmpty()) return true;
            return n.getMessage().toLowerCase().contains(query) || 
                   n.getTitle().toLowerCase().contains(query);
        });
        notificationsTable.setItems(filteredData);
    }

    @FXML
    void onNewNotification(ActionEvent event) {
        // Création d'une notification de test
        Notification newNotif = new Notification(0, currentUserId, "Info", "Nouveau", "Test message", "", false, false);
        notificationService.addNotification(newNotif);
        
        // Rafraîchir tout (tableau + chiffres)
        refreshData();
    }

    @FXML
    void onSelectAll(ActionEvent event) {
        // Logique pour sélectionner toutes les lignes
    }

    // --- Actions de Navigation ---

    @FXML void onDashboardClicked(ActionEvent event) { System.out.println("Aller au Dashboard"); }
    @FXML void onFilmsClicked(ActionEvent event) { System.out.println("Aller aux Films"); }
    @FXML void onSeriesClicked(ActionEvent event) { System.out.println("Aller aux Séries"); }
    @FXML void onUsersClicked(ActionEvent event) { System.out.println("Aller aux Utilisateurs"); }
    @FXML void onCommentsClicked(ActionEvent event) { System.out.println("Aller aux Commentaires"); }
    @FXML void onFavoritesClicked(ActionEvent event) { System.out.println("Aller aux Favoris"); }
    @FXML void onSettingsClicked(ActionEvent event) { System.out.println("Aller aux Paramètres"); }
    
    @FXML
    void onLogoutClicked(ActionEvent event) {
        // Fermeture propre de l'application
        System.exit(0);
    }
}