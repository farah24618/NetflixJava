package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EpisodeViewController implements Initializable {

    // =============================================
    // INJECTION FXML — NAVBAR / PROFIL
    // =============================================
    @FXML private HBox      profilBox;
    @FXML private Label     labelUserName;
    @FXML private StackPane avatarContainer;
    @FXML private ImageView avatarImage;
    @FXML private StackPane avatarFallback;
    @FXML private Label     avatarInitiale;
    @FXML private TextField searchField;

    // =============================================
    // INJECTION FXML — PLAYER VIDÉO
    // =============================================
    @FXML private StackPane videoPane;
    @FXML private Label     episodeTitleVideo;
    @FXML private Slider    progressSlider;
    @FXML private Slider    volumeSlider;
    @FXML private Label     currentTime;
    @FXML private Label     totalTime;
    @FXML private Label     currentTimeSmall;
    @FXML private Button    btnPlayPause;
    @FXML private Button    btnRewind;
    @FXML private Button    btnForward;
    @FXML private Button    btnSubtitles;
    @FXML private Button    btnVolume;
    @FXML private Button    btnFullscreen;
    @FXML private Button    btnSkipIntro;

    // =============================================
    // INJECTION FXML — INFOS ÉPISODE
    // =============================================
    @FXML private Label labelNumeroSaison;   // fx:id nouveau
    @FXML private Label labelNumeroEpisode;  // fx:id nouveau
    @FXML private Label labelSaisonEpisode;  // fx:id nouveau
    @FXML private Label labelDuree;          // fx:id nouveau
    @FXML private Label labelDateSortie;     // fx:id nouveau
    @FXML private Label episodeTitle;
    @FXML private Label episodeDesc;
    @FXML private VBox  castList;

    // =============================================
    // INJECTION FXML — BOUTONS ACTIONS
    // =============================================
    @FXML private Button btnLike;
    @FXML private Button btnDislike;
    @FXML private Button btnShare;
    @FXML private Button btnDownload;

    // =============================================
    // INJECTION FXML — ONGLETS
    // =============================================
    @FXML private VBox tabApropos;
    @FXML private VBox tabEpisodes;
    @FXML private VBox tabBandes;
    @FXML private VBox tabCommentaires;
    @FXML private VBox tabSimilaires;

    // =============================================
    // INJECTION FXML — PANNEAUX CONTENU
    // =============================================
    @FXML private VBox panelEpisodes;
    @FXML private VBox panelApropos;
    @FXML private VBox panelBandes;
    @FXML private VBox panelSimilaires;
    @FXML private VBox listeEpisodes;
    @FXML private HBox listeSimilaires;

    // =============================================
    // ÉTAT INTERNE
    // =============================================
    private boolean isPlaying   = true;
    private boolean isMuted     = false;
    private boolean subtitlesOn = false;
    private int     likeCount   = 0;

    // Données des épisodes : {numéro, titre, durée, description, dateSortie, saison}
    private final List<String[]> EPISODES = List.of(
        new String[]{"1", "Le Commencement",   "52 min", "Damien intègre une nouvelle organisation secrète...",           "15 Jan 2024", "1"},
        new String[]{"2", "Les Ombres",         "49 min", "Une rencontre inattendue remet tout en question...",            "22 Jan 2024", "1"},
        new String[]{"3", "Le Pacte",           "55 min", "Un accord dangereux est signé dans le plus grand secret...",   "29 Jan 2024", "1"},
        new String[]{"4", "L'Échange",          "56 min", "Damien découvre que son associé cache un secret compromettant. Tandis que la tension monte au sein du groupe, une rencontre inattendue va tout changer...", "05 Fév 2024", "1"},
        new String[]{"5", "Trahison",           "54 min", "Les alliés de Damien révèlent leur vrai visage...",            "12 Fév 2024", "1"},
        new String[]{"6", "La Chute",           "51 min", "Tout s'effondre autour de Damien en une seule nuit...",        "19 Fév 2024", "1"},
        new String[]{"7", "Résurrection",       "58 min", "Contre toute attente, Damien trouve un nouvel allié...",       "26 Fév 2024", "1"},
        new String[]{"8", "Le Dernier Recours", "53 min", "Une ultime confrontation se prépare dans l'ombre...",         "04 Mar 2024", "1"},
        new String[]{"9", "Vérité",             "50 min", "Les masques tombent et les vérités éclatent enfin...",         "11 Mar 2024", "1"},
        new String[]{"10","Fin de Partie",      "62 min", "Le dénouement final d'une saison explosive...",               "18 Mar 2024", "1"}
    );

    // =============================================
    // INITIALIZE
    // =============================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerProfil("Enfants", "/images/profil.png");
        chargerCast();
        chargerListeEpisodes();
        chargerSimilaires();
        configurerSliders();
        // Charger l'épisode 4 par défaut (index 3)
        mettreAJourInfosEpisode(EPISODES.get(3));
        // Onglet Épisodes actif par défaut
        activerOnglet(tabEpisodes);
    }

    // =============================================
    // MISE À JOUR INFOS ÉPISODE
    // Met à jour TOUS les labels quand on clique sur un épisode
    // =============================================

    /**
     * @param ep tableau : {numéro, titre, durée, description, dateSortie, saison}
     */
    private void mettreAJourInfosEpisode(String[] ep) {
        String numero      = ep[0];
        String titre       = ep[1];
        String duree       = ep[2];
        String description = ep[3];
        String date        = ep[4];
        String saison      = ep[5];

        // Fil d'ariane
        labelNumeroSaison.setText(saison);
        labelNumeroEpisode.setText(numero);

        // Titre principal
        episodeTitle.setText(titre);

        // Métadonnées
        labelSaisonEpisode.setText("Saison " + saison + " - Episode " + numero);
        labelDuree.setText(duree);
        labelDateSortie.setText(date);

        // Description
        episodeDesc.setText(description);

        // Player vidéo
        episodeTitleVideo.setText("S0" + saison + " E0" + numero + " — " + titre);
        progressSlider.setValue(0);
        currentTime.setText("00:00");
        totalTime.setText(duree);
        currentTimeSmall.setText("00:00 / " + duree);
        isPlaying = true;
        btnPlayPause.setText("⏸");

        System.out.println("Épisode chargé : " + numero + " — " + titre);
    }

    // =============================================
    // PROFIL
    // =============================================
    private void chargerProfil(String nom, String cheminImage) {
        labelUserName.setText(nom);
        avatarInitiale.setText(String.valueOf(nom.charAt(0)).toUpperCase());
        try {
            InputStream stream = getClass().getResourceAsStream(cheminImage);
            if (stream != null) {
                avatarImage.setImage(new Image(stream));
                avatarImage.setVisible(true);
                avatarFallback.setVisible(false);
            } else {
                avatarImage.setVisible(false);
                avatarFallback.setVisible(true);
            }
        } catch (Exception e) {
            avatarImage.setVisible(false);
            avatarFallback.setVisible(true);
        }
    }

    @FXML
    private void onProfilClicked() {
        System.out.println("Profil : " + labelUserName.getText());
    }

    // =============================================
    // CAST
    // =============================================
    private void chargerCast() {
        List<String[]> acteurs = List.of(
            new String[]{"Jean Dujardin",    "Damien Moreau"},
            new String[]{"Marion Cotillard", "Sophie Laurent"},
            new String[]{"Omar Sy",          "Marcus Diallo"},
            new String[]{"Isabelle Adjani",  "La Directrice"}
        );
        for (String[] acteur : acteurs) {
            VBox entree = new VBox(2);
            Label nom = new Label(acteur[0]);
            nom.setTextFill(Color.WHITE);
            nom.setFont(Font.font("System", FontWeight.BOLD, 13));
            Label role = new Label(acteur[1]);
            role.setTextFill(Color.web("#aaaaaa"));
            role.setFont(Font.font(12));
            entree.getChildren().addAll(nom, role);
            castList.getChildren().add(entree);
        }
    }

    // =============================================
    // LISTE DES ÉPISODES
    // =============================================
    private void chargerListeEpisodes() {
        listeEpisodes.getChildren().clear();
        for (String[] ep : EPISODES) {
            boolean estActuel = ep[0].equals("4");
            HBox carte = creerCarteEpisode(ep, estActuel);
            listeEpisodes.getChildren().add(carte);
        }
    }

    private HBox creerCarteEpisode(String[] ep, boolean estActuel) {
        String numero = ep[0];
        String titre  = ep[1];
        String duree  = ep[2];
        String desc   = ep[3];

        HBox carte = new HBox(16);
        carte.setStyle(
            "-fx-background-color: " + (estActuel ? "#2a2a2a" : "#1a1a1a") + ";" +
            "-fx-background-radius: 6; -fx-padding: 14; -fx-cursor: hand;" +
            (estActuel ? "-fx-border-color: #E50914; -fx-border-radius: 6; -fx-border-width: 1;" : "")
        );

        // Hover
        carte.setOnMouseEntered(e -> {
            if (!estActuel)
                carte.setStyle(
                    "-fx-background-color: #252525; -fx-background-radius: 6;" +
                    "-fx-padding: 14; -fx-cursor: hand;"
                );
        });
        carte.setOnMouseExited(e -> {
            if (!estActuel)
                carte.setStyle(
                    "-fx-background-color: #1a1a1a; -fx-background-radius: 6;" +
                    "-fx-padding: 14; -fx-cursor: hand;"
                );
        });

        // Miniature
        StackPane miniature = new StackPane();
        miniature.setPrefSize(180, 100);
        miniature.setMinSize(180, 100);
        miniature.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 4;");

        Label numLabel = new Label(numero);
        numLabel.setTextFill(Color.web(estActuel ? "#E50914" : "#555555"));
        numLabel.setFont(Font.font("System", FontWeight.BOLD, 26));

        Label playIcon = new Label("▶");
        playIcon.setTextFill(Color.WHITE);
        playIcon.setFont(Font.font(28));
        playIcon.setOpacity(0);

        miniature.getChildren().addAll(numLabel, playIcon);

        // Hover sur miniature
        carte.setOnMouseEntered(e -> {
            playIcon.setOpacity(0.8);
            numLabel.setOpacity(0);
            if (!estActuel)
                carte.setStyle(
                    "-fx-background-color: #252525; -fx-background-radius: 6;" +
                    "-fx-padding: 14; -fx-cursor: hand;"
                );
        });
        carte.setOnMouseExited(e -> {
            playIcon.setOpacity(0);
            numLabel.setOpacity(1);
            if (!estActuel)
                carte.setStyle(
                    "-fx-background-color: #1a1a1a; -fx-background-radius: 6;" +
                    "-fx-padding: 14; -fx-cursor: hand;"
                );
        });

        // Infos
        VBox infos = new VBox(6);
        HBox.setHgrow(infos, Priority.ALWAYS);

        HBox ligneTitre = new HBox(12);
        ligneTitre.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titreLabel = new Label("Épisode " + numero + " — " + titre);
        titreLabel.setTextFill(estActuel ? Color.web("#E50914") : Color.WHITE);
        titreLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        HBox.setHgrow(titreLabel, Priority.ALWAYS);

        Label dureeLabel = new Label(duree);
        dureeLabel.setTextFill(Color.web("#aaaaaa"));
        dureeLabel.setFont(Font.font(12));

        ligneTitre.getChildren().addAll(titreLabel, dureeLabel);

        Label descLabel = new Label(desc);
        descLabel.setTextFill(Color.web("#aaaaaa"));
        descLabel.setFont(Font.font(13));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(600);

        if (estActuel) {
            Label badge = new Label("● En cours");
            badge.setTextFill(Color.web("#E50914"));
            badge.setFont(Font.font("System", FontWeight.BOLD, 11));
            infos.getChildren().addAll(ligneTitre, badge, descLabel);
        } else {
            infos.getChildren().addAll(ligneTitre, descLabel);
        }

        // Bouton Lire
        Button btnLire = new Button("▶  Lire");
        btnLire.setStyle(
            "-fx-background-color: " + (estActuel ? "#E50914" : "#333333") + ";" +
            "-fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16; -fx-background-radius: 4; -fx-cursor: hand;"
        );

        // ✅ CLIC : met à jour TOUS les labels de la page
        btnLire.setOnAction(e -> mettreAJourInfosEpisode(ep));
        carte.setOnMouseClicked(e -> mettreAJourInfosEpisode(ep));

        carte.getChildren().addAll(miniature, infos, btnLire);
        return carte;
    }

    // =============================================
    // SIMILAIRES
    // =============================================
    private void chargerSimilaires() {
        List<String[]> series = List.of(
            new String[]{"Infiltré",  "#1a3a5c"},
            new String[]{"Rupture",   "#3a1a1a"},
            new String[]{"Vertige",   "#1a3a1a"},
            new String[]{"Le Réseau", "#2a1a3a"}
        );
        for (String[] serie : series) {
            VBox carte = new VBox(8);
            carte.setStyle("-fx-cursor: hand;");
            StackPane img = new StackPane();
            img.setPrefSize(160, 90);
            img.setStyle("-fx-background-color: " + serie[1] + "; -fx-background-radius: 6;");
            Label titre = new Label(serie[0]);
            titre.setTextFill(Color.WHITE);
            titre.setFont(Font.font("System", FontWeight.BOLD, 13));
            img.getChildren().add(titre);
            Label lbl = new Label(serie[0]);
            lbl.setTextFill(Color.web("#aaaaaa"));
            lbl.setFont(Font.font(12));
            carte.getChildren().addAll(img, lbl);
            listeSimilaires.getChildren().add(carte);
        }
    }

    // =============================================
    // SLIDERS
    // =============================================
    private void configurerSliders() {
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double totalSeconds  = 56 * 60;
            double currentSeconds = (newVal.doubleValue() / 100.0) * totalSeconds;
            String formatted = formatTime((int) currentSeconds);
            currentTime.setText(formatted);
            currentTimeSmall.setText(formatted + " / 56:00");
        });
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            btnVolume.setText(newVal.doubleValue() == 0 ? "🔇" : "Vol")
        );
    }

    // =============================================
    // HANDLERS PLAYER
    // =============================================
    @FXML private void onPlayPause() {
        isPlaying = !isPlaying;
        btnPlayPause.setText(isPlaying ? "⏸" : "▶");
    }

    @FXML private void onRewind() {
        progressSlider.setValue(Math.max(0, progressSlider.getValue() - 3));
    }

    @FXML private void onForward() {
        progressSlider.setValue(Math.min(100, progressSlider.getValue() + 3));
    }

    @FXML private void onSubtitles() {
        subtitlesOn = !subtitlesOn;
        btnSubtitles.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;" +
            "-fx-cursor: hand; -fx-border-color: " + (subtitlesOn ? "#E50914" : "white") + ";" +
            "-fx-border-radius: 3; -fx-padding: 2 6 2 6;"
        );
    }

    @FXML private void onVolume() {
        isMuted = !isMuted;
        volumeSlider.setValue(isMuted ? 0 : 80);
        btnVolume.setText(isMuted ? "🔇" : "Vol");
    }

    @FXML private void onFullscreen() {
        javafx.stage.Stage stage =
            (javafx.stage.Stage) videoPane.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML private void onSkipIntro() {
        progressSlider.setValue(10);
    }

    // =============================================
    // HANDLERS BOUTONS ACTIONS
    // =============================================
    @FXML private void onLike() {
        likeCount++;
        btnLike.setText("J'aime (" + likeCount + ")");
        btnLike.setStyle(
            "-fx-background-color: #E50914; -fx-text-fill: white;" +
            "-fx-padding: 8 18 8 18; -fx-background-radius: 4;" +
            "-fx-cursor: hand; -fx-font-size: 13px;"
        );
    }

    @FXML private void onDislike() {
        btnDislike.setStyle(
            "-fx-background-color: #555555; -fx-text-fill: white;" +
            "-fx-padding: 8 18 8 18; -fx-background-radius: 4;" +
            "-fx-cursor: hand; -fx-font-size: 13px;"
        );
    }

    @FXML private void onShare() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Partager");
        alert.setHeaderText(null);
        alert.setContentText("Lien copié dans le presse-papiers !");
        alert.showAndWait();
    }

    @FXML private void onDownload() {
        btnDownload.setText("Téléchargement...");
        btnDownload.setDisable(true);
    }

    // =============================================
    // HANDLERS ONGLETS
    // =============================================
    @FXML private void onTabApropos()      { activerOnglet(tabApropos); }
    @FXML private void onTabEpisodes()     { activerOnglet(tabEpisodes); }
    @FXML private void onTabBandes()       { activerOnglet(tabBandes); }
    @FXML private void onTabCommentaires() { activerOnglet(tabCommentaires); }
    @FXML private void onTabSimilaires()   { activerOnglet(tabSimilaires); }

    private void activerOnglet(VBox ongletActif) {
        VBox[] onglets = {tabApropos, tabEpisodes, tabBandes, tabCommentaires, tabSimilaires};
        for (VBox onglet : onglets) {
            Label label      = (Label) onglet.getChildren().get(0);
            HBox  indicateur = (HBox)  onglet.getChildren().get(1);
            if (onglet == ongletActif) {
                label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                indicateur.setStyle(
                    "-fx-background-color: #E50914; -fx-background-radius: 2 2 0 0; -fx-padding: 10 0 0 0;"
                );
                indicateur.setPrefWidth(Region.USE_COMPUTED_SIZE);
            } else {
                label.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");
                indicateur.setStyle("-fx-background-color: transparent; -fx-padding: 10 0 0 0;");
                indicateur.setPrefWidth(0);
            }
        }

        panelApropos.setVisible(false);    panelApropos.setManaged(false);
        panelEpisodes.setVisible(false);   panelEpisodes.setManaged(false);
        panelBandes.setVisible(false);     panelBandes.setManaged(false);
        panelSimilaires.setVisible(false); panelSimilaires.setManaged(false);

        if      (ongletActif == tabEpisodes)   { panelEpisodes.setVisible(true);   panelEpisodes.setManaged(true); }
        else if (ongletActif == tabApropos)    { panelApropos.setVisible(true);    panelApropos.setManaged(true); }
        else if (ongletActif == tabBandes)     { panelBandes.setVisible(true);     panelBandes.setManaged(true); }
        else if (ongletActif == tabSimilaires) { panelSimilaires.setVisible(true); panelSimilaires.setManaged(true); }
    }

    // =============================================
    // UTILITAIRES
    // =============================================
    private String formatTime(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        return String.format("%02d:%02d", min, sec);
    }
}