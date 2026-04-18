package tn.farah.NetflixJava.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import javafx.beans.property.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NotificationsAdminController implements Initializable {

   
    @FXML private Label unreadBadgeLabel;

    @FXML private Button btnAll;
    @FXML private Button btnUnread;
    @FXML private Button btnSent;

    @FXML private Label allCountLabel;
    @FXML private Label unreadCountLabel;
    @FXML private Label sentCountLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, String>  messageColumn;
    @FXML private TableColumn<Notification, Integer> userColumn;
    @FXML private TableColumn<Notification, String>  dateColumn;
    @FXML private TableColumn<Notification, Boolean> statusColumn;
    @FXML private CheckBox selectAllCheckbox;
 
    @FXML private TableColumn<Notification, Void> actionsColumn;

    private final NotificationService notificationService =
            new NotificationService(ConxDB.getInstance());

    private ObservableList<Notification> masterData = FXCollections.observableArrayList();

    private int currentUserId = 1;

    private String activeFilter = "ALL";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        refreshData();
        applyFilter("ALL");

        notificationsTable.setRowFactory(tv -> {
            TableRow<Notification> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Notification notif = row.getItem();
                    if (!notif.isRead()) {
                        notificationService.markAsRead(notif.getId());
                        refreshData();
                        System.out.println("✅ Notif #" + notif.getId() + " marquée comme lue");
                    }
                }
            });
            return row;
        });
    }

    private void setupTableColumns() {
        messageColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getMessage()));

        userColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());

        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getDate() != null
                ? cellData.getValue().getDate().toString()
                : ""));

        statusColumn.setCellValueFactory(cellData ->
            new SimpleBooleanProperty(cellData.getValue().isRead()).asObject());

        statusColumn.setCellFactory(column -> new TableCell<Notification, Boolean>() {
            @Override
            protected void updateItem(Boolean isRead, boolean empty) {
                super.updateItem(isRead, empty);
                if (empty || isRead == null) {
                    setText(null);
                } else {
                    setText(isRead ? "Lu" : "Non lu");
                    setTextFill(isRead ? Color.GRAY : Color.CHARTREUSE);
                }
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<Notification, Void>() {
            private final Button btnRead   = new Button("✓ Lu");
            private final Button btnDelete = new Button("🗑");

            {
                btnRead.setStyle(
                    "-fx-background-color: #1e8a3e; -fx-text-fill: white; " +
                    "-fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: HAND;"
                );
                btnRead.setOnAction(e -> {
                    Notification notif = getTableView().getItems().get(getIndex());
                    notificationService.markAsRead(notif.getId());
                    refreshData();
                });

                btnDelete.setStyle(
                    "-fx-background-color: #E50914; -fx-text-fill: white; " +
                    "-fx-background-radius: 5; -fx-padding: 4 8; -fx-cursor: HAND;"
                );
                btnDelete.setOnAction(e -> {
                    Notification notif = getTableView().getItems().get(getIndex());
                    notificationService.deleteNotification(notif.getId());
                    refreshData();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Notification notif = getTableView().getItems().get(getIndex());
                    btnRead.setVisible(!notif.isRead());
                    HBox box = new HBox(5, btnRead, btnDelete);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }


    private void refreshData() {
    
        List<Notification> data;
        switch (activeFilter) {
            case "UNREAD":
                data = notificationService.getUnreadNotifications(currentUserId);
                break;
            case "SENT":
                
            	data = notificationService.getSentByAdmin(currentUserId);
                break;
            default: 
                data = notificationService.getUserNotificationsAdmin();
                break;
        }
    
        System.out.println("=== refreshData() ===");
        System.out.println("Filter: " + activeFilter);
        System.out.println("CurrentUserId: " + currentUserId);
        System.out.println("Nombre de notifications reçues: " + data.size());
        for (Notification n : data) {
            System.out.println("  -> id=" + n.getId() 
                + " | userId=" + n.getUserId() 
                + " | message=" + n.getMessage()
                + " | isRead=" + n.isRead());
        }
        masterData.setAll(data);
        notificationsTable.setItems(masterData);

        int total  = notificationService.countAllAdmin();
        int unread = notificationService.countUnread(currentUserId);

        if (unreadBadgeLabel != null) unreadBadgeLabel.setText(String.valueOf(unread));
        if (allCountLabel    != null) allCountLabel.setText(String.valueOf(total));
        if (unreadCountLabel != null) unreadCountLabel.setText(String.valueOf(unread));

        int sent = notificationService.countSentByAdmin();
        if (sentCountLabel != null) sentCountLabel.setText(String.valueOf(sent));
    }

    private void applyFilter(String filter) {
        this.activeFilter = filter;

        String activeStyle   = "-fx-background-color: #E50914; -fx-text-fill: white; "
                             + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20;";
        String inactiveStyle = "-fx-background-color: #1e1e1e; -fx-text-fill: #888888; "
                             + "-fx-background-radius: 8; -fx-padding: 8 20;";

        if (btnAll    != null) btnAll.setStyle(   "ALL".equals(filter)    ? activeStyle : inactiveStyle);
        if (btnUnread != null) btnUnread.setStyle("UNREAD".equals(filter) ? activeStyle : inactiveStyle);
        if (btnSent   != null) btnSent.setStyle(  "SENT".equals(filter)   ? activeStyle : inactiveStyle);

        refreshData();
    }

    @FXML
    void onAllClicked(ActionEvent event) {
        applyFilter("ALL");
    }

    @FXML
    void onUnreadClicked(ActionEvent event) {
        applyFilter("UNREAD");
    }

    @FXML
    void onSentClicked(ActionEvent event) {
        applyFilter("SENT");
    }


    @FXML
    void onSearch(KeyEvent event) {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
          
            refreshData();
        } else {
           
            List<Notification> results = notificationService.search(currentUserId, query);
            notificationsTable.setItems(FXCollections.observableArrayList(results));
        }
    }


   
    @FXML
    void onNewNotification(ActionEvent event) {
        Dialog<Notification> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Notification");

        TextField titleField = new TextField();
        titleField.setPromptText("Titre");
        TextField messageField = new TextField();
        messageField.setPromptText("Message");
        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #E50914; -fx-font-size: 12px;");

        VBox content = new VBox(8,
            new Label("Titre:"), titleField,
            new Label("Message:"), messageField,
            new Label("User ID:"), userIdField,
            errorLabel  
        );
        content.setStyle("-fx-padding: 10;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

        userIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                errorLabel.setText("");
                okButton.setDisable(true);
                return;
            }
            try {
                int uid = Integer.parseInt(newVal);
                if (notificationService.userExists(uid)) {
                    errorLabel.setText("✅ Utilisateur trouvé");
                    errorLabel.setStyle("-fx-text-fill: green;");
                    okButton.setDisable(false);
                } else {
                    errorLabel.setText("❌ Utilisateur introuvable en base de données");
                    errorLabel.setStyle("-fx-text-fill: #E50914;");
                    okButton.setDisable(true); // ← bloque l'envoi
                }
            } catch (NumberFormatException e) {
                errorLabel.setText("⚠ Entrez un nombre valide");
                errorLabel.setStyle("-fx-text-fill: orange;");
                okButton.setDisable(true);
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                int uid = Integer.parseInt(userIdField.getText());
                return new Notification(
                    0, uid, "INFO",
                    titleField.getText(),
                    messageField.getText(),
                    "", false, false
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(notif -> {
            notificationService.addNotification(notif);
            refreshData();
            System.out.println("✅ Notification envoyée à userId=" + notif.getUserId());
        });
    }
  

    @FXML
    void onSelectAll(ActionEvent event) {
        
    }

   
    @FXML void onDashboardClicked(ActionEvent e) { ScreenManager.getInstance().navigateTo(Screen.AdminDashboard); }
    @FXML void onFilmsClicked(ActionEvent e)     { ScreenManager.getInstance().navigateTo(Screen.admin_main); }
    @FXML void onSeriesClicked(ActionEvent e)    { 
    	ScreenManager.getInstance().navigateTo(Screen.ManageSeries);
    }
    @FXML void onUsersClicked(ActionEvent e)     {
    	ScreenManager.getInstance().navigateTo(Screen.manageUsers);}
    @FXML void onCommentsClicked(ActionEvent e)  {
    	
    	ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin); }
    @FXML void onSettingsClicked(ActionEvent e)  { ScreenManager.getInstance().navigateTo(Screen.parametresAdmin); }

    @FXML
    void onLogoutClicked(ActionEvent e) {
        System.exit(0);
    }
    @FXML
    void onMarkAllRead(ActionEvent event) {
        notificationService.markAllAsRead(currentUserId);
        refreshData();
    }
}