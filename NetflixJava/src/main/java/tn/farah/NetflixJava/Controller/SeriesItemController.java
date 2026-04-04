package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.farah.NetflixJava.Entities.Serie;

public class SeriesItemController {

    @FXML private Label titleLabel;
    @FXML private Label yearLabel;
    @FXML private Label seasonsLabel; // Utilisé ici pour afficher l'état (Terminée/En cours)
    @FXML private Label viewsLabel;
    @FXML private Label commentsLabel;

    private Serie currentSerie;

    public void setSeriesData(Serie serie) {
        this.currentSerie = serie;
        
        // Titre de la série
        titleLabel.setText(serie.getTitre());
        
        // Année (depuis Media)
        if (serie.getDateSortie() != null) {
            yearLabel.setText(serie.getDateSortie().getYear() + " • Série");
        } else {
            yearLabel.setText("N/A • Série");
        }

        // État de la série (utilisant ton champ 'terminee')
        seasonsLabel.setText(serie.isTerminee() ? "Terminée" : "En cours"); 

        // Note moyenne (depuis Media)
        if (serie.getRatingMoyen() != null) {
            viewsLabel.setText(String.format("%.1f/5 ⭐", serie.getRatingMoyen()));
        } else {
            viewsLabel.setText("No Rating");
        }
        
        // Genre textuel (utilisant ton champ 'genre')
        commentsLabel.setText(serie.getGenre() != null ? serie.getGenre() : "Action");
    }

    @FXML
    private void handleManageEpisodes() {
        System.out.println("📂 Gestion des épisodes pour : " + currentSerie.getTitre());
    }

    @FXML
    private void handleDelete() {
        System.out.println("🗑️ Suppression demandée pour l'ID : " + currentSerie.getId());
    }
}