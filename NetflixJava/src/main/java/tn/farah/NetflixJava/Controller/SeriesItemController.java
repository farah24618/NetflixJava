package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.DAO.SaisonDAO;
import tn.farah.NetflixJava.DAO.SerieDAO; // Importé pour utiliser findEpisodeBySaison
import tn.farah.NetflixJava.utils.ConxDB;

import java.util.List;

public class SeriesItemController {

    @FXML private Label titleLabel;
    @FXML private Label yearLabel;
    @FXML private Label seasonsLabel;      // État: Terminée/En cours
    @FXML private Label viewsLabel;        // Note / Rating
    @FXML private Label commentsLabel;     // Genre
    @FXML private StackPane imageContainer; // Conteneur pour la photo (doit être dans le FXML)
    @FXML private MenuButton episodesMenuBtn; 

    private Serie currentSerie;
    private final SaisonDAO saisonDAO = new SaisonDAO(ConxDB.getInstance());
    private final SerieDAO serieDAO = new SerieDAO(ConxDB.getInstance()); // Pour récupérer les épisodes

    public void setSeriesData(Serie serie) {
        this.currentSerie = serie;
        
        // 1. Infos textuelles
        titleLabel.setText(serie.getTitre());
        String annee = (serie.getDateSortie() != null) ? String.valueOf(serie.getDateSortie().getYear()) : "N/A";
        yearLabel.setText(annee + " • Série");

        // 2. État visuel (Terminée ou En cours)
        if (serie.isTerminee()) {
            seasonsLabel.setText("Terminée");
            seasonsLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            seasonsLabel.setText("En cours");
            seasonsLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
        }

        // 3. Rating & Genre
        viewsLabel.setText(serie.getRatingMoyen() != null ? String.format("%.1f/5 ⭐", serie.getRatingMoyen()) : "N/A ⭐");
        commentsLabel.setText(serie.getGenre() != null ? serie.getGenre() : "Action");

        // 4. CHARGEMENT DE L'IMAGE (PHOTO)
        // On utilise l'URL venant de la colonne url_image_cover de la BDD
        loadImage(serie.getUrlImageCover()); 

        // 5. GÉNÉRATION DU MENU DYNAMIQUE (Popups)
        buildDynamicEpisodesMenu();
    }

    private void loadImage(String url) {
        if (url != null && !url.trim().isEmpty()) {
            try {
                // Chargement asynchrone (true) pour ne pas bloquer l'interface
                Image img = new Image(url, true);
                ImageView imgView = new ImageView(img);
                
                // Ajustement aux dimensions du container Netflix Admin
                imgView.setFitWidth(110);
                imgView.setFitHeight(65);
                imgView.setPreserveRatio(false);
                
                imageContainer.getChildren().clear();
                imageContainer.getChildren().add(imgView);
            } catch (Exception e) {
                System.out.println("Erreur chargement image [" + url + "] : " + e.getMessage());
            }
        }
    }

    private void buildDynamicEpisodesMenu() {
        if (episodesMenuBtn == null) return;
        episodesMenuBtn.getItems().clear();
        
        // Récupération des saisons via SaisonDAO
        List<Saison> saisons = saisonDAO.findBySerieId(currentSerie.getId());
        episodesMenuBtn.setText(saisons.size() + " SAISONS");

        for (Saison s : saisons) {
            // Création d'un 'Menu' pour permettre le sous-menu des épisodes (cascade)
            Menu saisonSubMenu = new Menu("Saison " + s.getNumeroSaison());
            
            // Correction : On utilise la méthode de ton SerieDAO
            List<Episode> episodes = serieDAO.findEpisodeBySaison(s.getId());
            
            for (Episode e : episodes) {
                MenuItem epItem = new MenuItem("Épisode " + e.getNumeroEpisode() + " : " + e.getTitre());
                epItem.setOnAction(event -> handleEditEpisode(e));
                saisonSubMenu.getItems().add(epItem);
            }

            // Séparateur et bouton "+ Ajouter épisode"
            saisonSubMenu.getItems().add(new SeparatorMenuItem());
            MenuItem addEpItem = new MenuItem("+ Ajouter épisode");
            addEpItem.getStyleClass().add("add-action-item"); // Pour le style rouge
            addEpItem.setOnAction(event -> handleAddEpisodeToSaison(s));
            saisonSubMenu.getItems().add(addEpItem);

            episodesMenuBtn.getItems().add(saisonSubMenu);
        }

        // Séparateur et bouton "+ Ajouter saison" en bas du menu principal
        episodesMenuBtn.getItems().add(new SeparatorMenuItem());
        MenuItem addSaisonItem = new MenuItem("+ Ajouter saison");
        addSaisonItem.getStyleClass().add("add-action-item"); // Pour le style rouge
        addSaisonItem.setOnAction(event -> handleAddSaisonToSerie());
        episodesMenuBtn.getItems().add(addSaisonItem);
    }

    private void handleEditEpisode(Episode e) { System.out.println("Editer Ep ID: " + e.getId()); }
    private void handleAddEpisodeToSaison(Saison s) { System.out.println("Ajout Ep dans Saison: " + s.getNumeroSaison()); }
    private void handleAddSaisonToSerie() { System.out.println("Ajout Saison dans: " + currentSerie.getTitre()); }
    
    @FXML private void handleDelete() { System.out.println("Supprimer : " + currentSerie.getTitre()); }
}