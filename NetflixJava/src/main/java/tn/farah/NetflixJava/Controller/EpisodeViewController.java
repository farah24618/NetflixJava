package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Entities.Rating;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Warning;
import tn.farah.NetflixJava.Service.CommentaireService;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.RatingService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.CardFactory;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

public class EpisodeViewController implements Initializable {

    private SerieService       serieService;
    private CommentaireService commentaireService;
    private SaisonService saisonService;
    private FavoriService favoriService;
    private UserService userService;
    private Connection cnnx;
    private static final String MEDIA_TYPE = "serie";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =============================================
    // INJECTION FXML -- NAVBAR / PROFIL
    // =============================================
    @FXML private HBox      profilBox;
    @FXML private Label     labelUserName;
    @FXML private StackPane avatarContainer;
    @FXML private ImageView avatarImage;
    @FXML private StackPane avatarFallback;
    @FXML private Label     avatarInitiale;
    @FXML private TextField searchField;

    // =============================================
    // INJECTION FXML -- POSTER
    // =============================================
    @FXML private StackPane videoPane;
    @FXML private ImageView posterImage;
    @FXML private Label     episodeTitleVideo;
    @FXML private Label     posterDesc;
    @FXML private Button    btnLire;
    @FXML private Button    btnFavoris;
    @FXML private Label     categories;

    // =============================================
    // INJECTION FXML -- INFOS EPISODE
    // =============================================
    @FXML private Label labelNumeroSaison;
    @FXML private Label labelNumeroEpisode;
    @FXML private Label labelDuree;
    @FXML private Label labelDateSortie;
    @FXML private Label episodeTitle;
    @FXML private Label episodeDesc;
    @FXML private Label castings;
    @FXML private Label labelWarnings;
    @FXML private Label labelWarningsApropos;

    // =============================================
    // INJECTION FXML -- BOUTONS ACTIONS
    // =============================================
    @FXML private Button btnLike;
    @FXML private Button btnDislike;
    @FXML private Button btnShare;
    @FXML private Button btnDownload;

    // =============================================
    // INJECTION FXML -- ONGLETS
    // =============================================
    @FXML private VBox tabApropos;
    @FXML private VBox tabEpisodes;
    @FXML private VBox tabBandes;
    @FXML private VBox tabCommentaires;
    @FXML private VBox tabSimilaires;

    // =============================================
    // INJECTION FXML -- PANNEAUX CONTENU
    // =============================================
    @FXML private VBox  panelEpisodes;
    @FXML private VBox  panelApropos;
    @FXML private VBox  panelBandes;
    @FXML private VBox  panelSimilaires;
    @FXML private VBox  panelCommentaires;
    @FXML private VBox  listeEpisodes;
    @FXML private HBox  listeSimilaires;

    // =============================================
    // LABELS DYNAMIQUES
    // =============================================
    @FXML private Label labelTitreSerie;
    @FXML private Label labelNbEpisodes;
    @FXML private Label labelSynopsis;
    @FXML private Label labelGenre;
    @FXML private Label labelAnnee;
    @FXML private Label labelProducteur;
    @FXML private Button btnNoter;

    // =============================================
    // ETAT INTERNE
    // =============================================
    private int           likeCount       = 0;
    private int           serieId         = -1;
    private List<Episode> episodesDB;
    private int           currentSaisonId = 1;
    private Episode       episodeActuel   = null;
    private final int userId = SessionManager.getInstance().getCurrentUserId();

    private VBox     commentListContainer;
    private TextArea commentInput;
    private CheckBox spoilerCheck;
    private RatingService ratingService;

    private final Pane[] overlayRef = new Pane[1];

    // =============================================
    // INITIALIZE
    // =============================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cnnx = ConxDB.getInstance();
        serieService       = new SerieService(cnnx);
        commentaireService = new CommentaireService(cnnx);
        favoriService      = new FavoriService(cnnx);
        saisonService      = new SaisonService(cnnx);
        userService        = new UserService(cnnx);
        ratingService      = new RatingService(cnnx);

        String pseudoUser = userService.findUserById(userId).getPseudo();
        chargerProfil((pseudoUser != null) ? pseudoUser : "Inconnu");

        if (episodesDB != null && !episodesDB.isEmpty()) {
            episodeActuel = episodesDB.get(0);
            mettreAJourInfosEpisode(episodeActuel);
        }

        activerOnglet(tabEpisodes);

        if (videoPane != null) {
            videoPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null && overlayRef[0] == null) {
                    CardFactory.createOverlay(newScene, overlayRef);
                    chargerSimilaires();
                }
            });
        } else {
            chargerSimilaires();
        }

        if (panelCommentaires != null) {
            construireInterfaceCommentaires();
        }
    }

    // =============================================
    // SETTERS
    // =============================================
    public void setSaisonId(int saisonId) {
        this.currentSaisonId = saisonId;
        if (serieService != null) {
            episodesDB = serieService.findEpisodeBySaison(saisonId);
            chargerListeEpisodes();
        }
    }

    public void setSerie(Serie serie) {
        this.serieId = serie.getId();
        chargerInfosSerie(this.serieId);

        List<Saison> saisons = saisonService.findBySerie(this.serieId);
        if (saisons != null && !saisons.isEmpty()) {
            this.currentSaisonId = saisons.get(0).getId();
            this.episodesDB = serieService.findEpisodeBySaison(this.currentSaisonId);
            chargerListeEpisodes();

            int nb = episodesDB != null ? episodesDB.size() : 0;
            if (labelNbEpisodes != null)
                labelNbEpisodes.setText(nb + " episode" + (nb > 1 ? "s" : ""));

            if (episodesDB != null && !episodesDB.isEmpty())
                mettreAJourInfosEpisode(episodesDB.get(0));
        }

        // ✅ FIX : mettre à jour le bouton favoris dès le chargement de la série
        mettreAJourBtnFavoris();

        if (commentListContainer != null) rafraichirListeCommentaires();

        if (ratingService != null && ratingService.hasRated(userId, this.serieId)) {
            double moyenne = ratingService.getFilmAverage(this.serieId);
            btnNoter.setText("⭐ " + moyenne + "/5");
            btnNoter.setDisable(true);
        }
    }

    /**
     * ✅ FIX : Met à jour le texte et le style du bouton favoris
     * selon que la série est déjà dans la liste ou non.
     */
    private void mettreAJourBtnFavoris() {
        if (btnFavoris == null || serieId == -1) return;
        boolean dejaDansListe = favoriService.exist(userId, serieId);
        if (dejaDansListe) {
            btnFavoris.setText("✔  Dans ma liste");
            btnFavoris.setStyle(
                "-fx-background-color: #46d369; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 15px;" +        // ← ajout font-size
                "-fx-padding: 11 28 11 28;" +                          // ← même padding que les autres
                "-fx-background-radius: 4; -fx-cursor: hand;");
        } else {
            btnFavoris.setText("＋  Ajouter aux favoris");
            btnFavoris.setStyle(
                "-fx-background-color: rgba(60,60,60,0.85); -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 15px;" +        // ← ajout font-size
                "-fx-padding: 11 28 11 28;" +                          // ← même padding que les autres
                "-fx-background-radius: 4; -fx-cursor: hand;" +
                "-fx-border-color: white; -fx-border-width: 1; -fx-border-radius: 4;");
        }
    }

    // =============================================
    // CHARGEMENT INFOS SERIE
    // =============================================
    private void chargerInfosSerie(int serieId) {
        if (serieId == -1) return;
        Serie serieAct = serieService.findById(serieId);
        if (serieAct == null) return;

        if (labelTitreSerie != null) labelTitreSerie.setText(serieAct.getTitre());
        if (labelSynopsis   != null) labelSynopsis.setText(
                serieAct.getSynopsis() != null ? serieAct.getSynopsis() : "");
        if (labelGenre      != null) labelGenre.setText(
                serieAct.getGenre() != null ? serieAct.getGenre() : "Inconnu");
        if (labelAnnee      != null) labelAnnee.setText(
                String.valueOf(serieAct.getDateSortie().getYear()));
        if (labelProducteur != null) labelProducteur.setText(
                serieAct.getProducteur() != null ? serieAct.getProducteur() : "N/A");

        int nb = episodesDB != null ? episodesDB.size() : 0;
        if (labelNbEpisodes != null)
            labelNbEpisodes.setText(nb + " episode" + (nb > 1 ? "s" : ""));

        if (posterImage != null && serieAct.getUrlImageCover() != null && !serieAct.getUrlImageCover().isBlank()) {
            try {
                Image img = new Image(serieAct.getUrlImageCover(), true);
                posterImage.setImage(img);
            } catch (Exception ignored) {}
        }

        if (episodeTitleVideo != null) episodeTitleVideo.setText(serieAct.getTitre());

        if (posterDesc != null && serieAct.getSynopsis() != null) {
            String syn = serieAct.getSynopsis();
            posterDesc.setText(syn.length() > 160 ? syn.substring(0, 157) + "..." : syn);
        }

        if (castings != null) {
            String casting = serieAct.getCasting();
            castings.setText(casting != null && !casting.isBlank() ? casting : "Non renseigne");
            castings.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
            castings.setWrapText(true);
        }

        String genreText = "-";
        if (serieAct.getGenres() != null && !serieAct.getGenres().isEmpty()) {
            genreText = serieAct.getGenres().stream()
                    .map(Category::getName).limit(2)
                    .reduce((a, b) -> a + " - " + b).orElse("-");
        }
        if (categories != null) {
            categories.setText(genreText);
            categories.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
        }
        String warningsText = "-";
        if (serieAct.getWarnings() != null && !serieAct.getWarnings().isEmpty()) {
            warningsText = serieAct.getWarnings().stream()
                    .map(Warning::getNom)
                    .reduce((a, b) -> a + " - " + b).orElse("-");
        }
        if (labelWarnings != null) {
            labelWarnings.setText(warningsText);
            labelWarnings.setStyle("-fx-text-fill: #d30000; -fx-font-size: 13px;");
        }
        if (labelWarningsApropos != null) {
            labelWarningsApropos.setText(warningsText);
            labelWarningsApropos.setStyle("-fx-text-fill: #E50914; -fx-font-size: 13px; -fx-font-weight: bold;");
        }
    }

    // =============================================
    // MISE A JOUR INFOS EPISODE
    // =============================================
    private void mettreAJourInfosEpisode(Episode ep) {
        episodeActuel = ep;

        String numero = String.valueOf(ep.getNumeroEpisode());
        Saison saison = null;

        try {
            saison = saisonService.getSaisonbyEpisodeId(ep.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String saisonNbr = (saison != null) ? String.valueOf(saison.getNumeroSaison()) : "?";
        String titre = ep.getTitre();
        String duree = ep.getDuree() + " min";
        String resume = ep.getResume() != null ? ep.getResume() : "";

        labelNumeroSaison.setText(saisonNbr);
        labelNumeroEpisode.setText(numero);
        episodeTitle.setText(titre);
        labelDuree.setText(duree);
        labelDateSortie.setText("");
        episodeDesc.setText(resume);

        if (episodeTitleVideo != null) {
            episodeTitleVideo.setText(
                    "S0" + saisonNbr + " E" + String.format("%02d", ep.getNumeroEpisode())
                    + " - " + titre);
        }

        if (posterDesc != null) {
            String shortDesc = resume.length() > 160 ? resume.substring(0, 157) + "..." : resume;
            posterDesc.setText(shortDesc);
        }

        if (posterImage != null && ep.getMiniatureUrl() != null && !ep.getMiniatureUrl().isBlank()) {
            try {
                InputStream imgStream = getClass().getResourceAsStream(ep.getMiniatureUrl());
                if (imgStream != null) {
                    posterImage.setImage(new Image(imgStream));
                }
            } catch (Exception ignored) {}
        }
    }

    // =============================================
    // PROFIL
    // =============================================
    private void chargerProfil(String nom) {
        labelUserName.setText(nom);
    }

    @FXML private void onProfilClicked() {
        System.out.println("Profil : " + labelUserName.getText());
    }

    // =============================================
    // LISTE DES EPISODES
    // =============================================
    private void chargerListeEpisodes() {
        listeEpisodes.getChildren().clear();
        if (episodesDB == null || episodesDB.isEmpty()) {
            Label vide = new Label("Aucun episode disponible.");
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
    // CARTE EPISODE
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
                (estActuel ? "-fx-border-color: #E50914; -fx-border-radius: 6; -fx-border-width: 1;" : ""));

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

        Label playIcon = new Label(">");
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

        Label titreLabel = new Label("Episode " + numero + " - " + titre);
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
            Label badge = new Label("En cours");
            badge.setTextFill(Color.web("#E50914"));
            badge.setFont(Font.font("System", FontWeight.BOLD, 11));
            infos.getChildren().addAll(ligneTitre, badge, descLabel);
        } else {
            infos.getChildren().addAll(ligneTitre, descLabel);
        }

        Button btnLireLocal = new Button(">  Lire");
        btnLireLocal.setStyle(
                "-fx-background-color: " + (estActuel ? "#E50914" : "#333333") + ";" +
                "-fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-padding: 8 16 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        btnLireLocal.setOnAction(e -> { mettreAJourInfosEpisode(ep); rafraichirListeEpisodes(ep); });
        carte.setOnMouseClicked(e -> { mettreAJourInfosEpisode(ep); rafraichirListeEpisodes(ep); });

        carte.getChildren().addAll(miniature, infos, btnLireLocal);
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
        if (panelSimilaires == null) return;
        panelSimilaires.getChildren().clear();
        if (serieId == -1) return;

        Serie serieActuelle = serieService.findById(serieId);
        if (serieActuelle == null) return;

        List<Serie> similaires = serieService.getAllSeries().stream()
            .filter(s -> s.getId() != serieId)
            .filter(s -> s.getGenres() != null && serieActuelle.getGenres() != null &&
                s.getGenres().stream().anyMatch(c ->
                    serieActuelle.getGenres().stream()
                        .anyMatch(sc -> sc.getName().equals(c.getName()))))
            .limit(5)
            .collect(java.util.stream.Collectors.toList());

        if (similaires.isEmpty()) {
            Label vide = new Label("Aucune série similaire trouvée.");
            vide.setTextFill(Color.web("#aaaaaa"));
            vide.setFont(Font.font(14));
            panelSimilaires.getChildren().add(vide);
            return;
        }

        VBox liste = new VBox(10);
        for (Serie s : similaires) {
            liste.getChildren().add(creerCarteSimiliaire(s));
        }
        panelSimilaires.getChildren().add(liste);
    }

    private HBox creerCarteSimiliaire(Serie s) {
        HBox carte = new HBox(16);
        carte.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 6;" +
                       "-fx-padding: 10; -fx-cursor: hand;");
        carte.setAlignment(Pos.CENTER_LEFT);

        StackPane imgPane = new StackPane();
        imgPane.setPrefSize(160, 90);
        imgPane.setMinSize(160, 90);
        imgPane.setMaxSize(160, 90);
        imgPane.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 4;");

        if (s.getUrlImageCover() != null && !s.getUrlImageCover().isEmpty()) {
            try {
                Image img = new Image(s.getUrlImageCover(), true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(160); iv.setFitHeight(90); iv.setPreserveRatio(false);
                imgPane.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        Rectangle clip = new Rectangle(160, 90);
        clip.setArcWidth(6); clip.setArcHeight(6);
        imgPane.setClip(clip);

        VBox infos = new VBox(6);
        HBox.setHgrow(infos, Priority.ALWAYS);

        Label titre = new Label(s.getTitre());
        titre.setTextFill(Color.WHITE);
        titre.setFont(Font.font("System", FontWeight.BOLD, 14));

        String genreText = "-";
        if (s.getGenres() != null && !s.getGenres().isEmpty()) {
            genreText = s.getGenres().stream()
                .map(Category::getName).limit(2)
                .reduce((a, b) -> a + " · " + b).orElse("-");
        }
        Label genre = new Label(genreText);
        genre.setTextFill(Color.web("#aaaaaa"));
        genre.setFont(Font.font(12));

        Label statut = new Label(s.isTerminee() ? "Terminée" : "En cours");
        statut.setTextFill(s.isTerminee() ? Color.web("#aaaaaa") : Color.web("#46d369"));
        statut.setFont(Font.font(12));

        infos.getChildren().addAll(titre, genre, statut);
        carte.getChildren().addAll(imgPane, infos);

        carte.setOnMouseEntered(e -> carte.setStyle(
            "-fx-background-color: #252525; -fx-background-radius: 6;" +
            "-fx-padding: 10; -fx-cursor: hand;"));
        carte.setOnMouseExited(e -> carte.setStyle(
            "-fx-background-color: #1a1a1a; -fx-background-radius: 6;" +
            "-fx-padding: 10; -fx-cursor: hand;"));

        carte.setOnMouseClicked(e -> setSerie(s));

        return carte;
    }

    // =============================================
    // COMMENTAIRES -- Construction UI
    // =============================================
    private void construireInterfaceCommentaires() {
        panelCommentaires.getChildren().clear();
        panelCommentaires.setSpacing(20);

        Label titre = new Label("Commentaires");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox formulaire = new VBox(12);
        formulaire.setStyle(
                "-fx-background-color: #1f1f1f; -fx-background-radius: 8;" +
                "-fx-padding: 16; -fx-border-color: #333333;" +
                "-fx-border-radius: 8; -fx-border-width: 1;");

        Label formTitle = new Label("Ecrire un commentaire");
        formTitle.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px; -fx-font-weight: bold;");

        commentInput = new TextArea();
        commentInput.setPromptText("Partagez votre avis sur cet episode...");
        commentInput.setPrefRowCount(3);
        commentInput.setWrapText(true);
        commentInput.setStyle(
                "-fx-control-inner-background: #2a2a2a;" +
                "-fx-text-fill: white;"                  +
                "-fx-prompt-text-fill: #666666;"         +
                "-fx-background-radius: 6;"              +
                "-fx-border-color: #444444;"             +
                "-fx-border-radius: 6;"                  +
                "-fx-border-width: 1;"                   +
                "-fx-font-size: 13px;");

        spoilerCheck = new CheckBox("  Ce commentaire contient un spoiler");
        spoilerCheck.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 13px;");

        HBox formActions = new HBox(12);
        formActions.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #aaaaaa;" +
                "-fx-border-color: #555555; -fx-border-radius: 4;" +
                "-fx-border-width: 1; -fx-padding: 7 18 7 18; -fx-cursor: hand;");
        btnAnnuler.setOnAction(e -> {
            commentInput.clear();
            spoilerCheck.setSelected(false);
        });

        Button btnPublier = new Button("Publier");
        btnPublier.setStyle(
                "-fx-background-color: #E50914; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-padding: 7 22 7 22;" +
                "-fx-background-radius: 4; -fx-cursor: hand;");
        btnPublier.setOnAction(e -> publierCommentaire());

        formActions.getChildren().addAll(btnAnnuler, btnPublier);
        formulaire.getChildren().addAll(formTitle, commentInput, spoilerCheck, formActions);

        commentListContainer = new VBox(12);
        panelCommentaires.getChildren().addAll(titre, formulaire, commentListContainer);

        rafraichirListeCommentaires();
    }

    private void publierCommentaire() {
        String texte = commentInput != null ? commentInput.getText().trim() : "";
        if (texte.isEmpty()) return;
        if (serieId == -1) return;

        boolean isSpoiler = spoilerCheck != null && spoilerCheck.isSelected();

        Commentaire c = new Commentaire();
        c.setUserId(userId);
        c.setMediaId(serieId);
        c.setContenu(texte);
        c.setSpoiler(isSpoiler);

        boolean ok = commentaireService.ajouterCommentaire(c);
        if (ok) {
            if (commentInput != null) commentInput.clear();
            if (spoilerCheck != null) spoilerCheck.setSelected(false);
            rafraichirListeCommentaires();
        } else {
            System.err.println("Impossible d'enregistrer le commentaire.");
        }
    }

    private void rafraichirListeCommentaires() {
        if (commentListContainer == null) return;
        commentListContainer.getChildren().clear();

        if (serieId == -1) return;

        List<Commentaire> commentaires =
                commentaireService.getCommentairesByMedia(serieId, MEDIA_TYPE);

        if (commentaires == null || commentaires.isEmpty()) {
            Label vide = new Label("Soyez le premier a commenter !");
            vide.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");
            commentListContainer.getChildren().add(vide);
            return;
        }

        for (Commentaire c : commentaires) {
            commentListContainer.getChildren().add(creerCarteCommentaire(c));
        }
    }

    private VBox creerCarteCommentaire(Commentaire comment) {
        VBox carte = new VBox(8);
        carte.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 8;" +
                "-fx-padding: 14; -fx-border-color: #2a2a2a;" +
                "-fx-border-radius: 8; -fx-border-width: 1;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        comment.setUsername(userService.findUserById(comment.getUserId()).getPseudo());

        StackPane avatarSP = new StackPane();
        avatarSP.setPrefSize(34, 34);
        avatarSP.setMinSize(34, 34);
        avatarSP.setStyle("-fx-background-color: #E50914; -fx-background-radius: 17;");
        String initial = (comment.getUsername() != null && !comment.getUsername().isEmpty())
                ? String.valueOf(comment.getUsername().charAt(0)).toUpperCase() : "?";
        Label initLabel = new Label(initial);
        initLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatarSP.getChildren().add(initLabel);

        Label userLabel = new Label(comment.getUsername() != null ? comment.getUsername() : "Anonyme");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(avatarSP, userLabel, spacer);

        if (comment.getDateCommentaire() != null) {
            Label dateLabel = new Label(comment.getDateCommentaire().format(DATE_FMT));
            dateLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
            header.getChildren().add(dateLabel);
        }

        if (comment.isSpoiler()) {
            Label spoilerBadge = new Label("SPOILER");
            spoilerBadge.setStyle(
                    "-fx-background-color: #ffcc0022; -fx-text-fill: #ffcc00;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 4; -fx-padding: 2 8 2 8;" +
                    "-fx-border-color: #ffcc0055; -fx-border-radius: 4; -fx-border-width: 1;");
            header.getChildren().add(spoilerBadge);
        }

        final boolean[] revealed = {!comment.isSpoiler()};
        Label textLabel = new Label(
                comment.isSpoiler() && !revealed[0]
                        ? "Cliquez sur Voir le spoiler pour reveler ce commentaire."
                        : comment.getContenu());
        textLabel.setStyle("-fx-text-fill: " +
                (comment.isSpoiler() && !revealed[0] ? "#555555" : "#cccccc") +
                "; -fx-font-size: 13px;");
        textLabel.setWrapText(true);

        if (comment.isSpoiler()) {
            Button btnReveal = new Button("Voir le spoiler");
            btnReveal.setStyle(
                    "-fx-background-color: #2a2a2a; -fx-text-fill: #ffcc00;" +
                    "-fx-font-size: 12px; -fx-padding: 5 12 5 12;" +
                    "-fx-background-radius: 4; -fx-cursor: hand;");
            btnReveal.setOnAction(e -> {
                revealed[0] = true;
                textLabel.setText(comment.getContenu());
                textLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
                btnReveal.setVisible(false);
                btnReveal.setManaged(false);
            });
            carte.getChildren().addAll(header, btnReveal, textLabel);
        } else {
            carte.getChildren().addAll(header, textLabel);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding: 6 0 0 0;");

        final int[] likesCount = {comment.getLikes()};
        Button btnLikeC = new Button("J'aime" + (likesCount[0] > 0 ? " (" + likesCount[0] + ")" : ""));
        btnLikeC.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: #aaaaaa;" +
                "-fx-font-size: 12px; -fx-padding: 5 14 5 14;" +
                "-fx-background-radius: 4; -fx-cursor: hand;");
        btnLikeC.setOnAction(e -> {
            commentaireService.ajouterLike(comment.getId());
            likesCount[0]++;
            btnLikeC.setText("J'aime (" + likesCount[0] + ")");
            btnLikeC.setStyle(
                    "-fx-background-color: #2a2a2a; -fx-text-fill: #E50914;" +
                    "-fx-font-size: 12px; -fx-padding: 5 14 5 14;" +
                    "-fx-background-radius: 4; -fx-cursor: hand;");
            btnLikeC.setDisable(true);
            if (cnnx != null && comment.getUserId() != userId) {
                NotificationService notifService = new NotificationService(cnnx);
                Serie serieActuelle = serieService.findById(serieId);
                Notification n = new Notification(0, comment.getUserId(), "COMMENTAIRE",
                    "Quelqu'un a aimé votre commentaire",
                    "Votre commentaire sur \"" + (serieActuelle != null ? serieActuelle.getTitre() : "")
                        + "\" a reçu un J'aime.",
                    java.time.LocalDate.now().toString(),
                    false, false);
                notifService.addNotification(n);
            }
        });

        Button btnReport = new Button("Signaler");
        btnReport.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #aaaaaa;" +
                "-fx-font-size: 12px; -fx-padding: 5 14 5 14;" +
                "-fx-background-radius: 4; -fx-cursor: hand;");
        btnReport.setOnAction(e -> {
            comment.setSignale(true);
            boolean ok = commentaireService.update(comment);
            if (ok) {
                btnReport.setText("Signalé");
                btnReport.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #555555;" +
                        "-fx-font-size: 12px; -fx-padding: 5 14 5 14;" +
                        "-fx-background-radius: 4; -fx-cursor: hand;");
                btnReport.setDisable(true);
                if (cnnx != null) {
                    NotificationService notifService = new NotificationService(cnnx);
                    Serie serieActuelle = serieService.findById(serieId);
                    String titreSerie = (serieActuelle != null) ? serieActuelle.getTitre() : "Inconnue";

                    // Note : Ici on suppose que l'ID 1 est l'Admin, ou on laisse 0 si le système le gère.
                    // On informe l'Admin qu'un utilisateur (userId) a signalé quelque chose.
                    Notification n = new Notification(
                        0,                                  // ID auto-incrémenté
                        1,                                  // ID du destinataire (Admin)
                        "SIGNALEMENT",                      // Type ou Expéditeur (ex: pseudo de l'utilisateur actuel)
                        "Le commentaire #" + comment.getId(), 
                        "a été signalé sur la série : " + titreSerie,
                        java.time.LocalDate.now().toString(),
                        false,                              // Lu
                        false                               // Supprimé
                    );

                    notifService.addNotification(n);
                }
            } else {
                System.err.println("Impossible de signaler le commentaire.");
            }
        });
        actions.getChildren().addAll(btnLikeC, btnReport);
        carte.getChildren().add(actions);
        return carte;
    }

    // =============================================
    // HANDLERS POSTER
    // =============================================
    @FXML private void onLire() {
        if (episodeActuel == null) return;
        videoController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.video);
        if (ctrl != null) ctrl.initEpisode(episodeActuel.getId());
    }

    @FXML
    private void onFavoris() {
        if (serieId == -1) return;

        Favori fav = new Favori();
        fav.setUserId(userId);
        fav.setMediaId(serieId);

        if (!favoriService.exist(userId, serieId)) {
            favoriService.ajouterFavori(fav);
        } else {
            favoriService.supprimerFavori(userId, serieId);
        }
        // ✅ FIX : toujours mettre à jour le style du bouton après l'action
        mettreAJourBtnFavoris();
    }

    // =============================================
    // HANDLERS BOUTONS
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
        alert.setContentText("Lien copie dans le presse-papiers !");
        alert.showAndWait();
    }

    @FXML private void onDownload() {
        btnDownload.setText("Telechargement...");
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

        panelApropos.setVisible(false);        panelApropos.setManaged(false);
        panelEpisodes.setVisible(false);       panelEpisodes.setManaged(false);
        panelBandes.setVisible(false);         panelBandes.setManaged(false);
        panelSimilaires.setVisible(false);     panelSimilaires.setManaged(false);
        panelCommentaires.setVisible(false);   panelCommentaires.setManaged(false);

        if      (ongletActif == tabEpisodes)     { panelEpisodes.setVisible(true);     panelEpisodes.setManaged(true); }
        else if (ongletActif == tabApropos)      { panelApropos.setVisible(true);      panelApropos.setManaged(true); }
        else if (ongletActif == tabBandes)       { panelBandes.setVisible(true);       panelBandes.setManaged(true); }
        else if (ongletActif == tabSimilaires)   { panelSimilaires.setVisible(true);   panelSimilaires.setManaged(true); chargerSimilaires(); }
        else if (ongletActif == tabCommentaires) { panelCommentaires.setVisible(true); panelCommentaires.setManaged(true); }
    }

    @FXML
    private void onNoter() {
        if (ratingService.hasRated(userId, serieId)) {
            btnNoter.setText("⭐ Déjà noté");
            btnNoter.setDisable(true);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Déjà noté");
            alert.setHeaderText(null);
            alert.setContentText("Vous avez déjà noté cette série.");
            alert.showAndWait();
            return;
        }

        Stage popup = new Stage(StageStyle.UNDECORATED);
        popup.initModality(Modality.APPLICATION_MODAL);

        final int[] selectedScore = {0};
        Label[] stars = new Label[5];

        Label titre = new Label("Notez cette série");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label sousTitre = new Label("Sélectionnez une note de 1 à 5 étoiles");
        sousTitre.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");

        HBox starBox = new HBox(8);
        starBox.setAlignment(Pos.CENTER);

        Label lblScore = new Label("Aucune note sélectionnée");
        lblScore.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

        for (int i = 0; i < 5; i++) {
            final int idx = i + 1;
            Label star = new Label("★");
            star.setStyle("-fx-font-size: 36px; -fx-text-fill: #555555; -fx-cursor: hand;");

            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle("-fx-font-size: 36px; -fx-text-fill: "
                            + (j < idx ? "#FFD700" : "#555555") + "; -fx-cursor: hand;");
            });
            starBox.setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle("-fx-font-size: 36px; -fx-text-fill: "
                            + (j < selectedScore[0] ? "#FFD700" : "#555555") + "; -fx-cursor: hand;");
            });
            star.setOnMouseClicked(e -> {
                selectedScore[0] = idx;
                lblScore.setText(idx + " étoile" + (idx > 1 ? "s" : ""));
                lblScore.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 13px;");
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle("-fx-font-size: 36px; -fx-text-fill: "
                            + (j < idx ? "#FFD700" : "#555555") + "; -fx-cursor: hand;");
            });

            stars[i] = star;
            starBox.getChildren().add(star);
        }

        Button btnConfirmer = new Button("Confirmer");
        btnConfirmer.setStyle(
                "-fx-background-color: #E50914; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-font-size: 13px; -fx-padding: 9 24 9 24; -fx-background-radius: 4; -fx-cursor: hand;");
        btnConfirmer.setOnAction(e -> {
            if (selectedScore[0] == 0) {
                lblScore.setText("⚠ Veuillez sélectionner une note !");
                lblScore.setStyle("-fx-text-fill: #E50914; -fx-font-size: 13px;");
                return;
            }
            Rating rating = new Rating(userId, serieId, selectedScore[0]);
            boolean ok = ratingService.addRating(rating);
            popup.close();

            if (ok) {
                double moyenne = ratingService.getFilmAverage(serieId);
                btnNoter.setText("⭐ " + moyenne + "/5");
                btnNoter.setDisable(true);
            }

            Alert alert = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(ok ? "Merci !" : "Erreur");
            alert.setHeaderText(null);
            alert.setContentText(ok
                    ? "Votre note de " + selectedScore[0] + " étoile(s) a été enregistrée.\nMoyenne actuelle : " + ratingService.getFilmAverage(serieId) + "/5"
                    : "Erreur lors de l'enregistrement.");
            alert.showAndWait();
        });

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.setStyle(
                "-fx-background-color: #2a2a2a; -fx-text-fill: #aaaaaa; -fx-font-size: 13px;" +
                "-fx-padding: 9 24 9 24; -fx-background-radius: 4; -fx-cursor: hand;" +
                "-fx-border-color: #555555; -fx-border-width: 1; -fx-border-radius: 4;");
        btnAnnuler.setOnAction(e -> popup.close());

        HBox btnBox = new HBox(12, btnConfirmer, btnAnnuler);
        btnBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(16, titre, sousTitre, starBox, lblScore, btnBox);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 10;" +
                "-fx-border-color: #333333; -fx-border-width: 1; -fx-border-radius: 10;");
        layout.setPadding(new Insets(30, 40, 30, 40));

        Scene scene = new Scene(layout, 360, 260);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
    }

    // =============================================
    // NAVIGATION
    // =============================================
    @FXML private void handleAcceuil(ActionEvent event) { ScreenManager.getInstance().navigateTo(Screen.home); }
    @FXML private void handleFilm(ActionEvent event)    { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void handleSeries(ActionEvent event)  { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML private void handleMyList(ActionEvent event)  { ScreenManager.getInstance().navigateTo(Screen.myList); }
}