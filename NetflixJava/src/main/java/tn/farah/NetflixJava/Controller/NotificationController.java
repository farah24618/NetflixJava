package tn.farah.NetflixJava.Controller;

import java.sql.Connection;
import java.util.List;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationController {

    @FXML
    private ListView<Notification> notificationListView;

    @FXML
    private Label badgeLabel;
    private Connection conx;
    private NotificationService service ;
    private ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private int currentUserId;
    @FXML
    public void initialize() {
    	this.currentUserId = SessionManager.getInstance().getCurrentUserId();
    	this.conx=ConxDB.getInstance();
    	this.service=new NotificationService(conx);
        
        setupListViewDesign();
        
        loadNotifications();
        setupClickListener();
        startAutoRefresh();
    }
    @FXML
    private void onBack() {
        ScreenManager.getInstance().navigateTo(Screen.home); // adapte si ton Screen enum est différent
    }

    // ✨ DESIGN NETFLIX PRO : Transforme chaque ligne en "Carte"
    private void setupListViewDesign() {
        notificationListView.setCellFactory(param -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // 1. Conteneur principal (La Carte)
                    VBox card = new VBox(8);
                    card.setPadding(new Insets(15, 20, 15, 20));
                    
                    // Styles CSS (Fond sombre, bordures discrètes)
                    String defaultStyle = "-fx-background-color: #141414; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;";
                    String hoverStyle = "-fx-background-color: #2b2b2b; -fx-cursor: hand; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;";
                    card.setStyle(defaultStyle);

                    // 2. Entête (Titre + Indicateur de lecture)
                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);
                    
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
                    
                    header.getChildren().add(title);
                    
                    // Petit point rouge si non lu
                    if (!item.isRead()) {
                        Label dot = new Label("●");
                        dot.setStyle("-fx-text-fill: #E50914; -fx-font-size: 12px;");
                        header.getChildren().add(dot);
                    }

                    // 3. Message (Gris clair)
                    Label message = new Label(item.getMessage());
                    message.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 13px;");
                    message.setWrapText(true);

                    // 4. Date (Gris foncé)
                    Label date = new Label(item.getDate());
                    date.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");

                    card.getChildren().addAll(header, message, date);

                    // Effets de survol (Hover)
                    card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
                    card.setOnMouseExited(e -> card.setStyle(defaultStyle));

                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            }
        });
    }

    private void loadNotifications() {
        List<Notification> list = service.getUserNotifications(currentUserId);
        notifications.setAll(list);
        notificationListView.setItems(notifications);
        updateBadge();
    }

    private void updateBadge() {
        if (badgeLabel != null) {
            int count = service.countUnread(currentUserId);
            badgeLabel.setText(String.valueOf(count));
            badgeLabel.setVisible(count > 0);
        }
    }

    private void setupClickListener() {
        notificationListView.setOnMouseClicked(event -> {
            Notification selected = notificationListView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead()) {
                service.markAsRead(selected.getId());
                selected.setRead(true);
                updateBadge();
                notificationListView.refresh();
            }
        });
    }

    private void startAutoRefresh() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> loadNotifications()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML private void showAllNotifications() { notificationListView.setItems(notifications); }
    @FXML private void showSeriesNotifications() { filter("SÉRIE"); }
    @FXML private void showPaymentNotifications() { filter("PAIEMENT"); }
    @FXML private void showCommentNotifications() { filter("COMMENTAIRE"); }

    private void filter(String type) {
        ObservableList<Notification> filtered = FXCollections.observableArrayList();
        for (Notification n : notifications) {
            if (n.getType().equalsIgnoreCase(type)) filtered.add(n);
        }
        notificationListView.setItems(filtered);
    }
}