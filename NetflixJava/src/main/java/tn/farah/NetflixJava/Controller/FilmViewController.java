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

import java.net.URL;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Entities.Rating;
import tn.farah.NetflixJava.Entities.Warning;
import tn.farah.NetflixJava.Service.CommentaireService;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.RatingService;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.CardFactory;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;

public class FilmViewController implements Initializable {

    private FilmService        filmService;
    private CommentaireService commentaireService;
    private FavoriService      favoriService;
    private UserService        userService;
    private Connection         cnnx;

    private static final String   MEDIA_TYPE = "film";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =============================================
    // INJECTION FXML -- NAVBAR
    // =============================================
    @FXML private HBox      profilBox;
    @FXML private Label     labelUserName;
    @FXML private StackPane avatarContainer;
    @FXML private ImageView avatarImage;

    // =============================================
    // INJECTION FXML -- POSTER
    // =============================================
    @FXML private StackPane videoPane;
    @FXML private ImageView posterImage;
    @FXML private Label     filmTitleVideo;
    @FXML private Label     posterDesc;
    @FXML private Button    btnLire;
    @FXML private Button    btnFavoris;
    @FXML private Label     categories;

    // =============================================
    // INJECTION FXML -- INFOS FILM
    // =============================================
    @FXML private Label filmTitle;
    @FXML private Label labelDuree;
    @FXML private Label labelDateSortie;
    @FXML private Label filmDesc;
    @FXML private Label castings;
    @FXML private Label labelWarnings;

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
    @FXML private VBox tabBandes;
    @FXML private VBox tabCommentaires;
    @FXML private VBox tabSimilaires;

    // =============================================
    // INJECTION FXML -- PANNEAUX CONTENU
    // =============================================
    @FXML private VBox panelApropos;
    @FXML private VBox panelBandes;
    @FXML private VBox panelSimilaires;
    @FXML private VBox panelCommentaires;

    // =============================================
    // LABELS DYNAMIQUES (À PROPOS)
    // =============================================
    @FXML private Label labelSynopsis;
    @FXML private Label labelGenre;
    @FXML private Label labelAnnee;
    @FXML private Label labelProducteur;
    @FXML private Label labelWarningsApropos;

    // =============================================
    // ETAT INTERNE
    // =============================================
    private int    likeCount = 0;
    private int    filmId    = -1;
    private Film   filmActuel = null;
    private final int userId = SessionManager.getInstance().getCurrentUserId();

    private VBox     commentListContainer;
    private TextArea commentInput;
    private CheckBox spoilerCheck;
    private RatingService ratingService;
    @FXML private Button btnNoter;

    // =============================================
    // INITIALIZE
    // =============================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cnnx               = ConxDB.getInstance();
        filmService        = new FilmService(cnnx);
        commentaireService = new CommentaireService(cnnx);
        favoriService      = new FavoriService(cnnx);
        userService        = new UserService(cnnx);
        ratingService      = new RatingService(cnnx);
        String pseudo = userService.findUserById(userId).getPseudo();
        chargerProfil(pseudo != null ? pseudo : "Inconnu");

        activerOnglet(tabApropos);

        if (panelCommentaires != null) {
            construireInterfaceCommentaires();
        }
    }

    // =============================================
    // SETTER PRINCIPAL
    // =============================================
    public void setFilm(Film film) {
        this.filmId    = film.getId();
        this.filmActuel = film;
        chargerInfosFilm(film);

        // ✅ FIX : mettre à jour l'état du bouton favoris dès le chargement
        mettreAJourBtnFavoris();

        if (commentListContainer != null) {
            rafraichirListeCommentaires();
        }

        // Vérifier si l'utilisateur a déjà noté
        if (btnNoter != null && ratingService != null) {
            if (ratingService.hasRated(userId, this.filmId)) {
                double moyenne = ratingService.getFilmAverage(this.filmId);
                btnNoter.setText("⭐ " + moyenne + "/5");
                btnNoter.setDisable(true);
            } else {
                btnNoter.setText("⭐ Noter");
                btnNoter.setDisable(false);
            }
        }
    }

    /**
     * ✅ FIX : Met à jour le texte et le style du bouton favoris
     * selon que le film est déjà dans la liste ou non.
     */
    private void mettreAJourBtnFavoris() {
        if (btnFavoris == null || filmId == -1) return;
        boolean dejaDansListe = favoriService.exist(userId, filmId);
        if (dejaDansListe) {
            btnFavoris.setText("✔  Dans ma liste");
            btnFavoris.setStyle(
                "-fx-background-color: #46d369; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 4;" +
                "-fx-padding: 8 18 8 18; -fx-cursor: hand;");
        } else {
            btnFavoris.setText("＋  Ajouter aux favoris");
            btnFavoris.setStyle(
                "-fx-background-color: #333333; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 4;" +
                "-fx-padding: 8 18 8 18; -fx-cursor: hand;");
        }
    }

    // =============================================
    // CHARGEMENT INFOS FILM
    // =============================================
    private void chargerInfosFilm(Film film) {
        if (film == null) return;

        if (filmTitleVideo != null) filmTitleVideo.setText(film.getTitre());
        if (posterDesc != null && film.getSynopsis() != null) {
            String syn = film.getSynopsis();
            posterDesc.setText(syn.length() > 160 ? syn.substring(0, 157) + "..." : syn);
        }
        if (posterImage != null && film.getUrlImageCover() != null && !film.getUrlImageCover().isBlank()) {
            try {
                Image img = new Image(film.getUrlImageCover(), true);
                posterImage.setImage(img);
            } catch (Exception ignored) {}
        }

        if (filmTitle    != null) filmTitle.setText(film.getTitre());
        if (labelDuree   != null) labelDuree.setText(film.getDuree() + " min");
        if (labelDateSortie != null && film.getDateSortie() != null)
            labelDateSortie.setText(String.valueOf(film.getDateSortie().getYear()));
        if (filmDesc != null)
            filmDesc.setText(film.getSynopsis() != null ? film.getSynopsis() : "");

        String genreText = "-";
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreText = film.getGenres().stream()
                    .map(Category::getName).limit(3)
                    .reduce((a, b) -> a + " - " + b).orElse("-");
        }
        if (categories != null) {
            categories.setText(genreText);
            categories.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
        }
        if (labelGenre != null) labelGenre.setText(genreText);

        String warningsText = "-";
        if (film.getWarnings() != null && !film.getWarnings().isEmpty()) {
            warningsText = film.getWarnings().stream()
                    .map(Warning::getNom)
                    .reduce((a, b) -> a + " - " + b).orElse("-");
        }
        if (labelWarnings != null) {
            labelWarnings.setText(warningsText);
            labelWarnings.setStyle("-fx-text-fill: #d30000; -fx-font-size: 13px;");
        }
        if (labelWarningsApropos != null) {
            labelWarningsApropos.setText(warningsText);
        }

        if (castings != null) {
            String casting = film.getCasting();
            castings.setText(casting != null && !casting.isBlank() ? casting : "Non renseigné");
            castings.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 13px;");
            castings.setWrapText(true);
        }

        if (labelSynopsis   != null) labelSynopsis.setText(film.getSynopsis() != null ? film.getSynopsis() : "");
        if (labelProducteur != null) labelProducteur.setText(film.getProducteur() != null ? film.getProducteur() : "N/A");
        if (labelAnnee      != null && film.getDateSortie() != null)
            labelAnnee.setText(String.valueOf(film.getDateSortie().getYear()));
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
    // SIMILAIRES
    // =============================================
    private void chargerSimilaires() {
        if (panelSimilaires == null || filmId == -1) return;
        panelSimilaires.getChildren().clear();

        Label titre = new Label("Films similaires");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        List<Film> tous;
        try { tous = filmService.getAllFilmsSorted(); }
        catch (Exception e) { e.printStackTrace(); return; }

        List<Film> similaires = tous.stream()
                .filter(f -> f.getId() != filmId)
                .filter(f -> f.getGenres() != null && filmActuel.getGenres() != null &&
                        f.getGenres().stream().anyMatch(c ->
                                filmActuel.getGenres().stream()
                                        .anyMatch(fc -> fc.getName().equals(c.getName()))))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        VBox liste = new VBox(10);
        if (similaires.isEmpty()) {
            Label vide = new Label("Aucun film similaire trouvé.");
            vide.setTextFill(Color.web("#aaaaaa"));
            liste.getChildren().add(vide);
        } else {
            for (Film f : similaires) liste.getChildren().add(creerCarteSimiliaire(f));
        }
        panelSimilaires.getChildren().addAll(titre, liste);
    }

    private HBox creerCarteSimiliaire(Film f) {
        HBox carte = new HBox(16);
        carte.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 6;" +
                       "-fx-padding: 10; -fx-cursor: hand;");
        carte.setAlignment(Pos.CENTER_LEFT);

        StackPane imgPane = new StackPane();
        imgPane.setPrefSize(160, 90);
        imgPane.setMinSize(160, 90);
        imgPane.setMaxSize(160, 90);
        imgPane.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 4;");

        if (f.getUrlImageCover() != null && !f.getUrlImageCover().isEmpty()) {
            try {
                Image img = new Image(f.getUrlImageCover(), true);
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

        Label titre = new Label(f.getTitre());
        titre.setTextFill(Color.WHITE);
        titre.setFont(Font.font("System", FontWeight.BOLD, 14));

        String genreText = "-";
        if (f.getGenres() != null && !f.getGenres().isEmpty()) {
            genreText = f.getGenres().stream()
                    .map(Category::getName).limit(2)
                    .reduce((a, b) -> a + " · " + b).orElse("-");
        }
        Label genre = new Label(genreText);
        genre.setTextFill(Color.web("#aaaaaa"));
        genre.setFont(Font.font(12));

        Label duree = new Label(f.getDuree() + " min");
        duree.setTextFill(Color.web("#aaaaaa"));
        duree.setFont(Font.font(12));

        infos.getChildren().addAll(titre, genre, duree);
        carte.getChildren().addAll(imgPane, infos);

        carte.setOnMouseEntered(e -> carte.setStyle(
                "-fx-background-color: #252525; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;"));
        carte.setOnMouseExited(e -> carte.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;"));
        carte.setOnMouseClicked(e -> setFilm(f));

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

        Label formTitle = new Label("Écrire un commentaire");
        formTitle.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px; -fx-font-weight: bold;");

        commentInput = new TextArea();
        commentInput.setPromptText("Partagez votre avis sur ce film...");
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
        btnAnnuler.setOnAction(e -> { commentInput.clear(); spoilerCheck.setSelected(false); });

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
        if (texte.isEmpty() || filmId == -1) return;

        boolean isSpoiler = spoilerCheck != null && spoilerCheck.isSelected();

        Commentaire c = new Commentaire();
        c.setUserId(userId);
        c.setMediaId(filmId);
        c.setContenu(texte);
        c.setSpoiler(isSpoiler);

        boolean ok = commentaireService.ajouterCommentaire(c);
        if (ok) {
            if (commentInput != null) commentInput.clear();
            if (spoilerCheck != null) spoilerCheck.setSelected(false);
            rafraichirListeCommentaires();
            NotificationService notifService = new NotificationService(cnnx);
            Notification n = new Notification(0, userId, "COMMENTAIRE",
                "Commentaire publié",
                "Votre avis a été publié.",
                java.time.LocalDate.now().toString(),
                false, false);
            notifService.addNotification(n);
        } else {
            System.err.println("Impossible d'enregistrer le commentaire.");
        }
    }

    private void rafraichirListeCommentaires() {
        if (commentListContainer == null || filmId == -1) return;
        commentListContainer.getChildren().clear();

        List<Commentaire> commentaires =
                commentaireService.getCommentairesByMedia(filmId, MEDIA_TYPE);

        if (commentaires == null || commentaires.isEmpty()) {
            Label vide = new Label("Soyez le premier à commenter !");
            vide.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");
            commentListContainer.getChildren().add(vide);
            return;
        }
        for (Commentaire c : commentaires)
            commentListContainer.getChildren().add(creerCarteCommentaire(c));
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
        avatarSP.setPrefSize(34, 34); avatarSP.setMinSize(34, 34);
        avatarSP.setStyle("-fx-background-color: #E50914; -fx-background-radius: 17;");
        String initial = (comment.getUsername() != null && !comment.getUsername().isEmpty())
                ? String.valueOf(comment.getUsername().charAt(0)).toUpperCase() : "?";
        Label initLabel = new Label(initial);
        initLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatarSP.getChildren().add(initLabel);

        Label userLabel = new Label(comment.getUsername() != null ? comment.getUsername() : "Anonyme");
        userLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
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
                        ? "Cliquez sur Voir le spoiler pour révéler ce commentaire."
                        : comment.getContenu());
        textLabel.setStyle("-fx-text-fill: " +
                (comment.isSpoiler() && !revealed[0] ? "#555555" : "#cccccc") + "; -fx-font-size: 13px;");
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
                btnReveal.setVisible(false); btnReveal.setManaged(false);
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
                Notification n = new Notification(0, comment.getUserId(), "COMMENTAIRE",
                    "Quelqu'un a aimé votre commentaire",
                    "Votre commentaire sur \"" + (filmActuel != null ? filmActuel.getTitre() : "")
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
        if (filmActuel == null) return;
        FilmPlayerController ctrl = ScreenManager.getInstance()
            .navigateAndGetController(Screen.filmPlayer);
        if (ctrl != null) ctrl.initFilm(filmActuel);
    }

    @FXML private void onFavoris() {
        if (filmId == -1) return;
        Favori fav = new Favori();
        fav.setUserId(userId);
        fav.setMediaId(filmId);

        if (!favoriService.exist(userId, filmId)) {
            favoriService.ajouterFavori(fav);
        } else {
            favoriService.supprimerFavori(userId, filmId);
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
    @FXML private void onTabBandes()       { activerOnglet(tabBandes); }
    @FXML private void onTabCommentaires() { activerOnglet(tabCommentaires); }
    @FXML private void onTabSimilaires()   { activerOnglet(tabSimilaires); chargerSimilaires(); }

    private void activerOnglet(VBox ongletActif) {
        VBox[] onglets = {tabApropos, tabBandes, tabCommentaires, tabSimilaires};
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

        panelApropos.setVisible(false);      panelApropos.setManaged(false);
        panelBandes.setVisible(false);       panelBandes.setManaged(false);
        panelSimilaires.setVisible(false);   panelSimilaires.setManaged(false);
        panelCommentaires.setVisible(false); panelCommentaires.setManaged(false);

        if      (ongletActif == tabApropos)      { panelApropos.setVisible(true);      panelApropos.setManaged(true); }
        else if (ongletActif == tabBandes)       { panelBandes.setVisible(true);       panelBandes.setManaged(true); }
        else if (ongletActif == tabSimilaires)   { panelSimilaires.setVisible(true);   panelSimilaires.setManaged(true); }
        else if (ongletActif == tabCommentaires) { panelCommentaires.setVisible(true); panelCommentaires.setManaged(true); }
    }

    @FXML
    private void onNoter() {
        if (btnNoter == null || filmId == -1) return;
        if (ratingService.hasRated(userId, filmId)) {
            btnNoter.setText("⭐ Déjà noté");
            btnNoter.setDisable(true);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Déjà noté");
            alert.setHeaderText(null);
            alert.setContentText("Vous avez déjà noté ce film.");
            alert.showAndWait();
            return;
        }

        Stage popup = new Stage(StageStyle.UNDECORATED);
        popup.initModality(Modality.APPLICATION_MODAL);

        final int[] selectedScore = {0};
        Label[] stars = new Label[5];

        Label titre = new Label("Notez ce film");
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

            Rating rating = new Rating(userId, filmId, selectedScore[0]);
            boolean ok = ratingService.addRating(rating);

            if (ok) {
                double moyenne = ratingService.getFilmAverage(filmId);
                btnNoter.setText("⭐ " + moyenne + "/5");
                btnNoter.setDisable(true);
            }

            popup.close();

            Alert alert = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(ok ? "Merci !" : "Erreur");
            alert.setHeaderText(null);
            alert.setContentText(ok
                    ? "Note de " + selectedScore[0] + " étoile(s) enregistrée !\nMoyenne actuelle : "
                      + ratingService.getFilmAverage(filmId) + "/5"
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