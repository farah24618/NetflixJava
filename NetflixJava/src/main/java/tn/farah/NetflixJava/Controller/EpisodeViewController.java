package tn.farah.NetflixJava.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Service.CommentaireService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.utils.CardFactory;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

public class EpisodeViewController implements Initializable {

    private SerieService       serieService;
    private CommentaireService commentaireService;

    // mediaType doit correspondre exactement a la valeur stockee en DB
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
    @FXML private Label labelSaisonEpisode;
    @FXML private Label labelDuree;
    @FXML private Label labelDateSortie;
    @FXML private Label episodeTitle;
    @FXML private Label episodeDesc;
    @FXML private Label castings;

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

    // =============================================
    // ETAT INTERNE
    // =============================================
    private int           likeCount       = 0;
    private int           serieId         = -1;
    private List<Episode> episodesDB;
    private int           currentSaisonId = 1;
    private Episode       episodeActuel   = null;

    // References UI commentaires (construites en Java)
    private VBox     commentListContainer;
    private TextArea commentInput;
    private CheckBox spoilerCheck;

    private final Pane[] overlayRef = new Pane[1];

    // =============================================
    // INITIALIZE
    // =============================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serieService       = new SerieService(ConxDB.getInstance());
        commentaireService = new CommentaireService();

        episodesDB = serieService.findEpisodeBySaison(currentSaisonId);

        chargerListeEpisodes();
        chargerProfil("Enfants", "/images/profil.png");

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
        if (labelNbEpisodes != null && episodesDB != null) {
            int nb = episodesDB.size();
            labelNbEpisodes.setText(nb + " episode" + (nb > 1 ? "s" : ""));
        }
        // Recharge les commentaires maintenant qu'on connait serieId
        if (commentListContainer != null) {
            rafraichirListeCommentaires();
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

        if (posterImage != null) {
            String affiche = serieAct.getUrlImageCover();
            if (affiche != null && !affiche.isBlank()) {
                try {
                    InputStream s = getClass().getResourceAsStream(affiche);
                    if (s != null) posterImage.setImage(new Image(s));
                } catch (Exception ignored) {}
            }
        }

        if (episodeTitleVideo != null) episodeTitleVideo.setText(serieAct.getTitre());

        if (posterDesc != null && serieAct.getSynopsis() != null) {
            String syn = serieAct.getSynopsis();
            posterDesc.setText(syn.length() > 160 ? syn.substring(0, 157) + "..." : syn);
        }

        // Cast
        if (castings != null) {
            String casting = serieAct.getCasting();
            castings.setText(casting != null && !casting.isBlank() ? casting : "Non renseigne");
            castings.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
            castings.setWrapText(true);
        }

        // Genres -- BUG FIX: etait isEmpty() au lieu de !isEmpty()
        String genreText = "-";
        if (serieAct.getGenres() != null && !serieAct.getGenres().isEmpty()) {
            genreText = serieAct.getGenres().stream()
                    .map(Category::getName).limit(2)
                    .reduce((a, b) -> a + " - " + b).orElse("-");
        }
        if (categories != null) {
            categories.setText(genreText);
            categories.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        }
    }

    // =============================================
    // MISE A JOUR INFOS EPISODE
    // =============================================
    private void mettreAJourInfosEpisode(Episode ep) {
        episodeActuel = ep;

        String numero = String.valueOf(ep.getNumeroEpisode());
        String saison = String.valueOf(
                SaisonService.getSaisonbyEpisodeId(ep.getId()).getNumeroSaison());
        String titre  = ep.getTitre();
        String duree  = ep.getDuree() + " min";
        String resume = ep.getResume() != null ? ep.getResume() : "";

        labelNumeroSaison.setText(saison);
        labelNumeroEpisode.setText(numero);
        episodeTitle.setText(titre);
        labelSaisonEpisode.setText("Saison " + saison + " - Episode " + numero);
        labelDuree.setText(duree);
        labelDateSortie.setText("");
        episodeDesc.setText(resume);

        if (episodeTitleVideo != null)
            episodeTitleVideo.setText(
                    "S0" + saison + " E" + String.format("%02d", ep.getNumeroEpisode())
                    + " - " + titre);

        if (posterDesc != null) {
            String shortDesc = resume.length() > 160 ? resume.substring(0, 157) + "..." : resume;
            posterDesc.setText(shortDesc);
        }

        if (posterImage != null && ep.getMiniatureUrl() != null && !ep.getMiniatureUrl().isBlank()) {
            try {
                InputStream imgStream = getClass().getResourceAsStream(ep.getMiniatureUrl());
                if (imgStream != null) posterImage.setImage(new Image(imgStream));
            } catch (Exception ignored) {}
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
        List<Serie> toutesLesSeries = serieService.getAllSeries();
        List<Serie> similaires = toutesLesSeries.stream()
                .filter(s -> s.getId() != serieId).limit(10).toList();

        if (similaires.isEmpty()) {
            Label vide = new Label("Aucune serie similaire trouvee.");
            vide.setTextFill(Color.web("#aaaaaa"));
            vide.setFont(Font.font(14));
            panelSimilaires.getChildren().setAll(vide);
            return;
        }
        VBox carousel = CardFactory.buildSerieCarousel(
                "Series similaires", similaires, overlayRef,
                serie -> System.out.println("Serie similaire : " + serie.getTitre()));
        panelSimilaires.getChildren().setAll(carousel);
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

        // ── FIX TextArea blanc ──────────────────────────────────────────────────
        // JavaFX ignore -fx-background-color sur TextArea via inline style.
        // Le fond visible est controle par -fx-control-inner-background.
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
        // ────────────────────────────────────────────────────────────────────────

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

    // =============================================
    // COMMENTAIRES -- Publier en DB
    // =============================================
    private void publierCommentaire() {
    	final int    userId = SessionManager.getInstance().getCurrentUserId();
        String texte = commentInput != null ? commentInput.getText().trim() : "";
        if (texte.isEmpty()) return;
        if (serieId == -1) return;

        boolean isSpoiler = spoilerCheck != null && spoilerCheck.isSelected();

        Commentaire c = new Commentaire(
        		userId,
                serieId,
                0,                          // userId -- remplace par l'id utilisateur connecte si dispo
                texte,
                isSpoiler
        );
        System.out.println(c.getUserId());

        boolean ok = commentaireService.ajouterCommentaire(c);
        if (ok) {
            if (commentInput != null) commentInput.clear();
            if (spoilerCheck != null) spoilerCheck.setSelected(false);
            rafraichirListeCommentaires();   // recharge depuis la DB
        } else {
            System.err.println("Impossible d'enregistrer le commentaire.");
        }
    }

    // =============================================
    // COMMENTAIRES -- Recharge depuis DB
    // =============================================
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

    // =============================================
    // COMMENTAIRES -- Carte (entite DB)
    // =============================================
    private VBox creerCarteCommentaire(Commentaire comment) {
        VBox carte = new VBox(8);
        carte.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 8;" +
                "-fx-padding: 14; -fx-border-color: #2a2a2a;" +
                "-fx-border-radius: 8; -fx-border-width: 1;");

        // Header : avatar + username + date + badge spoiler
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

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

        // Texte (masque si spoiler jusqu'au clic)
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

        // Actions : Like (DB) + Signaler
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
            commentaireService.ajouterLike(comment.getId());   // persiste en DB
            likesCount[0]++;
            btnLikeC.setText("J'aime (" + likesCount[0] + ")");
            btnLikeC.setStyle(
                    "-fx-background-color: #2a2a2a; -fx-text-fill: #E50914;" +
                    "-fx-font-size: 12px; -fx-padding: 5 14 5 14;" +
                    "-fx-background-radius: 4; -fx-cursor: hand;");
            btnLikeC.setDisable(true);   // empeche le double-like
        });

        Button btnReport = new Button("Signaler");
        btnReport.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #aaaaaa;" +
                "-fx-font-size: 12px; -fx-padding: 5 14 5 14;" +
                "-fx-background-radius: 4; -fx-cursor: hand;");
        //ici je veux signaler le commentaire en vraie en appelant commentService
        btnReport.setOnAction(e -> {
            btnReport.setText("Signale");
            btnReport.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #555555;" +
                    "-fx-font-size: 12px; -fx-padding: 5 14 5 14;" +
                    "-fx-background-radius: 4; -fx-cursor: hand;");
            btnReport.setDisable(true);
        });

        actions.getChildren().addAll(btnLikeC, btnReport);
        carte.getChildren().add(actions);
        return carte;
    }

    // =============================================
    // HANDLERS POSTER
    // =============================================
    @FXML private void onLire() {
        if (episodeActuel != null) System.out.println("Lire : " + episodeActuel.getTitre());
    }

    @FXML private void onFavoris() {
        if (btnFavoris == null) return;
        btnFavoris.setText("Ajoute aux favoris");
        btnFavoris.setStyle(
                "-fx-background-color: #E50914; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 15px;" +
                "-fx-padding: 11 28 11 28; -fx-background-radius: 4; -fx-cursor: hand;");
        
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
        else if (ongletActif == tabSimilaires)   { panelSimilaires.setVisible(true);   panelSimilaires.setManaged(true); }
        else if (ongletActif == tabCommentaires) { panelCommentaires.setVisible(true); panelCommentaires.setManaged(true); }
    }

    // =============================================
    // NAVIGATION
    // =============================================
    //!!!!ces bouton ne marche pas
    @FXML private void handleAcceuil(ActionEvent event) { ScreenManager.getInstance().navigateTo(Screen.home); }
    @FXML private void handleFilm(ActionEvent event)    { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void handleSeries(ActionEvent event)  { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML private void handleMyList(ActionEvent event)  { ScreenManager.getInstance().navigateTo(Screen.myList); }
}