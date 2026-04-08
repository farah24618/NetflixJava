package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.DAO.SaisonDAO;
import tn.farah.NetflixJava.DAO.EpisodeDAO;
import tn.farah.NetflixJava.utils.DatabaseConnection; 
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.net.URL;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class SeriesAdminController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label seriesCountLabel;
    @FXML private VBox seriesListContainer;

    private SerieService serieService;
    private List<Serie> allSeries = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = DatabaseConnection.getConnection(); 
        if (connection == null) return;

        serieService = new SerieService(connection);
        loadData();
    }

    private void loadData() {
        Connection conn = DatabaseConnection.getConnection();
        allSeries = serieService.getAllSeries(); 
        
        SaisonDAO saisonDAO = new SaisonDAO(conn);
        EpisodeDAO episodeDAO = new EpisodeDAO(conn);

        for (Serie s : allSeries) {
            List<Saison> saisons = saisonDAO.findBySerieId(s.getId());
            // Trier les saisons par numéro
            saisons.sort(Comparator.comparingInt(Saison::getNumeroSaison));
            
            for (Saison sa : saisons) {
                List<Episode> episodes = episodeDAO.findBySaisonId(sa.getId());
                // Trier les épisodes par numéro
                episodes.sort(Comparator.comparingInt(Episode::getNumeroEpisode));
                sa.setEpisodes(episodes);
            }
            s.setSaisons(saisons);
        }
        renderAdminList(allSeries);
    }

    private void renderAdminList(List<Serie> source) {
        if (seriesListContainer == null) return;
        seriesListContainer.getChildren().clear();
        if (seriesCountLabel != null) seriesCountLabel.setText("Mes Séries (" + source.size() + ")");

        for (Serie s : source) {
            seriesListContainer.getChildren().add(createSerieRow(s));
        }
    }

    private HBox createSerieRow(Serie s) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 20, 10, 20));
        row.setStyle("-fx-background-color: #141414; -fx-border-color: #222; -fx-border-width: 0 0 1 0;");

        StackPane imgCont = new StackPane();
        imgCont.setPrefSize(110, 65);
        imgCont.setStyle("-fx-background-color: #222;"); 
        loadImageIntoContainer(imgCont, s.getUrlImageCover());

        VBox infos = new VBox(2);
        Label title = new Label(s.getTitre());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        infos.getChildren().add(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuButton saisonBtn = new MenuButton(s.getSaisons().size() + " saisons");
        saisonBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        refreshSaisonMenu(saisonBtn, s);

        Button deleteBtn = new Button("SUPPRIMER SÉRIE");
        deleteBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleSupprimerSerie(s));

        row.getChildren().addAll(imgCont, infos, spacer, saisonBtn, deleteBtn);
        return row;
    }

    private void refreshSaisonMenu(MenuButton btn, Serie s) {
        btn.getItems().clear();
        List<Saison> saisons = s.getSaisons();
        int totalSaisons = saisons.size();

        for (int i = 0; i < totalSaisons; i++) {
            Saison saison = saisons.get(i);
            boolean isLastSaison = (i == totalSaisons - 1);

            Menu saisonMenu = new Menu("Saison " + saison.getNumeroSaison());
            saisonMenu.getStyleClass().add("dark-menu");

            // --- Liste des épisodes ---
            List<Episode> episodes = saison.getEpisodes();
            if (episodes.isEmpty()) {
                MenuItem empty = new MenuItem("Aucun épisode");
                empty.setDisable(true);
                saisonMenu.getItems().add(empty);
            } else {
                for (Episode ep : episodes) {
                    MenuItem epItem = new MenuItem("Ep " + ep.getNumeroEpisode() + " : " + ep.getTitre());
                    epItem.setStyle("-fx-text-fill: white;");
                    saisonMenu.getItems().add(epItem);
                }

                // Bouton : Supprimer DERNIER épisode de cette saison
                saisonMenu.getItems().add(new SeparatorMenuItem());
                MenuItem delLastEp = new MenuItem("🗑 Supprimer dernier épisode");
                delLastEp.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                delLastEp.setOnAction(e -> handleSupprimerDernierEpisode(saison));
                saisonMenu.getItems().add(delLastEp);
            }

            // Bouton : Ajouter épisode
            MenuItem addEp = new MenuItem("+ Ajouter épisode");
            addEp.setStyle("-fx-text-fill: #e50914;");
            addEp.setOnAction(e -> handleAddEpisode(saison));
            saisonMenu.getItems().add(addEp);

            // --- Bouton : Supprimer LA SAISON (Uniquement si c'est la dernière) ---
            if (isLastSaison) {
                saisonMenu.getItems().add(new SeparatorMenuItem());
                MenuItem delSaison = new MenuItem("❌ Supprimer Saison " + saison.getNumeroSaison());
                delSaison.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
                delSaison.setOnAction(e -> handleSupprimerDerniereSaison(s, saison));
                saisonMenu.getItems().add(delSaison);
            }

            btn.getItems().add(saisonMenu);
        }

        btn.getItems().add(new SeparatorMenuItem());
        MenuItem addSaison = new MenuItem("+ Ajouter Saison " + (totalSaisons + 1));
        addSaison.setStyle("-fx-text-fill: #e50914; -fx-font-weight: bold;");
        addSaison.setOnAction(a -> handleAddSaison(s));
        btn.getItems().add(addSaison);
    }

    private void handleSupprimerDerniereSaison(Serie s, Saison saison) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer définitivement la dernière saison ?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            // Appeler votre service de suppression ici
            // serieService.deleteSaison(saison.getId()); 
            s.getSaisons().remove(saison);
            renderAdminList(allSeries);
        }
    }

    private void handleSupprimerDernierEpisode(Saison saison) {
        if (!saison.getEpisodes().isEmpty()) {
            Episode dernier = saison.getEpisodes().get(saison.getEpisodes().size() - 1);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'épisode " + dernier.getNumeroEpisode() + " ?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().get() == ButtonType.YES) {
                // Appeler votre service de suppression ici
                // episodeDAO.delete(dernier.getId());
                saison.getEpisodes().remove(dernier);
                renderAdminList(allSeries);
            }
        }
    }

    private void handleSupprimerSerie(Serie s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer TOUTE la série : " + s.getTitre() + " ?");
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (serieService.deleteSerie(s.getId())) {
                allSeries.remove(s);
                renderAdminList(allSeries);
            }
        }
    }

    private void loadImageIntoContainer(StackPane container, String url) {
        if (url == null || url.trim().isEmpty()) return;
        try {
            String finalUrl = (url.startsWith("http") || url.startsWith("file:")) ? url : "file:" + url;
            Image img = new Image(finalUrl, true); 
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(110); imgView.setFitHeight(65);
            imgView.setPreserveRatio(false);
            container.getChildren().setAll(imgView);
        } catch (Exception e) {
            System.err.println("Erreur image : " + e.getMessage());
        }
    }

    private void handleAddSaison(Serie s) { System.out.println("Ajout Saison"); }
    private void handleAddEpisode(Saison s) { System.out.println("Ajout Episode"); }
    
    @FXML private void onSearch() {
        String q = searchField.getText().toLowerCase();
        renderAdminList(allSeries.stream().filter(s -> s.getTitre().toLowerCase().contains(q)).collect(Collectors.toList()));
    }

    @FXML private void handleNavDashboard() { ScreenManager.getInstance().navigateTo(Screen.home); }
}