package tn.farah.NetflixJava.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.farah.NetflixJava.DAO.UserDao;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.utils.DatabaseConnection;

public class ManageUsersController {

    @FXML private VBox usersContainer; 
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo; 
    @FXML private Button tabAll, tabAdmins, tabBlocked;
    @FXML private Label showingLabel;
    @FXML private ComboBox<Integer> rowsPerPageCombo;

    private UserDao dao;
    private List<User> allUsers = new ArrayList<>(); 

    @FXML
    public void initialize() {
        dao = new UserDao(DatabaseConnection.getConnection());
        
        // 1. Charger les données
        allUsers = dao.getAllUsers();
        
        // 2. Initialiser le tri (Français)
        if (sortCombo != null) {
            sortCombo.getItems().clear();
            sortCombo.getItems().addAll("Nom (A-Z)", "Nom (Z-A)", "Email");
            sortCombo.setPromptText("Trier par");
        }
        
        // 3. Initialiser lignes par page
        if (rowsPerPageCombo != null) {
            rowsPerPageCombo.getItems().addAll(10, 20, 50);
            rowsPerPageCombo.setValue(10);
        }

        // 4. Recherche en temps réel
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterUsers(newVal);
        });

        // 5. Affichage initial
        renderUsers(allUsers);
        
        // 6. Mise à jour des textes des onglets (Français)
        tabAll.setText("Tous " + allUsers.size());
        tabAdmins.setText("Administrateurs");
        tabBlocked.setText("Bloqués");
        
        resetTabStyles();
        tabAll.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
    }

    // --- NAVIGATION ---
    @FXML
    private void handleNavDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/farah/NetflixJava/View/Dashboard.fxml"));
            Stage stage = (Stage) usersContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Erreur de redirection : " + e.getMessage());
        }
    }

    // --- LOGIQUE DE TRI ---
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

    // --- LOGIQUE DE FILTRAGE ---
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
        usersContainer.getChildren().clear();
        for (User u : list) {
            usersContainer.getChildren().add(createUserRow(u));
        }
        updatePaginationLabel(list.size());
    }

    // --- CRÉATION DES LIGNES (UI FRANÇAIS) ---
    private HBox createUserRow(User u) {
        HBox row = new HBox(0); 
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15 30; -fx-border-color: #1a1a1a; -fx-border-width: 0 0 1 0;");

        CheckBox cb = new CheckBox();
        cb.setMinWidth(40);

        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setMinWidth(180);
        
        boolean isAdmin = "ADMIN".equalsIgnoreCase(u.getRole().name());
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

        // Badge Statut (Français)
        Label statusBadge = new Label(u.isActive() ? "ACTIF" : "BLOQUÉ");
        statusBadge.setMinWidth(120);
        statusBadge.setAlignment(Pos.CENTER);
        updateStatusBadgeStyle(statusBadge, u.isActive());

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setMinWidth(240);
        
        if (isAdmin) {
            Button logsBtn = new Button("Historique");
            logsBtn.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: #888; -fx-border-color: #333; -fx-background-radius: 5; -fx-cursor: hand;");
            logsBtn.setOnAction(e -> handleViewLogs(u));
            actions.getChildren().add(logsBtn);
        } else {
            Button blockBtn = new Button();
            updateBlockButtonStyle(blockBtn, u.isActive());
            blockBtn.setOnAction(event -> {
                boolean newStatus = !u.isActive();
                u.setActive(newStatus);
                if (dao.updateUser(u)) {
                    // Log en Français
                    String actionMsg = (newStatus ? "Déblocage" : "Blocage") + " de l'utilisateur: " + u.getUsername();
                    dao.addAuditLog(1, actionMsg); 
                    
                    renderUsers(allUsers); // Rafraîchir
                }
            });
            actions.getChildren().add(blockBtn);
        }
        
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555; -fx-cursor: hand; -fx-font-size: 14px;");
        deleteBtn.setOnAction(e -> {
            if (dao.deleteUser(u.getId())) {
                allUsers.remove(u);
                renderUsers(allUsers);
                tabAll.setText("Tous " + allUsers.size());
            }
        });

        actions.getChildren().addAll(deleteBtn);
        row.getChildren().addAll(cb, userBox, emailLabel, statusBadge, actions);
        return row;
    }

    // --- FENÊTRE DE LOGS (FRANÇAIS) ---
    private void handleViewLogs(User u) {
        Stage logStage = new Stage();
        logStage.setTitle("Historique d'audit - " + u.getUsername());

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #0a0a0c; -fx-padding: 25;");
        
        Label title = new Label("Historique d'activité : " + u.getUsername());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #111; -fx-text-fill: #888; -fx-border-color: #222;");

        List<String> logs = dao.getAdminLogs(u.getId());
        logArea.setText(logs.isEmpty() ? "Aucune activité récente enregistrée." : String.join("\n", logs));

        Button closeBtn = new Button("Fermer");
        closeBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> logStage.close());

        root.getChildren().addAll(title, logArea, closeBtn);
        logStage.setScene(new Scene(root, 450, 350));
        logStage.show();
    }

    private void updateBlockButtonStyle(Button btn, boolean isActive) {
        btn.setText(isActive ? "Bloquer" : "Débloquer");
        if (isActive) {
            btn.setStyle("-fx-background-color: #222; -fx-text-fill: #e50914; -fx-border-color: #333; -fx-cursor: hand; -fx-background-radius: 6;");
        } else {
            btn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;");
        }
        btn.setMinWidth(100);
    }

    private void updateStatusBadgeStyle(Label badge, boolean isActive) {
        if (isActive) {
            badge.setStyle("-fx-background-color: #1b3320; -fx-text-fill: #4ade80; -fx-padding: 3 12; -fx-background-radius: 12; -fx-font-size: 10px;");
        } else {
            badge.setStyle("-fx-background-color: #331b1b; -fx-text-fill: #f87171; -fx-padding: 3 12; -fx-background-radius: 12; -fx-font-size: 10px;");
        }
    }

    private void updatePaginationLabel(int count) {
        if (showingLabel != null) {
            showingLabel.setText("Affichage 1-" + Math.min(count, 10) + " sur " + count);
        }
    }

    // --- ONGLETS ---
    @FXML private void handleTabAll() {
        renderUsers(allUsers);
        resetTabStyles();
        tabAll.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
    }

    @FXML private void handleTabAdmins() {
        List<User> admins = allUsers.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole().name())).collect(Collectors.toList());
        renderUsers(admins);
        resetTabStyles();
        tabAdmins.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
    }

    @FXML private void handleTabBlocked() {
        List<User> blocked = allUsers.stream().filter(u -> !u.isActive()).collect(Collectors.toList());
        renderUsers(blocked);
        resetTabStyles();
        tabBlocked.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
    }

    private void resetTabStyles() {
        String idle = "-fx-background-color: transparent; -fx-text-fill: #808080; -fx-border-color: #2a2a2a; -fx-border-radius: 6; -fx-background-radius: 6;";
        tabAll.setStyle(idle); tabAdmins.setStyle(idle); tabBlocked.setStyle(idle);
    }

    // --- AUTRES HANDLERS ---
    @FXML private void handleLogout() { System.exit(0); }
    @FXML private void handleNavFilms() {}
    @FXML private void handleNavSeries() {}
    @FXML private void handleNavComments() {}
    @FXML private void handleNavNotifications() {}
    @FXML private void handleNavSettings() {}
    @FXML private void handleSelectAll() {}
    @FXML private void handleRowsPerPage() {}
    @FXML private void handleDeleteSelected() {}
}