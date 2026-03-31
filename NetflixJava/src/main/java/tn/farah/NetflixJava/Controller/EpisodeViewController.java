package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import tn.farah.NetflixJava.DAO.EpisodeDAO;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

public class EpisodeViewController implements Initializable {

    private SerieService serieService;

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
    @FXML private MediaView mediaView;
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
    @FXML private Label labelNumeroSaison;
    @FXML private Label labelNumeroEpisode;
    @FXML private Label labelSaisonEpisode;
    @FXML private Label labelDuree;
    @FXML private Label labelDateSortie;
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
    // LABELS DYNAMIQUES
    // =============================================
    @FXML private Label labelTitreSerie;
    @FXML private Label labelNbEpisodes;
    @FXML private Label labelSynopsis;
    @FXML private Label labelGenre;
    @FXML private Label labelAnnee;
    @FXML private Label labelProducteur;

    // =============================================
    // ÉTAT INTERNE
    // =============================================
    private boolean     isPlaying   = true;
    private boolean     isMuted     = false;
    private boolean     subtitlesOn = false;
    private int         likeCount   = 0;
    private int         serieId     = -1;

    private List<Episode> episodesDB;
    private int           currentSaisonId = 1;
    private Episode       episodeActuel   = null;

    // =============================================
    // MEDIAPLAYER
    // =============================================
    private MediaPlayer mediaPlayer;

    // =============================================
    // INITIALIZE
    // =============================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serieService = new SerieService(ConxDB.getInstance());
        episodesDB   = EpisodeDAO.findBySaison(currentSaisonId);

        System.out.println("🔍 currentSaisonId = " + currentSaisonId);
        System.out.println("🔍 serieId         = " + serieId);
        System.out.println("🔍 nb épisodes     = " + (episodesDB != null ? episodesDB.size() : "null"));

        chargerListeEpisodes();
        chargerSimilaires();
        configurerSliders();
        chargerProfil("Enfants", "/images/profil.png");

        if (episodesDB != null && !episodesDB.isEmpty()) {
            episodeActuel = episodesDB.get(0);
            mettreAJourInfosEpisode(episodeActuel);
        }
        activerOnglet(tabEpisodes);
    }

    // =============================================
    // SETTERS — appelés depuis le controller parent
    // =============================================
    public void setSaisonId(int saisonId) {
        this.currentSaisonId = saisonId;
    }

    public void setSerie(Serie serie) {
        this.serieId = serie.getId();
        chargerInfosSerie(this.serieId);
        if (labelNbEpisodes != null && episodesDB != null) {
            int nb = episodesDB.size();
            labelNbEpisodes.setText(nb + " épisode" + (nb > 1 ? "s" : ""));
        }
    }

    // =============================================
    // CHARGEMENT INFOS SÉRIE VIA SERVICE
    // =============================================
    private void chargerInfosSerie(int serieId) {
        if (serieId == -1) return;

        Serie serieAct = serieService.findById(serieId);
        if (serieAct == null) {
            System.err.println("❌ Série introuvable pour id=" + serieId);
            return;
        }

        if (labelTitreSerie != null) labelTitreSerie.setText(serieAct.getTitre());

        int nbEpisodes = episodesDB != null ? episodesDB.size() : 0;
        if (labelNbEpisodes != null)
            labelNbEpisodes.setText(nbEpisodes + " épisode" + (nbEpisodes > 1 ? "s" : ""));

        if (labelSynopsis != null)
            labelSynopsis.setText(serieAct.getSynopsis() != null ? serieAct.getSynopsis() : "Résumé non disponible");
        if (labelGenre != null)
            labelGenre.setText(serieAct.getGenre() != null ? serieAct.getGenre() : "Inconnu");
        if (labelAnnee != null) {
            int annee = serieAct.getDateSortie().getYear();
            labelAnnee.setText(annee != 0 ? String.valueOf(annee) : "0");
        }
        if (labelProducteur != null)
            labelProducteur.setText(serieAct.getProducteur() != null ? serieAct.getProducteur() : "N/A");
    }

    // =============================================
    // CHARGEMENT VIDÉO
    // =============================================
    private void loadVideo(String videoUrl) {
        if (mediaView == null) {
            System.err.println("❌ ERREUR CRITIQUE : mediaView est null !");
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        if (videoUrl == null || videoUrl.isBlank()) {
            System.out.println("⚠️ Aucune URL vidéo pour cet épisode.");
            return;
        }

        String url = videoUrl.trim().replace("\\", "/");
        if (url.matches("^[A-Za-z]:.*")) {
            url = "file:///" + url;
        }

        System.out.println("🎬 URL finale chargée : " + url);

        if (url.startsWith("file:")) {
            try {
                java.io.File fichier = new java.io.File(new java.net.URI(url));
                System.out.println("📁 Fichier existe ? " + fichier.exists());
                System.out.println("📁 Peut lire ?     " + fichier.canRead());
                if (!fichier.exists()) {
                    System.err.println("❌ Fichier introuvable : " + fichier.getAbsolutePath());
                    return;
                }
            } catch (Exception ex) {
                System.err.println("❌ URI invalide : " + url + " → " + ex.getMessage());
                return;
            }
        }

        try {
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.setPreserveRatio(true);

            mediaPlayer.setOnReady(() -> {
                Duration total = mediaPlayer.getTotalDuration();
                String totalFormatted = formatTime((int) total.toSeconds());
                totalTime.setText(totalFormatted);
                currentTimeSmall.setText("00:00 / " + totalFormatted);
                progressSlider.setValue(0);
                mediaPlayer.play();
                isPlaying = true;
                btnPlayPause.setText("⏸");
                System.out.println("✅ Vidéo prête et lancée !");
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldT, newT) -> {
                if (!progressSlider.isValueChanging()) {
                    double totalSec   = mediaPlayer.getTotalDuration().toSeconds();
                    double currentSec = newT.toSeconds();
                    if (totalSec > 0) progressSlider.setValue((currentSec / totalSec) * 100.0);
                    String cur = formatTime((int) currentSec);
                    String tot = formatTime((int) totalSec);
                    currentTime.setText(cur);
                    currentTimeSmall.setText(cur + " / " + tot);
                }
            });

            progressSlider.valueChangingProperty().addListener((obs, wasChanging, changing) -> {
                if (!changing && mediaPlayer != null) {
                    double totalSec = mediaPlayer.getTotalDuration().toSeconds();
                    mediaPlayer.seek(Duration.seconds((progressSlider.getValue() / 100.0) * totalSec));
                }
            });

            progressSlider.setOnMouseClicked(e -> {
                if (mediaPlayer != null) {
                    double totalSec = mediaPlayer.getTotalDuration().toSeconds();
                    mediaPlayer.seek(Duration.seconds((progressSlider.getValue() / 100.0) * totalSec));
                }
            });

            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            volumeSlider.valueProperty().addListener((obs, o, n) -> {
                if (mediaPlayer != null) mediaPlayer.setVolume(n.doubleValue() / 100.0);
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.pause();
                isPlaying = false;
                btnPlayPause.setText("▶");
                progressSlider.setValue(0);
            });

            mediaPlayer.setOnError(() -> {
                javafx.scene.media.MediaException err = mediaPlayer.getError();
                System.err.println("❌ Erreur MediaPlayer : " + err.getType() + " → " + err.getMessage());
            });

        } catch (Exception e) {
            System.err.println("❌ Impossible de charger la vidéo : " + url);
            System.err.println("❌ Cause : " + e.getClass().getName() + " → " + e.getMessage());
        }
    }

    // =============================================
    // MISE À JOUR INFOS ÉPISODE
    // =============================================
    private void mettreAJourInfosEpisode(Episode ep) {
        episodeActuel = ep;

        String numero = String.valueOf(ep.getNumeroEpisode());
        String saison = String.valueOf(ep.getSaisonId());
        String titre  = ep.getTitre();
        String duree  = ep.getDuree() + " min";
        String resume = ep.getResume() != null ? ep.getResume() : "";

        labelNumeroSaison.setText(saison);
        labelNumeroEpisode.setText(numero);
        episodeTitle.setText(titre);
        labelSaisonEpisode.setText("Saison " + saison + " - Épisode " + numero);
        labelDuree.setText(duree);
        labelDateSortie.setText("");
        episodeDesc.setText(resume);
        episodeTitleVideo.setText("S0" + saison + " E" + String.format("%02d", ep.getNumeroEpisode()) + " — " + titre);
        currentTime.setText("00:00");
        totalTime.setText(duree);
        currentTimeSmall.setText("00:00 / " + duree);
        progressSlider.setValue(0);
        isPlaying = true;
        btnPlayPause.setText("⏸");

        System.out.println("✅ Épisode chargé : " + numero + " — " + titre);

        if (ep.getVideoUrl() != null && !ep.getVideoUrl().isBlank()) {
            String url = ep.getVideoUrl().trim().replace("\\", "/");
            if (url.matches("^[A-Za-z]:.*")) url = "file:///" + url;
            System.out.println("🎬 Chargement vidéo : " + url);
            loadVideo(url);
        } else {
            String resourcePath = "/videos/episode_" + ep.getId() + ".mp4";
            URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl != null) {
                loadVideo(resourceUrl.toExternalForm());
            } else {
                System.out.println("⚠️ Pas de vidéo trouvée pour l'épisode " + ep.getId());
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                    mediaPlayer = null;
                }
            }
        }
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

    @FXML private void onProfilClicked() {
        System.out.println("Profil : " + labelUserName.getText());
    }

    // =============================================
    // UTILITAIRE CAST
    // =============================================
    private void ajouterLabelMessage(String message) {
        Label label = new Label(message);
        label.setTextFill(Color.web("#aaaaaa"));
        label.setFont(Font.font("System", 13));
        castList.getChildren().add(label);
    }

    // =============================================
    // LISTE DES ÉPISODES
    // =============================================
    private void chargerListeEpisodes() {
        listeEpisodes.getChildren().clear();
        if (episodesDB == null || episodesDB.isEmpty()) {
            Label vide = new Label("Aucun épisode disponible.");
            vide.setTextFill(Color.web("#aaaaaa"));
            vide.setFont(Font.font(14));
            listeEpisodes.getChildren().add(vide);
            return;
        }
        for (Episode ep : episodesDB) {
            boolean estActuel = episodeActuel != null
                ? ep.getId() == episodeActuel.getId()
                : ep.getId() == episodesDB.get(0).getId();
            listeEpisodes.getChildren().add(creerCarteEpisode(ep, estActuel));
        }
    }

    // =============================================
    // CARTE ÉPISODE
    // =============================================
    private HBox creerCarteEpisode(Episode ep, boolean estActuel) {
        String numero = String.valueOf(ep.getNumeroEpisode());
        String titre  = ep.getTitre();
        String duree  = ep.getDuree() + " min";
        String desc   = ep.getResume() != null ? ep.getResume() : "";

        HBox carte = new HBox(16);
        carte.setStyle(
            "-fx-background-color: " + (estActuel ? "#2a2a2a" : "#1a1a1a") + ";" +
            "-fx-background-radius: 6; -fx-padding: 14; -fx-cursor: hand;" +
            (estActuel ? "-fx-border-color: #E50914; -fx-border-radius: 6; -fx-border-width: 1;" : "")
        );

        StackPane miniature = new StackPane();
        miniature.setPrefSize(180, 100);
        miniature.setMinSize(180, 100);

        if (ep.getMiniatureUrl() != null && !ep.getMiniatureUrl().isEmpty()) {
            try {
                InputStream imgStream = getClass().getResourceAsStream(ep.getMiniatureUrl());
                if (imgStream != null) {
                    ImageView miniImg = new ImageView(new Image(imgStream));
                    miniImg.setFitWidth(180);
                    miniImg.setFitHeight(100);
                    miniImg.setPreserveRatio(false);
                    miniature.getChildren().add(miniImg);
                } else {
                    miniature.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 4;");
                }
            } catch (Exception e) {
                miniature.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 4;");
            }
        } else {
            miniature.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 4;");
        }

        Label numLabel = new Label(numero);
        numLabel.setTextFill(Color.web(estActuel ? "#E50914" : "#555555"));
        numLabel.setFont(Font.font("System", FontWeight.BOLD, 26));

        Label playIcon = new Label("▶");
        playIcon.setTextFill(Color.WHITE);
        playIcon.setFont(Font.font(28));
        playIcon.setOpacity(0);

        miniature.getChildren().addAll(numLabel, playIcon);

        carte.setOnMouseEntered(e -> {
            playIcon.setOpacity(0.8);
            numLabel.setOpacity(0);
            if (!estActuel) carte.setStyle(
                "-fx-background-color: #252525; -fx-background-radius: 6;" +
                "-fx-padding: 14; -fx-cursor: hand;");
        });
        carte.setOnMouseExited(e -> {
            playIcon.setOpacity(0);
            numLabel.setOpacity(1);
            if (!estActuel) carte.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 6;" +
                "-fx-padding: 14; -fx-cursor: hand;");
        });

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

        Button btnLire = new Button("▶  Lire");
        btnLire.setStyle(
            "-fx-background-color: " + (estActuel ? "#E50914" : "#333333") + ";" +
            "-fx-text-fill: white; -fx-font-weight: bold;" +
            "-fx-padding: 8 16 8 16; -fx-background-radius: 4; -fx-cursor: hand;"
        );

        btnLire.setOnAction(e -> { mettreAJourInfosEpisode(ep); rafraichirListeEpisodes(ep); });
        carte.setOnMouseClicked(e -> { mettreAJourInfosEpisode(ep); rafraichirListeEpisodes(ep); });

        carte.getChildren().addAll(miniature, infos, btnLire);
        return carte;
    }

    private void rafraichirListeEpisodes(Episode epSelectionne) {
        listeEpisodes.getChildren().clear();
        for (Episode ep : episodesDB) {
            boolean estActuel = ep.getId() == epSelectionne.getId();
            listeEpisodes.getChildren().add(creerCarteEpisode(ep, estActuel));
        }
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
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            btnVolume.setText(newVal.doubleValue() == 0 ? "🔇" : "Vol")
        );
    }

    // =============================================
    // HANDLERS PLAYER
    // =============================================
    @FXML private void onPlayPause() {
        if (mediaPlayer == null) return;
        isPlaying = !isPlaying;
        if (isPlaying) { mediaPlayer.play();  btnPlayPause.setText("⏸"); }
        else           { mediaPlayer.pause(); btnPlayPause.setText("▶"); }
    }

    @FXML private void onRewind() {
        if (mediaPlayer == null) return;
        mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
    }

    @FXML private void onForward() {
        if (mediaPlayer == null) return;
        mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
    }

    @FXML private void onSubtitles() {
        subtitlesOn = !subtitlesOn;
        btnSubtitles.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;" +
            "-fx-cursor: hand; -fx-border-color: " + (subtitlesOn ? "#E50914" : "white") + ";" +
            "-fx-border-radius: 3; -fx-padding: 2 6 2 6;");
    }

    @FXML private void onVolume() {
        if (mediaPlayer == null) return;
        isMuted = !isMuted;
        mediaPlayer.setMute(isMuted);
        volumeSlider.setValue(isMuted ? 0 : 80);
        btnVolume.setText(isMuted ? "🔇" : "Vol");
    }

    @FXML private void onFullscreen() {
        javafx.stage.Stage stage = (javafx.stage.Stage) videoPane.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML private void onSkipIntro() {
        if (mediaPlayer == null || episodeActuel == null) return;
        int introSecs = episodeActuel.getDurreeIntro();
        mediaPlayer.seek(Duration.seconds(introSecs > 0 ? introSecs : 60));
    }

    // =============================================
    // HANDLERS BOUTONS ACTIONS
    // =============================================
    @FXML private void onLike() {
        likeCount++;
        btnLike.setText("J'aime (" + likeCount + ")");
        btnLike.setStyle("-fx-background-color: #E50914; -fx-text-fill: white;" +
            "-fx-padding: 8 18 8 18; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 13px;");
    }

    @FXML private void onDislike() {
        btnDislike.setStyle("-fx-background-color: #555555; -fx-text-fill: white;" +
            "-fx-padding: 8 18 8 18; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 13px;");
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
                    "-fx-background-color: #E50914; -fx-background-radius: 2 2 0 0; -fx-padding: 10 0 0 0;");
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

    @FXML private void handleAcceuil(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.home);
    }
    @FXML private void handleFilm(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.films);
    }
    @FXML private void handleSeries(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.series);
    }
    @FXML private void handleMyList(ActionEvent event) {
        ScreenManager.getInstance().navigateTo(Screen.myList);
    }
}