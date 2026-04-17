/*package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;

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
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
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
        Connection connection = ConxDB.getInstance();
        serieService = new SerieService(connection);
        loadData(connection);
    }

    private void loadData(Connection conn) {
        allSeries = serieService.getAllSeries();
        
        SaisonService saisonSer = new SaisonService(conn);
        EpisodeService episodeSer = new EpisodeService(conn);

        for (Serie s : allSeries) {
            List<Saison> saisons = saisonSer.findBySerieId(s.getId());
            saisons.sort(Comparator.comparingInt(Saison::getNumeroSaison));
            
            for (Saison sa : saisons) {
                List<Episode> episodes = episodeSer.findBySaisonId(sa.getId());
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
        
        Button UpdateBtn = new Button("Modifier SÉRIE");
        UpdateBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        UpdateBtn.setOnAction(e -> handleUpdateSerie(s)); 
        row.getChildren().addAll(imgCont, infos, spacer, saisonBtn,UpdateBtn, deleteBtn);
        return row;
    }
    private void handleUpdateSerie(Serie s) {
        ScreenManager sm = ScreenManager.getInstance();
        sm.setEditingSerie(s);
        sm.navigateTo(Screen.addSerie);
    }

    private void refreshSaisonMenu(MenuButton btn, Serie s) {
        btn.getItems().clear();
        List<Saison> saisons = s.getSaisons();
        int totalSaisons = saisons.size();

        for (int i = 0; i < totalSaisons; i++) {
            Saison saison = saisons.get(i);
            boolean isLastSaison = (i == totalSaisons - 1);

            Menu saisonMenu = new Menu("Saison " + saison.getNumeroSaison());

            // ✅ NOUVEAU : Modifier la saison (clic sur "✏️ Modifier Saison X")
            MenuItem editSaisonItem = new MenuItem("✏️ Modifier Saison " + saison.getNumeroSaison());
            editSaisonItem.setStyle("-fx-text-fill: #00aaff;");
            editSaisonItem.setOnAction(e -> {
                ScreenManager.getInstance().setEditingSaison(saison);
                ScreenManager.getInstance().navigateTo(Screen.addSaison);
            });
            saisonMenu.getItems().add(editSaisonItem);
            saisonMenu.getItems().add(new SeparatorMenuItem()); // séparateur visuel

            // ─── Épisodes ───────────────────────────────────────────
            List<Episode> episodes = saison.getEpisodes();
            if (episodes.isEmpty()) {
                MenuItem empty = new MenuItem("Aucun épisode");
                empty.setDisable(true);
                saisonMenu.getItems().add(empty);
            } else {
                for (Episode ep : episodes) {
                    MenuItem epItem = new MenuItem("Ep " + ep.getNumeroEpisode() + " : " + ep.getTitre());
                    epItem.setOnAction(e -> {
                        ScreenManager.getInstance().setEditingEpisode(ep);
                        ScreenManager.getInstance().navigateTo(Screen.addEpisode);
                    });
                    saisonMenu.getItems().add(epItem);
                }

                saisonMenu.getItems().add(new SeparatorMenuItem());
                MenuItem delLastEp = new MenuItem("🗑 Supprimer dernier épisode");
                delLastEp.setStyle("-fx-text-fill: orange;");
                delLastEp.setOnAction(e -> handleSupprimerDernierEpisode(saison));
                saisonMenu.getItems().add(delLastEp);
            }

            MenuItem addEp = new MenuItem("+ Ajouter épisode");
            addEp.setStyle("-fx-text-fill: #e50914;");
            addEp.setOnAction(e -> handleAddEpisode(saison));
            saisonMenu.getItems().add(addEp);

            // ─── Supprimer saison (dernière uniquement) ──────────────
            if (isLastSaison) {
                saisonMenu.getItems().add(new SeparatorMenuItem());
                MenuItem delSaison = new MenuItem("❌ Supprimer Saison " + saison.getNumeroSaison());
                delSaison.setStyle("-fx-text-fill: #ff4444;");
                delSaison.setOnAction(e -> handleSupprimerDerniereSaison(s, saison));
                saisonMenu.getItems().add(delSaison);
            }

            btn.getItems().add(saisonMenu);
        }

        // ✅ NOUVEAU : Ajouter saison avec numéro automatique pré-rempli
        btn.getItems().add(new SeparatorMenuItem());
        MenuItem addSaison = new MenuItem("+ Ajouter Saison " + (totalSaisons + 1));
        addSaison.setStyle("-fx-text-fill: #e50914;");
        addSaison.setOnAction(a -> handleAddSaison(s));
        btn.getItems().add(addSaison);
    }
    private void handleSupprimerDerniereSaison(Serie s, Saison saison) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer définitivement la saison " + saison.getNumeroSaison() + " ?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            s.getSaisons().remove(saison);
            renderAdminList(allSeries);
        }
    }

    private void handleSupprimerDernierEpisode(Saison saison) {
        if (!saison.getEpisodes().isEmpty()) {
            Episode dernier = saison.getEpisodes().get(saison.getEpisodes().size() - 1);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l'épisode " + dernier.getNumeroEpisode() + " ?", ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                saison.getEpisodes().remove(dernier);
                renderAdminList(allSeries);
            }
        }
    }

    private void handleSupprimerSerie(Serie s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer TOUTE la série : " + s.getTitre() + " ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
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

    private void handleAddSaison(Serie s) {
        // Calculer le prochain numéro automatiquement
        int nextNum = s.getSaisons().size() + 1;
        
        Saison newSaison = new Saison();
        newSaison.setIdSerie(s.getId());
        newSaison.setNumeroSaison(nextNum); // ✅ Pré-rempli automatiquement
        
        ScreenManager.getInstance().setEditingSaison(newSaison);
        ScreenManager.getInstance().navigateTo(Screen.addSaison);
    }
    private void handleAddEpisode(Saison s) { ScreenManager.getInstance().navigateTo(Screen.addEpisode);}

    @FXML 
    private void onSearch() {
        String q = searchField.getText().toLowerCase();
        renderAdminList(allSeries.stream()
                .filter(s -> s.getTitre().toLowerCase().contains(q))
                .collect(Collectors.toList()));
    }

    // --- NAVIGATION ---
    @FXML
    private void handleNavDashboard(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    @FXML
    private void handleNavUsers(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.manageUsers);
    }

    @FXML
    private void handleNavFilms(ActionEvent event) {
        // J'ai gardé parametresAdmin comme tu l'avais écrit, 
        // mais vérifie si tu n'as pas un Screen.manageFilms à la place.
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
        
    }  
        
     // --- NAVIGATION DYNAMIQUE ---

        

       
       
        @FXML
        private void handleNavNotif(ActionEvent event) {
            System.out.println("Aller vers Notifications");
             ScreenManager.getInstance().navigateTo(Screen.notificationAdmin); 
        }

        @FXML
        private void handleNavComments(ActionEvent event) {
            System.out.println("Aller vers Commentaires");
             ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
        }

        @FXML
        private void handleNavSettings(ActionEvent event) {
            System.out.println("Aller vers Paramètres");
            ScreenManager.getInstance().navigateTo(Screen.parametresAdmin);
        }    
        */
package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
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
import tn.farah.NetflixJava.Service.EpisodeService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
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
    private SaisonService saisonService;
    private EpisodeService episodeService;

    private List<Serie> allSeries = new ArrayList<>();
    private Map<Integer, List<Saison>> saisonsMap = new HashMap<>();
    private Map<Integer, List<Episode>> episodesMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Connection connection = ConxDB.getInstance();
        serieService = new SerieService(connection);
        saisonService = new SaisonService(connection);
        episodeService = new EpisodeService(connection);
        loadData();
    }

    private void loadData() {
        allSeries = serieService.getAllSeries();

        for (Serie s : allSeries) {
            List<Saison> saisons = saisonService.findBySerieId(s.getId());
            saisons.sort(Comparator.comparingInt(Saison::getNumeroSaison));
            saisonsMap.put(s.getId(), saisons);

            for (Saison sa : saisons) {
                List<Episode> episodes = episodeService.findBySaisonId(sa.getId());
                episodes.sort(Comparator.comparingInt(Episode::getNumeroEpisode));
                episodesMap.put(sa.getId(), episodes);
            }
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

        List<Saison> saisons = saisonsMap.getOrDefault(s.getId(), new ArrayList<>());

        MenuButton saisonBtn = new MenuButton(saisons.size() + " saisons");
        saisonBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        refreshSaisonMenu(saisonBtn, s, saisons);

        Button deleteBtn = new Button("SUPPRIMER SÉRIE");
        deleteBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleSupprimerSerie(s));

        Button UpdateBtn = new Button("Modifier SÉRIE");
        UpdateBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        UpdateBtn.setOnAction(e -> handleUpdateSerie(s));

        row.getChildren().addAll(imgCont, infos, spacer, saisonBtn, UpdateBtn, deleteBtn);
        return row;
    }

    private void handleUpdateSerie(Serie s) {
        ScreenManager sm = ScreenManager.getInstance();
        sm.setEditingSerie(s);
        sm.navigateTo(Screen.addSerie);
    }

    private void refreshSaisonMenu(MenuButton btn, Serie s, List<Saison> saisons) {
        btn.getItems().clear();
        int totalSaisons = saisons.size();

        for (int i = 0; i < totalSaisons; i++) {
            Saison saison = saisons.get(i);
            boolean isLastSaison = (i == totalSaisons - 1);

            Menu saisonMenu = new Menu("Saison " + saison.getNumeroSaison());

            MenuItem editSaisonItem = new MenuItem("✏️ Modifier Saison " + saison.getNumeroSaison());
            editSaisonItem.setStyle("-fx-text-fill: #00aaff;");
            editSaisonItem.setOnAction(e -> {
                ScreenManager.getInstance().setEditingSaison(saison);
                ScreenManager.getInstance().navigateTo(Screen.addSaison);
            });
            saisonMenu.getItems().add(editSaisonItem);
            saisonMenu.getItems().add(new SeparatorMenuItem());

            List<Episode> episodes = episodesMap.getOrDefault(saison.getId(), new ArrayList<>());

            if (episodes.isEmpty()) {
                MenuItem empty = new MenuItem("Aucun épisode");
                empty.setDisable(true);
                saisonMenu.getItems().add(empty);
            } else {
                for (Episode ep : episodes) {
                    MenuItem epItem = new MenuItem("Ep " + ep.getNumeroEpisode() + " : " + ep.getTitre());
                    epItem.setOnAction(e -> {
                        ScreenManager.getInstance().setEditingEpisode(ep);
                        ScreenManager.getInstance().navigateTo(Screen.addEpisode);
                    });
                    saisonMenu.getItems().add(epItem);
                }

                saisonMenu.getItems().add(new SeparatorMenuItem());
                MenuItem delLastEp = new MenuItem("🗑 Supprimer dernier épisode");
                delLastEp.setStyle("-fx-text-fill: orange;");
                delLastEp.setOnAction(e -> handleSupprimerDernierEpisode(saison, episodes));
                saisonMenu.getItems().add(delLastEp);
            }

            MenuItem addEp = new MenuItem("+ Ajouter épisode");
            addEp.setStyle("-fx-text-fill: #e50914;");
            addEp.setOnAction(e -> handleAddEpisode(saison));
            saisonMenu.getItems().add(addEp);

            if (isLastSaison) {
                saisonMenu.getItems().add(new SeparatorMenuItem());
                MenuItem delSaison = new MenuItem("❌ Supprimer Saison " + saison.getNumeroSaison());
                delSaison.setStyle("-fx-text-fill: #ff4444;");
                delSaison.setOnAction(e -> handleSupprimerDerniereSaison(s, saison, saisons));
                saisonMenu.getItems().add(delSaison);
            }

            btn.getItems().add(saisonMenu);
        }

        btn.getItems().add(new SeparatorMenuItem());
        MenuItem addSaison = new MenuItem("+ Ajouter Saison " + (totalSaisons + 1));
        addSaison.setStyle("-fx-text-fill: #e50914;");
        addSaison.setOnAction(a -> handleAddSaison(s, saisons));
        btn.getItems().add(addSaison);
    }

    private void handleSupprimerDerniereSaison(Serie s, Saison saison, List<Saison> saisons) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer définitivement la saison " + saison.getNumeroSaison() + " ?",
                ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            saisons.remove(saison);
            episodesMap.remove(saison.getId());
            renderAdminList(allSeries);
        }
    }

    private void handleSupprimerDernierEpisode(Saison saison, List<Episode> episodes) {
        if (!episodes.isEmpty()) {
            Episode dernier = episodes.get(episodes.size() - 1);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Supprimer l'épisode " + dernier.getNumeroEpisode() + " ?",
                    ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                episodes.remove(dernier);
                renderAdminList(allSeries);
            }
        }
    }

    private void handleSupprimerSerie(Serie s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer TOUTE la série : " + s.getTitre() + " ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (serieService.deleteSerie(s.getId())) {
                saisonsMap.getOrDefault(s.getId(), new ArrayList<>())
                          .forEach(sa -> episodesMap.remove(sa.getId()));
                saisonsMap.remove(s.getId());
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

    private void handleAddSaison(Serie s, List<Saison> saisons) {
        int nextNum = saisons.size() + 1;

        Saison newSaison = new Saison();
        newSaison.setIdSerie(s.getId());
        newSaison.setNumeroSaison(nextNum);

        ScreenManager.getInstance().setEditingSaison(newSaison);
        ScreenManager.getInstance().navigateTo(Screen.addSaison);
    }

    private void handleAddEpisode(Saison s) {
        ScreenManager.getInstance().navigateTo(Screen.addEpisode);
    }

    @FXML
    private void onSearch() {
        String q = searchField.getText().toLowerCase();
        renderAdminList(allSeries.stream()
                .filter(s -> s.getTitre().toLowerCase().contains(q))
                .collect(Collectors.toList()));
    }

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.AdminDashboard);
    }

    @FXML
    private void handleNavUsers(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.manageUsers);
    }

    @FXML
    private void handleNavFilms(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.admin_main);
    }

    @FXML
    private void handleNavNotif(ActionEvent event) {
        System.out.println("Aller vers Notifications");
        ScreenManager.getInstance().navigateTo(Screen.notificationAdmin);
    }

    @FXML
    private void handleNavComments(ActionEvent event) {
        System.out.println("Aller vers Commentaires");
        ScreenManager.getInstance().navigateTo(Screen.CommentaireAdmin);
    }

    @FXML
    private void handleNavSettings(ActionEvent event) {
        System.out.println("Aller vers Paramètres");
        ScreenManager.getInstance().navigateTo(Screen.parametresAdmin);
    }
}
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    