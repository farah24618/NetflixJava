package tn.farah.NetflixJava.Controller;

import java.awt.Button;
import java.awt.Image;
import java.awt.Label;

import javax.swing.text.html.ImageView;
import javax.swing.text.html.ListView;

import javafx.fxml.FXML;
<<<<<<< HEAD

import javafx.scene.control.Button;
=======
>>>>>>> branch 'master' of https://github.com/farah24618/NetflixJava.git
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Serie;

public class DetailMediaController {

    @FXML
    private ImageView imgLogo;

    @FXML
    private ImageView imgBackground;

    @FXML
    private ImageView imgAffiche;

    @FXML
    private Label lblTitre;

    @FXML
    private Label lblType;

    @FXML
    private Label lblNote;

    @FXML
    private Label lblAnnee;

    @FXML
    private Label lblCategorie;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblCasting;

    @FXML
    private Button btnWatch;

    @FXML
    private Button btnTrailer;

    @FXML
    private Button btnFavori;

    @FXML
    private VBox serieBox;

    @FXML
    private ComboBox<String> cbSaisons;

    @FXML
    private ListView<String> listEpisodes;

    @FXML
    public void initialize() {
        // logo au cas où le FXML ne le charge pas
        try {
            imgLogo.setImage(
                    new Image(getClass().getResource("/tn/farah/NetflixJava/ImagesNet/logo.png").toExternalForm())
            );
        } catch (Exception e) {
            System.out.println("Logo non chargé");
        }
    }

    public void afficherFilm(Film film) {
        lblTitre.setText(film.getTitre());
        lblType.setText("Film");
        lblNote.setText(String.valueOf(film.getNote()));
        lblAnnee.setText(String.valueOf(film.getAnnee()));
        lblCategorie.setText(film.getCategorie());
        lblDescription.setText(film.getDescription());
        lblCasting.setText(film.getCasting());

        try {
            if (film.getImageUrl() != null && !film.getImageUrl().isEmpty()) {
                Image image = new Image(film.getImageUrl());
                imgAffiche.setImage(image);
                imgBackground.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Image film non chargée");
        }

        serieBox.setVisible(false);
        serieBox.setManaged(false);
    }

    public void afficherSerie(Serie serie) {
        lblTitre.setText(serie.getTitre());
        lblType.setText("Série");
        lblNote.setText(String.valueOf(serie.getNote()));
        lblAnnee.setText(String.valueOf(serie.getAnnee()));
        lblCategorie.setText(serie.getCategorie());
        lblDescription.setText(serie.getDescription());
        lblCasting.setText(serie.getCasting());

        try {
            if (serie.getImageUrl() != null && !serie.getImageUrl().isEmpty()) {
                Image image = new Image(serie.getImageUrl());
                imgAffiche.setImage(image);
                imgBackground.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Image série non chargée");
        }

        serieBox.setVisible(true);
        serieBox.setManaged(true);

        cbSaisons.getItems().clear();
        listEpisodes.getItems().clear();

        if (serie.getSaisons() != null) {
            for (Saison s : serie.getSaisons()) {
                cbSaisons.getItems().add("Saison " + s.getNumeroSaison());
            }
        }
    }
}