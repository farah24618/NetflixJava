package tn.farah.NetflixJava.Controller;

import java.io.IOException;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Scene;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;

import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class ManageUsersController {

    @FXML private VBox usersContainer; 
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo; 
    @FXML private Button tabAll, tabAdmins, tabBlocked;
    @FXML private Label showingLabel;
    @FXML private ComboBox<Integer> rowsPerPageCombo;

    private UserService userSer;
    private List<User> allUsers = new ArrayList<>(); 

    @FXML
    public void initialize() {
    	userSer = new UserService(ConxDB.getInstance());
        
        
        allUsers = userSer.getAllUsers();

        if (sortCombo != null) {
            sortCombo.getItems().clear();
            sortCombo.getItems().addAll("Nom (A-Z)", "Nom (Z-A)", "Email");
            sortCombo.setOnAction(e -> handleSort());
        }
     
        if (rowsPerPageCombo != null) {
            rowsPerPageCombo.getItems().addAll(10, 20, 50);
            rowsPerPageCombo.setValue(10);
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterUsers(newVal);
            });
        }

        renderUsers(allUsers);
        
        updateTabCounts();
        
        handleTabAll();
    }

    private void updateTabCounts() {
        if (tabAll != null) tabAll.setText("Tous " + allUsers.size());
    }


    @FXML
    private void handleNavDashboard() {
        System.out.println("🏠 Vers Dashboard");
        ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    @FXML
    private void handleNavFilms() {
        System.out.println("🎬 Vers Films");
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
    }

    @FXML
    private void handleNavSeries() {
        System.out.println("📺 Vers Séries");
        ScreenManager.getInstance().navigateTo(Screen.ManageSeries);
    }

    @FXML
    private void handleNavNotifications() {
        System.out.println("🔔 Vers Notifications");
        ScreenManager.getInstance().navigateTo(Screen.notificationAdmin);
    }

    @FXML
    private void handleNavComments() {
        System.out.println("💬 Vers Commentaires");
        ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
    }

    @FXML
    private void handleNavSettings() {
        System.out.println("⚙ Vers Paramètres");
        ScreenManager.getInstance().navigateTo(Screen.parametresAdmin);
    }

    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion");
        System.exit(0);
    }

    @FXML
    private void handleSort() {
        String selected = sortCombo.getValue();
        if (selected == null) return;

        List<User> listToSort = new ArrayList<>(allUsers);
        switch (selected) {
            case "Nom (A-Z)":
                listToSort.sort(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Nom (Z-A)":
                listToSort.sort(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "Email":
                listToSort.sort(Comparator.comparing(User::getEmail, String.CASE_INSENSITIVE_ORDER));
                break;
        }
        renderUsers(listToSort);
    }

    private void filterUsers(String query) {
        if (query == null || query.isEmpty()) {
            renderUsers(allUsers);
            return;
        }
        String lowerCaseQuery = query.toLowerCase().trim();
        List<User> filtered = allUsers.stream()
            .filter(u -> 
                (u.getUsername() != null && u.getUsername().toLowerCase().contains(lowerCaseQuery)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(lowerCaseQuery))
            )
            .collect(Collectors.toList());
        renderUsers(filtered);
    }

    private void renderUsers(List<User> list) {
        if (usersContainer == null) return;
        usersContainer.getChildren().clear();
        for (User u : list) {
            usersContainer.getChildren().add(createUserRow(u));
        }
        updatePaginationLabel(list.size());
    }

    private HBox createUserRow(User u) {
        HBox row = new HBox(0); 
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15 30; -fx-border-color: #1a1a1a; -fx-border-width: 0 0 1 0;");

        CheckBox cb = new CheckBox();
        cb.setMinWidth(40);

        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setMinWidth(180);
        
        boolean isAdmin = u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().name());
        Circle avatarCircle = new Circle(15, Color.web(isAdmin ? "#e50914" : "#2a2a2a"));
        
        Label initial = new Label(u.getInitial());
        initial.setTextFill(Color.WHITE);
        StackPane avatar = new StackPane(avatarCircle, initial);
        
        Label nameLabel = new Label(u.getUsername());
        nameLabel.setTextFill(Color.WHITE);
        userBox.getChildren().addAll(avatar, nameLabel);

        Label emailLabel = new Label(u.getEmail());
        emailLabel.setTextFill(Color.web("#808080"));
        emailLabel.setMinWidth(220);

        Label statusBadge = new Label(u.isActive() ? "ACTIF" : "BLOQUÉ");
        statusBadge.setMinWidth(120);
        statusBadge.setAlignment(Pos.CENTER);
        updateStatusBadgeStyle(statusBadge, u.isActive());

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setMinWidth(240);
        
        if (!isAdmin) {
            Button blockBtn = new Button();
            updateBlockButtonStyle(blockBtn, u.isActive());
            blockBtn.setOnAction(event -> {
                u.setActive(!u.isActive());
                if (userSer.updateUser(u)) {
                    userSer.addAuditLog(1, (u.isActive() ? "Déblocage" : "Blocage") + " de : " + u.getUsername());
                    renderUsers(allUsers);
                }
            });
            actions.getChildren().add(blockBtn);
        }
        else {
            javafx.scene.layout.Region placeholder = new javafx.scene.layout.Region();
            placeholder.setMinWidth(100); 
            actions.getChildren().add(placeholder);
        }
        
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-cursor: hand; -fx-font-size: 14px;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText("Supprimer l'utilisateur");
            confirm.setContentText("Voulez-vous vraiment supprimer « " + u.getUsername() + " » ?\nCette action est irréversible.");
          
            confirm.getDialogPane().setStyle("-fx-background-color: #111111;");
            confirm.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");
            confirm.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #1a1a1a;");
            confirm.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");

            ButtonType btnOui = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnNon = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnOui, btnNon);

            confirm.showAndWait().ifPresent(response -> {
                if (response == btnOui) {
                    if (userSer.deleteUser(u.getId())) {
                        allUsers.remove(u);
                        renderUsers(allUsers);
                        updateTabCounts();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur");
                        error.setHeaderText(null);
                        error.setContentText("Impossible de supprimer l'utilisateur.");
                        error.showAndWait();
                    }
                }
            });
        });

        actions.getChildren().addAll(deleteBtn);
        row.getChildren().addAll(cb, userBox, emailLabel, statusBadge, actions);
        return row;
    }
    


    private void updateBlockButtonStyle(Button btn, boolean isActive) {
        btn.setText(isActive ? "Bloquer" : "Débloquer");
        btn.setStyle(isActive ? 
            "-fx-background-color: #222; -fx-text-fill: #e50914; -fx-border-color: #333; -fx-cursor: hand; -fx-background-radius: 6;" :
            "-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");
        btn.setMinWidth(100);
    }

    private void updateStatusBadgeStyle(Label badge, boolean isActive) {
        badge.setStyle(isActive ? 
            "-fx-background-color: #1b3320; -fx-text-fill: #4ade80; -fx-padding: 3 12; -fx-background-radius: 12;" :
            "-fx-background-color: #331b1b; -fx-text-fill: #f87171; -fx-padding: 3 12; -fx-background-radius: 12;");
    }

    private void updatePaginationLabel(int count) {
        if (showingLabel != null) {
            showingLabel.setText("Total : " + count + " utilisateurs");
        }
    }

    @FXML private void handleTabAll() {
        renderUsers(allUsers);
        resetTabStyles();
        if(tabAll != null) tabAll.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-background-radius: 6;");
    }

    @FXML private void handleTabAdmins() {
        List<User> admins = allUsers.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole().name())).collect(Collectors.toList());
        renderUsers(admins);
        resetTabStyles();
        if(tabAdmins != null) tabAdmins.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-background-radius: 6;");
    }

    @FXML private void handleTabBlocked() {
        List<User> blocked = allUsers.stream().filter(u -> !u.isActive()).collect(Collectors.toList());
        renderUsers(blocked);
        resetTabStyles();
        if(tabBlocked != null) tabBlocked.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-background-radius: 6;");
    }

    private void resetTabStyles() {
        String idle = "-fx-background-color: transparent; -fx-text-fill: #808080; -fx-border-color: #2a2a2a; -fx-border-radius: 6;";
        if(tabAll != null) tabAll.setStyle(idle); 
        if(tabAdmins != null) tabAdmins.setStyle(idle); 
        if(tabBlocked != null) tabBlocked.setStyle(idle);
    }
    @FXML
    private void handleAjouterAdmin() {
    	 ScreenManager.getInstance().navigateTo(Screen.ajouterAdmin);
    }
}