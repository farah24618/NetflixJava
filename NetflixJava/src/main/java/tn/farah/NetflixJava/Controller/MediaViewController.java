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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Notification;
import tn.farah.NetflixJava.Entities.Rating;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Warning;
import tn.farah.NetflixJava.Service.CommentaireService;
import tn.farah.NetflixJava.Service.FavoriService;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.Service.NotificationService;
import tn.farah.NetflixJava.Service.RatingService;
import tn.farah.NetflixJava.Service.SaisonService;
import tn.farah.NetflixJava.Service.SerieService;
import tn.farah.NetflixJava.Service.UserService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;
import tn.farah.NetflixJava.utils.SessionManager;


public class MediaViewController implements Initializable {

   
    private FilmService        filmService;
    private SerieService       serieService;
    private SaisonService      saisonService;
    private CommentaireService commentaireService;
    private FavoriService      favoriService;
    private UserService        userService;
    private RatingService      ratingService;
    private Connection         cnnx;


    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    
    private boolean modeSerieActif = false;
    @FXML private HBox      profilBox;
    @FXML private Label     labelUserName;
    @FXML private StackPane avatarContainer;
    @FXML private ImageView avatarImage;

    @FXML private StackPane videoPane;
    @FXML private ImageView posterImage;
    @FXML private Label     mediaTitleVideo; 
    @FXML private Label     posterDesc;
    @FXML private Button    btnLire;
    @FXML private Button    btnFavoris;
    @FXML private Button    btnNoter;
    @FXML private Label     categories;

    @FXML private HBox  breadcrumbBox;
    @FXML private Label labelTitreSerie;
    @FXML private Label labelNumeroSaison;
    @FXML private Label labelNumeroEpisode;
    @FXML private Label mediaTitle;            
    @FXML private Label labelDuree;
    @FXML private Label labelDateSortie;
    @FXML private Label mediaDesc;            
    @FXML private Label episodeDesc;          
    @FXML private Label castings;
    @FXML private Label labelWarnings;

    @FXML private VBox tabApropos;
    @FXML private VBox tabEpisodes;          
    @FXML private VBox tabBandes;
    @FXML private VBox tabCommentaires;
    @FXML private VBox tabSimilaires;

    
    @FXML private VBox panelApropos;
    @FXML private VBox panelEpisodes;
    @FXML private VBox panelBandes;
    @FXML private VBox panelCommentaires;
    @FXML private VBox panelSimilaires;
    @FXML private HBox listeSimilaires;

   
    @FXML private Label labelCreateurTitle;    
    @FXML private Label aproposPanelTitle;    
    @FXML private Label labelSynopsis;
    @FXML private Label labelGenre;
    @FXML private Label labelAnnee;
    @FXML private Label labelProducteur;
    @FXML private Label labelWarningsApropos;

    @FXML private ComboBox<String> saisonComboBox;
    @FXML private Label            labelNbEpisodes;
    @FXML private VBox             listeEpisodes;

    private final int userId = SessionManager.getInstance().getCurrentUserId();

    private int  filmId     = -1;
    private Film filmActuel = null;

    private int           serieId         = -1;
    private Serie         serieActuelle   = null;
    private int           currentSaisonId = -1;
    private List<Episode> episodesDB;
    private Episode       episodeActuel   = null;

    private VBox     commentListContainer;
    private TextArea commentInput;
    private CheckBox spoilerCheck;

   
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cnnx               = ConxDB.getInstance();
        filmService        = new FilmService(cnnx);
        serieService       = new SerieService(cnnx);
        saisonService      = new SaisonService(cnnx);
        commentaireService = new CommentaireService(cnnx);
        favoriService      = new FavoriService(cnnx);
        userService        = new UserService(cnnx);
        ratingService      = new RatingService(cnnx);

        String pseudo = "Inconnu";
        if (userService.findUserById(userId) != null)
            pseudo = userService.findUserById(userId).getPseudo();
        labelUserName.setText(pseudo);

        
        construireInterfaceCommentaires();

        
        activerOnglet(tabApropos);
    }

    
    public void setFilm(Film film) {
        modeSerieActif = false;
        this.filmId    = film.getId();
        this.filmActuel = film;

       
        setSerieUIVisible(false);

        if (aproposPanelTitle  != null) aproposPanelTitle.setText("À propos du film");
        if (labelCreateurTitle != null) labelCreateurTitle.setText("RÉALISATEUR");

        chargerInfosFilm(film);
        mettreAJourBtnFavoris(filmId);
        rafraichirListeCommentaires("film");
        initialiserBtnNoter();
        activerOnglet(tabApropos);
    }

    public void setSerie(Serie serie) {
        modeSerieActif  = true;
        this.serieActuelle = serie;
        this.serieId       = serie.getId();

        setSerieUIVisible(true);

        if (aproposPanelTitle  != null) aproposPanelTitle.setText("À propos de la série");
        if (labelCreateurTitle != null) labelCreateurTitle.setText("CRÉATEUR");

        chargerInfosSerie(serieId);
        chargerSaisonsCombo(serie);
        mettreAJourBtnFavoris(serieId);
        rafraichirListeCommentaires("serie");
        initialiserBtnNoter();
        activerOnglet(tabEpisodes);
    }

    private void setSerieUIVisible(boolean visible) {
        if (breadcrumbBox != null) {
            breadcrumbBox.setVisible(visible);
            breadcrumbBox.setManaged(visible);
        }
        if (tabEpisodes != null) {
            tabEpisodes.setVisible(visible);
            tabEpisodes.setManaged(visible);
        }
    }

    private void chargerInfosFilm(Film film) {
        if (film == null) return;

        set(mediaTitleVideo, film.getTitre());
        set(mediaTitle,      film.getTitre());

        if (posterDesc != null && film.getSynopsis() != null) {
            String syn = film.getSynopsis();
            posterDesc.setText(syn.length() > 160 ? syn.substring(0, 157) + "..." : syn);
        }
        if (posterImage != null && notBlank(film.getUrlImageBanner())) {
            loadImage(posterImage, film.getUrlImageBanner());
        }

        set(labelDuree, film.getDuree() + " min");
        if (labelDateSortie != null && film.getDateSortie() != null)
            labelDateSortie.setText(String.valueOf(film.getDateSortie().getYear()));
        set(mediaDesc, film.getSynopsis());

        String genreText = genresText(film.getGenres());
        set(categories,  genreText);
        set(labelGenre,  genreText);

        String warnText = warningsText(film.getWarnings());
        set(labelWarnings,       warnText);
        set(labelWarningsApropos, warnText);

        String cast = film.getCasting();
        set(castings, cast != null && !cast.isBlank() ? cast : "Non renseigné");

        set(labelSynopsis,   film.getSynopsis());
        set(labelProducteur, film.getProducteur() != null ? film.getProducteur() : "N/A");
        if (labelAnnee != null && film.getDateSortie() != null)
            labelAnnee.setText(String.valueOf(film.getDateSortie().getYear()));
    }

    private void chargerInfosSerie(int sId) {
        if (sId == -1) return;
        Serie s = serieService.findById(sId);
        if (s == null) return;

        set(labelTitreSerie, s.getTitre());
        set(mediaTitleVideo, s.getTitre());
        set(mediaTitle,      s.getTitre());

        if (posterDesc != null && s.getSynopsis() != null) {
            String syn = s.getSynopsis();
            posterDesc.setText(syn.length() > 160 ? syn.substring(0, 157) + "..." : syn);
        }
        if (posterImage != null && notBlank(s.getUrlImageBanner()))
            loadImage(posterImage, s.getUrlImageBanner());

        set(mediaDesc, s.getSynopsis());

        String genreText = genresText(s.getGenres());
        set(categories,  genreText);
        set(labelGenre,  genreText);

        if (labelAnnee != null && s.getDateSortie() != null)
            labelAnnee.setText(String.valueOf(s.getDateSortie().getYear()));
        set(labelProducteur, s.getProducteur() != null ? s.getProducteur() : "N/A");

        String cast = s.getCasting();
        set(castings, cast != null && !cast.isBlank() ? cast : "Non renseigné");

        String warnText = warningsText(s.getWarnings());
        set(labelWarnings,        warnText);
        set(labelWarningsApropos, warnText);

        set(labelSynopsis, s.getSynopsis());
    }

    private void chargerSaisonsCombo(Serie serie) {
        List<Saison> saisons = saisonService.findBySerie(serie.getId());
        if (saisons == null || saisons.isEmpty()) return;

        saisonComboBox.getItems().clear();

        saisonComboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #2a2a2a;");
            }
        });
        saisonComboBox.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #2a2a2a;");
            }
        });

        for (Saison s : saisons)
            saisonComboBox.getItems().add("Saison " + s.getNumeroSaison());

        saisonComboBox.setValue("Saison " + saisons.get(0).getNumeroSaison());
        this.currentSaisonId = saisons.get(0).getId();
        this.episodesDB = serieService.findEpisodeBySaison(currentSaisonId);
        chargerListeEpisodes();
        mettreAJourNbEpisodes();

        if (episodesDB != null && !episodesDB.isEmpty())
            mettreAJourInfosEpisode(episodesDB.get(0));

        saisonComboBox.setOnAction(e -> {
            int idx = saisonComboBox.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                Saison choix = saisons.get(idx);
                currentSaisonId = choix.getId();
                episodesDB = serieService.findEpisodeBySaison(currentSaisonId);
                chargerListeEpisodes();
                mettreAJourNbEpisodes();
                if (episodesDB != null && !episodesDB.isEmpty())
                    mettreAJourInfosEpisode(episodesDB.get(0));
            }
        });
    }

    private void mettreAJourNbEpisodes() {
        int nb = episodesDB != null ? episodesDB.size() : 0;
        if (labelNbEpisodes != null)
            labelNbEpisodes.setText(nb + " épisode" + (nb > 1 ? "s" : ""));
    }

    private void mettreAJourInfosEpisode(Episode ep) {
        episodeActuel = ep;

        String numero = String.valueOf(ep.getNumeroEpisode());
        Saison saison = null;
        try { saison = saisonService.getSaisonbyEpisodeId(ep.getId()); }
        catch (Exception e) { e.printStackTrace(); }

        String saisonNbr = saison != null ? String.valueOf(saison.getNumeroSaison()) : "?";

        set(labelNumeroSaison,  saisonNbr);
        set(labelNumeroEpisode, numero);
        set(mediaTitle,         ep.getTitre());
        set(labelDuree,         ep.getDuree() + " min");
        set(labelDateSortie,    "");
        set(episodeDesc,        ep.getResume() != null ? ep.getResume() : "");
        set(mediaDesc,          ep.getResume() != null ? ep.getResume() : "");

        if (mediaTitleVideo != null)
            mediaTitleVideo.setText("S0" + saisonNbr + " E"
                + String.format("%02d", ep.getNumeroEpisode()) + " - " + ep.getTitre());

        if (posterDesc != null) {
            String r = ep.getResume() != null ? ep.getResume() : "";
            posterDesc.setText(r.length() > 160 ? r.substring(0, 157) + "..." : r);
        }

        if (posterImage != null && ep.getMiniatureUrl() != null && !ep.getMiniatureUrl().isBlank()) {
            try {
                InputStream is = getClass().getResourceAsStream(ep.getMiniatureUrl());
                if (is != null) posterImage.setImage(new Image(is));
            } catch (Exception ignored) {}
        }
    }

    private void chargerListeEpisodes() {
        listeEpisodes.getChildren().clear();
        if (episodesDB == null || episodesDB.isEmpty()) {
            Label vide = new Label("Aucun épisode disponible.");
            vide.getStyleClass().add("empty-label-lg");
            listeEpisodes.getChildren().add(vide);
            return;
        }
        for (Episode ep : episodesDB) {
            boolean actuel = episodeActuel != null
                    ? ep.getId() == episodeActuel.getId()
                    : ep.getId() == episodesDB.get(0).getId();
            listeEpisodes.getChildren().add(creerCarteEpisode(ep, actuel));
        }
    }

    private void rafraichirListeEpisodes(Episode epSelectionne) {
        listeEpisodes.getChildren().clear();
        for (Episode ep : episodesDB) {
            boolean actuel = ep.getId() == epSelectionne.getId();
            listeEpisodes.getChildren().add(creerCarteEpisode(ep, actuel));
        }
    }

    private HBox creerCarteEpisode(Episode ep, boolean estActuel) {
        HBox carte = new HBox(16);
        carte.getStyleClass().add(estActuel ? "episode-card-active" : "episode-card");

        StackPane miniature = new StackPane();
        miniature.setPrefSize(180, 100);
        miniature.setMinSize(180, 100);
        if (ep.getMiniatureUrl() != null && !ep.getMiniatureUrl().isEmpty()) {
            try {
                File f = new File(ep.getMiniatureUrl());
                Image img;
                if (f.exists()) {
                    img = new Image(f.toURI().toString());
                } else {
                    InputStream is = getClass().getResourceAsStream(ep.getMiniatureUrl());
                    img = is != null ? new Image(is) : null;
                }
                if (img != null && !img.isError()) {
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(180); iv.setFitHeight(100); iv.setPreserveRatio(false);
                    miniature.getChildren().add(iv);
                } else {
                    miniature.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 4;");
                }
            } catch (Exception e) {
                miniature.setStyle("-fx-background-color: #2e2e2e; -fx-background-radius: 4;");
            }
        }

        Label numLabel = new Label(String.valueOf(ep.getNumeroEpisode()));
        numLabel.getStyleClass().add(estActuel ? "episode-num-label-active" : "episode-num-label");
        Label playIcon = new Label(">");
        playIcon.setTextFill(Color.WHITE);
        playIcon.setFont(Font.font(28));
        playIcon.setOpacity(0);
        miniature.getChildren().addAll(numLabel, playIcon);

        carte.setOnMouseEntered(e -> {
            playIcon.setOpacity(0.8);
            numLabel.setOpacity(0);
            if (!estActuel) carte.setStyle("-fx-background-color: #252525;" +
                    "-fx-background-radius: 6; -fx-padding: 14; -fx-cursor: hand;");
        });
        carte.setOnMouseExited(e -> {
            playIcon.setOpacity(0);
            numLabel.setOpacity(1);
            if (!estActuel) carte.setStyle("-fx-background-color: #1a1a1a;" +
                    "-fx-background-radius: 6; -fx-padding: 14; -fx-cursor: hand;");
        });

        // Infos
        VBox infos = new VBox(6);
        HBox.setHgrow(infos, Priority.ALWAYS);

        HBox ligneTitre = new HBox(12);
        ligneTitre.setAlignment(Pos.CENTER_LEFT);
        Label titreLabel = new Label("Episode " + ep.getNumeroEpisode() + " - " + ep.getTitre());
        titreLabel.getStyleClass().add(estActuel ? "episode-title-label-active" : "episode-title-label");
        HBox.setHgrow(titreLabel, Priority.ALWAYS);
        Label dureeLabel = new Label(ep.getDuree() + " min");
        dureeLabel.getStyleClass().add("episode-duration-label");
        ligneTitre.getChildren().addAll(titreLabel, dureeLabel);

        Label descLabel = new Label(ep.getResume() != null ? ep.getResume() : "");
        descLabel.getStyleClass().add("episode-desc-label");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(600);

        if (estActuel) {
            Label badge = new Label("En cours");
            badge.getStyleClass().add("episode-badge-encours");
            infos.getChildren().addAll(ligneTitre, badge, descLabel);
        } else {
            infos.getChildren().addAll(ligneTitre, descLabel);
        }

        // Bouton Lire
        Button btnLireLocal = new Button(">  Lire");
        btnLireLocal.getStyleClass().addAll("btn-lire-episode",
                estActuel ? "btn-lire-episode-active" : "btn-lire-episode-inactive");
        btnLireLocal.setOnAction(e -> {
            e.consume();
            mettreAJourInfosEpisode(ep);
            rafraichirListeEpisodes(ep);
            ouvrirEpisode(ep);
        });

        carte.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) return;
            mettreAJourInfosEpisode(ep);
            rafraichirListeEpisodes(ep);
        });

        carte.getChildren().addAll(miniature, infos, btnLireLocal);
        return carte;
    }

   
    private void mettreAJourBtnFavoris(int mediaId) {
        if (btnFavoris == null || mediaId == -1) return;
        boolean deja = favoriService.exist(userId, mediaId);
        if (deja) {
            btnFavoris.setText("✔  Dans ma liste");
            btnFavoris.getStyleClass().removeAll("btn-favoris");
            btnFavoris.getStyleClass().add("btn-favoris-added");
        } else {
            btnFavoris.setText("＋  Ajouter aux favoris");
            btnFavoris.getStyleClass().removeAll("btn-favoris-added");
            if (!btnFavoris.getStyleClass().contains("btn-favoris"))
                btnFavoris.getStyleClass().add("btn-favoris");
        }
    }
    private void initialiserBtnNoter() {
        if (btnNoter == null || ratingService == null) return;
        int mediaId = modeSerieActif ? serieId : filmId;
        if (mediaId == -1) return;
        if (ratingService.hasRated(userId, mediaId)) {
            double moy = ratingService.getFilmAverage(mediaId);
            btnNoter.setText("⭐ " + moy + "/5");
            btnNoter.setDisable(true);
        } else {
            btnNoter.setText("Noter");
            btnNoter.setDisable(false);
        }
    }

    
    private void chargerSimilaires() {
        if (panelSimilaires == null) return;
        panelSimilaires.getChildren().clear();

        if (modeSerieActif) {
            chargerSimilairesSerie();
        } else {
            chargerSimilairesFilm();
        }
    }

    private void chargerSimilairesFilm() {
        if (filmId == -1 || filmActuel == null) return;
        Label titre = new Label("Films similaires");
        titre.getStyleClass().add("similaires-title");

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
            vide.getStyleClass().add("empty-label-lg");
            liste.getChildren().add(vide);
        } else {
            for (Film f : similaires) liste.getChildren().add(creerCarteFilmSimilaire(f));
        }
        panelSimilaires.getChildren().addAll(titre, liste);
    }

    private HBox creerCarteFilmSimilaire(Film f) {
        HBox carte = new HBox(16);
        carte.getStyleClass().add("similaire-card");
        carte.setAlignment(Pos.CENTER_LEFT);

        StackPane imgPane = creerImagePane(f.getUrlImageBanner());

        VBox infos = new VBox(6);
        HBox.setHgrow(infos, Priority.ALWAYS);
        Label titre = new Label(f.getTitre());
        titre.getStyleClass().add("similaire-title");
        Label genre = new Label(genresText(f.getGenres()));
        genre.getStyleClass().add("similaire-genre");
        Label duree = new Label(f.getDuree() + " min");
        duree.getStyleClass().add("similaire-meta");
        infos.getChildren().addAll(titre, genre, duree);
        carte.getChildren().addAll(imgPane, infos);

        carte.setOnMouseEntered(e -> carte.setStyle(
                "-fx-background-color: #252525; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;"));
        carte.setOnMouseExited(e -> carte.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;"));
        carte.setOnMouseClicked(e -> setFilm(f));
        return carte;
    }

    private void chargerSimilairesSerie() {
        if (serieId == -1 || serieActuelle == null) return;
        Label titre = new Label("Séries similaires");
        titre.getStyleClass().add("similaires-title");

        List<Serie> similaires = serieService.getAllSeries().stream()
                .filter(s -> s.getId() != serieId)
                .filter(s -> s.getGenres() != null && serieActuelle.getGenres() != null &&
                        s.getGenres().stream().anyMatch(c ->
                                serieActuelle.getGenres().stream()
                                        .anyMatch(sc -> sc.getName().equals(c.getName()))))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        VBox liste = new VBox(10);
        if (similaires.isEmpty()) {
            Label vide = new Label("Aucune série similaire trouvée.");
            vide.getStyleClass().add("empty-label-lg");
            liste.getChildren().add(vide);
        } else {
            for (Serie s : similaires) liste.getChildren().add(creerCarteSerieSimilaire(s));
        }
        panelSimilaires.getChildren().addAll(titre, liste);
    }

    private HBox creerCarteSerieSimilaire(Serie s) {
        HBox carte = new HBox(16);
        carte.getStyleClass().add("similaire-card");
        carte.setAlignment(Pos.CENTER_LEFT);

        StackPane imgPane = creerImagePane(s.getUrlImageBanner());

        VBox infos = new VBox(6);
        HBox.setHgrow(infos, Priority.ALWAYS);
        Label titre = new Label(s.getTitre());
        titre.getStyleClass().add("similaire-title");
        Label genre = new Label(genresText(s.getGenres()));
        genre.getStyleClass().add("similaire-genre");
        Label statut = new Label(s.isTerminee() ? "Terminée" : "En cours");
        statut.getStyleClass().add(s.isTerminee() ? "similaire-statut-termine" : "similaire-statut-encours");
        infos.getChildren().addAll(titre, genre, statut);
        carte.getChildren().addAll(imgPane, infos);

        carte.setOnMouseEntered(e -> carte.setStyle(
                "-fx-background-color: #252525; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;"));
        carte.setOnMouseExited(e -> carte.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 6; -fx-padding: 10; -fx-cursor: hand;"));
        carte.setOnMouseClicked(e -> setSerie(s));
        return carte;
    }

    
    private void construireInterfaceCommentaires() {
        panelCommentaires.getChildren().clear();
        panelCommentaires.setSpacing(20);

        Label titre = new Label("Commentaires");
        titre.getStyleClass().add("commentaires-title");

        // Formulaire
        VBox formulaire = new VBox(12);
        formulaire.getStyleClass().add("comment-form");

        Label formTitle = new Label("Écrire un commentaire");
        formTitle.getStyleClass().add("comment-form-title");

        commentInput = new TextArea();
        commentInput.setPromptText("Partagez votre avis...");
        commentInput.setPrefRowCount(3);
        commentInput.setWrapText(true);
        commentInput.getStyleClass().add("comment-input");

        spoilerCheck = new CheckBox("  Ce commentaire contient un spoiler");
        spoilerCheck.getStyleClass().add("spoiler-check");

        HBox formActions = new HBox(12);
        formActions.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnnuler = new Button("Annuler");
        btnAnnuler.getStyleClass().add("btn-annuler");
        btnAnnuler.setOnAction(e -> {
            commentInput.clear();
            spoilerCheck.setSelected(false);
        });

        Button btnPublier = new Button("Publier");
        btnPublier.getStyleClass().add("btn-publier");
        btnPublier.setOnAction(e -> publierCommentaire());

        formActions.getChildren().addAll(btnAnnuler, btnPublier);
        formulaire.getChildren().addAll(formTitle, commentInput, spoilerCheck, formActions);

        commentListContainer = new VBox(12);
        panelCommentaires.getChildren().addAll(titre, formulaire, commentListContainer);
    }

    private void publierCommentaire() {
        String texte = commentInput != null ? commentInput.getText().trim() : "";
        boolean isSpoiler = spoilerCheck != null && spoilerCheck.isSelected();

        int mediaId   = modeSerieActif ? serieId : filmId;
        String mtype  = modeSerieActif ? "serie"  : "film";
        if (texte.isEmpty() || mediaId == -1) return;

        Commentaire c = new Commentaire();
        c.setUserId(userId);
        c.setMediaId(mediaId);
        c.setContenu(texte);
        c.setSpoiler(isSpoiler);

        boolean ok = commentaireService.ajouterCommentaire(c);
        if (ok) {
            if (commentInput  != null) commentInput.clear();
            if (spoilerCheck  != null) spoilerCheck.setSelected(false);
            rafraichirListeCommentaires(mtype);
          
            NotificationService ns = new NotificationService(cnnx);
            Notification n = new Notification(0, userId, "COMMENTAIRE",
                    "Commentaire publié", "Votre avis a été publié.",
                    java.time.LocalDate.now().toString(), false, false);
            ns.addNotification(n);
        } else {
            System.err.println("Impossible d'enregistrer le commentaire.");
        }
    }

    private void rafraichirListeCommentaires(String mediaType) {
        if (commentListContainer == null) return;
        commentListContainer.getChildren().clear();

        int mediaId = modeSerieActif ? serieId : filmId;
        if (mediaId == -1) return;

        List<Commentaire> commentaires =
                commentaireService.getCommentairesByMedia(mediaId, mediaType);

        if (commentaires == null || commentaires.isEmpty()) {
            Label vide = new Label("Soyez le premier à commenter !");
            vide.getStyleClass().add("empty-label");
            commentListContainer.getChildren().add(vide);
            return;
        }

        for (Commentaire c : commentaires)
            commentListContainer.getChildren().add(creerCarteCommentaire(c, mediaType));
    }

    private VBox creerCarteCommentaire(Commentaire comment, String mediaType) {
        VBox carte = new VBox(8);
        carte.getStyleClass().add("comment-card");

       
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        if (userService.findUserById(comment.getUserId()) != null)
            comment.setUsername(userService.findUserById(comment.getUserId()).getPseudo());

        StackPane avatarSP = new StackPane();
        avatarSP.setPrefSize(34, 34); avatarSP.setMinSize(34, 34);
        avatarSP.getStyleClass().add("comment-avatar");
        String initial = (comment.getUsername() != null && !comment.getUsername().isEmpty())
                ? String.valueOf(comment.getUsername().charAt(0)).toUpperCase() : "?";
        Label initLabel = new Label(initial);
        initLabel.getStyleClass().add("comment-avatar-label");
        avatarSP.getChildren().add(initLabel);

        Label userLabel = new Label(comment.getUsername() != null ? comment.getUsername() : "Anonyme");
        userLabel.getStyleClass().add("comment-username");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(avatarSP, userLabel, spacer);

        if (comment.getDateCommentaire() != null) {
            Label dateLabel = new Label(comment.getDateCommentaire().format(DATE_FMT));
            dateLabel.getStyleClass().add("comment-date");
            header.getChildren().add(dateLabel);
        }
        if (comment.isSpoiler()) {
            Label spoilerBadge = new Label("SPOILER");
            spoilerBadge.getStyleClass().add("spoiler-badge");
            header.getChildren().add(spoilerBadge);
        }

        final boolean[] revealed = {!comment.isSpoiler()};
        Label textLabel = new Label(comment.isSpoiler() && !revealed[0]
                ? "Cliquez sur Voir le spoiler pour révéler ce commentaire."
                : comment.getContenu());
        textLabel.getStyleClass().add(
                comment.isSpoiler() && !revealed[0] ? "comment-text-hidden" : "comment-text");
        textLabel.setWrapText(true);

        if (comment.isSpoiler()) {
            Button btnReveal = new Button("Voir le spoiler");
            btnReveal.getStyleClass().add("btn-reveal");
            btnReveal.setOnAction(e -> {
                revealed[0] = true;
                textLabel.setText(comment.getContenu());
                textLabel.getStyleClass().setAll("comment-text");
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
        btnLikeC.getStyleClass().add("btn-like-comment");
        btnLikeC.setOnAction(e -> {
            commentaireService.ajouterLike(comment.getId());
            likesCount[0]++;
            btnLikeC.setText("J'aime (" + likesCount[0] + ")");
            btnLikeC.getStyleClass().setAll("btn-like-comment-active");
            btnLikeC.setDisable(true);

            if (cnnx != null && comment.getUserId() != userId) {
                String titre = modeSerieActif
                        ? (serieActuelle != null ? serieActuelle.getTitre() : "")
                        : (filmActuel   != null ? filmActuel.getTitre()    : "");
                NotificationService ns = new NotificationService(cnnx);
                ns.addNotification(new Notification(0, comment.getUserId(), "COMMENTAIRE",
                        "Quelqu'un a aimé votre commentaire",
                        "Votre commentaire sur \"" + titre + "\" a reçu un J'aime.",
                        java.time.LocalDate.now().toString(), false, false));
            }
        });

        Button btnReport = new Button("Signaler");
        btnReport.getStyleClass().add("btn-report");
        btnReport.setOnAction(e -> {
            comment.setSignale(true);
            boolean ok = commentaireService.update(comment);
            if (ok) {
                btnReport.setText("Signalé");
                btnReport.getStyleClass().setAll("btn-report-done");
                btnReport.setDisable(true);
                if (cnnx != null) {
                    String titreSerie = modeSerieActif
                            ? (serieActuelle != null ? serieActuelle.getTitre() : "Inconnue")
                            : (filmActuel   != null ? filmActuel.getTitre()    : "Inconnu");
                    NotificationService ns = new NotificationService(cnnx);
                    ns.addNotification(new Notification(0, 1, "SIGNALEMENT",
                            "Le commentaire #" + comment.getId(),
                            "a été signalé sur : " + titreSerie,
                            java.time.LocalDate.now().toString(), false, false));
                }
            } else {
                System.err.println("Impossible de signaler le commentaire.");
            }
        });

        actions.getChildren().addAll(btnLikeC, btnReport);

        if (comment.getUserId() == userId) {
            Button btnSupprimer = new Button("🗑 Supprimer");
            btnSupprimer.getStyleClass().add("btn-delete-comment");
            btnSupprimer.setOnAction(e -> {
                boolean ok = commentaireService.delet(comment.getId());
                if (ok) {
                    rafraichirListeCommentaires(mediaType);
                } else {
                    System.err.println("Impossible de supprimer le commentaire.");
                }
            });
            actions.getChildren().add(btnSupprimer);
        }

        carte.getChildren().add(actions);
        return carte;
    }

  
    private void chargerBandesAnnonces() {
        if (panelBandes == null) return;
        panelBandes.getChildren().clear();

        Label titre = new Label("Bandes-annonces & Teasers");
        titre.getStyleClass().add("bandes-title");

        String urlTeaser = modeSerieActif
                ? (serieActuelle != null ? serieActuelle.getUrlTeaser() : null)
                : (filmActuel   != null ? filmActuel.getUrlTeaser()    : null);
        String nomMedia = modeSerieActif
                ? (serieActuelle != null ? serieActuelle.getTitre() : "")
                : (filmActuel   != null ? filmActuel.getTitre()    : "");

        if (urlTeaser == null || urlTeaser.isBlank()) {
            Label vide = new Label("Aucune bande-annonce disponible.");
            vide.getStyleClass().add("empty-label");
            panelBandes.getChildren().addAll(titre, vide);
            return;
        }

        StackPane carte = new StackPane();
        carte.setPrefSize(280, 158);
        carte.getStyleClass().add("teaser-card");

        Label playIcon = new Label("▶");
        playIcon.setStyle("-fx-text-fill: white; -fx-font-size: 36px;");

        Label labelTitre = new Label("Teaser — " + nomMedia);
        labelTitre.getStyleClass().add("teaser-label");
        StackPane.setAlignment(labelTitre, Pos.BOTTOM_LEFT);

        carte.getChildren().addAll(playIcon, labelTitre);

        carte.setOnMouseEntered(e -> carte.setStyle(
                "-fx-background-color: #2a2a2a; -fx-background-radius: 6; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(229,9,20,0.4), 12, 0, 0, 0);"));
        carte.setOnMouseExited(e -> carte.setStyle(
                "-fx-background-color: #1a1a1a; -fx-background-radius: 6; -fx-cursor: hand;"));

        final String url = urlTeaser;
        final String nom = nomMedia;
        carte.setOnMouseClicked(e -> {
            UniversalPlayerController ctrl = ScreenManager.getInstance()
                    .navigateAndGetController(Screen.Player);
            if (ctrl != null) ctrl.initTeaser(url, nom);
        });

        panelBandes.getChildren().addAll(titre, carte);
    }
    @FXML private void onLire() {
        if (modeSerieActif) {
            if (episodeActuel == null) return;
            ouvrirEpisode(episodeActuel);
        } else {
            if (filmActuel == null) return;
            UniversalPlayerController ctrl = ScreenManager.getInstance()
                    .navigateAndGetController(Screen.Player);
            if (ctrl != null) ctrl.initFilm(filmActuel, userId);
        }
    }

    @FXML private void onFavoris() {
        int mediaId = modeSerieActif ? serieId : filmId;
        if (mediaId == -1) return;
        Favori fav = new Favori();
        fav.setUserId(userId);
        fav.setMediaId(mediaId);
        if (!favoriService.exist(userId, mediaId)) {
            favoriService.ajouterFavori(fav);
        } else {
            favoriService.supprimerFavori(userId, mediaId);
        }
        mettreAJourBtnFavoris(mediaId);
    }

    @FXML private void onNoter() {
        int mediaId = modeSerieActif ? serieId : filmId;
        if (mediaId == -1 || ratingService == null) return;

        if (ratingService.hasRated(userId, mediaId)) {
            btnNoter.setText("⭐ Déjà noté");
            btnNoter.setDisable(true);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Déjà noté");
            alert.setHeaderText(null);
            alert.setContentText("Vous avez déjà noté ce " + (modeSerieActif ? "série" : "film") + ".");
            alert.showAndWait();
            return;
        }

        Stage popup = new Stage(StageStyle.UNDECORATED);
        popup.initModality(Modality.APPLICATION_MODAL);

        final int[] selectedScore = {0};
        Label[] stars = new Label[5];

        final String STAR_ON  = "-fx-font-size: 36px; -fx-text-fill: #FFD700; -fx-cursor: hand;";
        final String STAR_OFF = "-fx-font-size: 36px; -fx-text-fill: #555555; -fx-cursor: hand;";

        Label titre = new Label("Notez ce " + (modeSerieActif ? "série" : "film"));
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
            star.setStyle(STAR_OFF);

            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle(j < idx ? STAR_ON : STAR_OFF);
            });
            starBox.setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle(j < selectedScore[0] ? STAR_ON : STAR_OFF);
            });
            star.setOnMouseClicked(e -> {
                selectedScore[0] = idx;
                lblScore.setText(idx + " étoile" + (idx > 1 ? "s" : ""));
                lblScore.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 13px;");
                for (int j = 0; j < 5; j++)
                    stars[j].setStyle(j < idx ? STAR_ON : STAR_OFF);
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
            Rating rating = new Rating(userId, mediaId, selectedScore[0]);
            boolean ok = ratingService.addRating(rating);

            if (ok) {
                double moy = ratingService.getFilmAverage(mediaId);
                btnNoter.setText("⭐ " + moy + "/5");
                btnNoter.setDisable(true);
                if (modeSerieActif && serieActuelle != null) {
                    serieActuelle.setRatingMoyen(moy);
                    try {
						serieService.updateSerie(serieActuelle);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                } else if (filmActuel != null) {
                    filmActuel.setRatingMoyen(moy);
                    try { filmService.update(filmActuel); }
                    catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
            popup.close();

            Alert alert = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(ok ? "Merci !" : "Erreur");
            alert.setHeaderText(null);
            alert.setContentText(ok
                    ? "Note de " + selectedScore[0] + " étoile(s) enregistrée !\nMoyenne : "
                      + ratingService.getFilmAverage(mediaId) + "/5"
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
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);
        popup.showAndWait();
    }

    @FXML private void onTabApropos()      { activerOnglet(tabApropos); }
    @FXML private void onTabEpisodes()     { activerOnglet(tabEpisodes); }
    @FXML private void onTabBandes()       { activerOnglet(tabBandes);  chargerBandesAnnonces(); }
    @FXML private void onTabCommentaires() { activerOnglet(tabCommentaires); }
    @FXML private void onTabSimilaires()   { activerOnglet(tabSimilaires); chargerSimilaires(); }

    private void activerOnglet(VBox ongletActif) {

        VBox[] onglets = modeSerieActif
                ? new VBox[]{tabApropos, tabEpisodes, tabBandes, tabCommentaires, tabSimilaires}
                : new VBox[]{tabApropos, tabBandes, tabCommentaires, tabSimilaires};

        for (VBox onglet : onglets) {
            if (onglet == null) continue;
            Label label      = (Label) onglet.getChildren().get(0);
            HBox  indicateur = (HBox)  onglet.getChildren().get(1);
            if (onglet == ongletActif) {
                label.getStyleClass().setAll("tab-label-active");
                indicateur.getStyleClass().setAll("tab-indicator-active");
                indicateur.setPrefWidth(Region.USE_COMPUTED_SIZE);
            } else {
                label.getStyleClass().setAll("tab-label-inactive");
                indicateur.getStyleClass().setAll("tab-indicator-inactive");
                indicateur.setPrefWidth(0);
            }
        }

        setPanel(panelApropos,       false);
        setPanel(panelEpisodes,      false);
        setPanel(panelBandes,        false);
        setPanel(panelSimilaires,    false);
        setPanel(panelCommentaires,  false);

      
        if      (ongletActif == tabApropos)      setPanel(panelApropos,      true);
        else if (ongletActif == tabEpisodes)     setPanel(panelEpisodes,     true);
        else if (ongletActif == tabBandes)       setPanel(panelBandes,       true);
        else if (ongletActif == tabSimilaires)   setPanel(panelSimilaires,   true);
        else if (ongletActif == tabCommentaires) {
            setPanel(panelCommentaires, true);
            rafraichirListeCommentaires(modeSerieActif ? "serie" : "film");
        }
    }

    
    @FXML private void onRetour()                      { ScreenManager.getInstance().goBack(); }
    @FXML private void onProfilClicked()               { System.out.println("Profil : " + labelUserName.getText()); }
    @FXML private void handleAcceuil(ActionEvent event){ ScreenManager.getInstance().navigateTo(Screen.home); }
    @FXML private void handleFilm(ActionEvent event)   { ScreenManager.getInstance().navigateTo(Screen.films); }
    @FXML private void handleSeries(ActionEvent event) { ScreenManager.getInstance().navigateTo(Screen.series); }
    @FXML private void handleMyList(ActionEvent event) { ScreenManager.getInstance().navigateTo(Screen.myList); }

    
    private void ouvrirEpisode(Episode ep) {
        UniversalPlayerController ctrl = ScreenManager.getInstance()
                .navigateAndGetController(Screen.Player);
        if (ctrl != null) ctrl.initEpisode(ep.getId(), userId);
    }

    private void set(Label label, String text) {
        if (label != null) label.setText(text != null ? text : "");
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private void loadImage(ImageView iv, String url) {
        try { iv.setImage(new Image(url, true)); }
        catch (Exception ignored) {}
    }

    private void setPanel(VBox panel, boolean visible) {
        if (panel == null) return;
        panel.setVisible(visible);
        panel.setManaged(visible);
    }

    private String genresText(Set<Category> set) {
        if (set == null || set.isEmpty()) return "-";
        return set.stream().map(Category::getName).limit(3)
                .reduce((a, b) -> a + " - " + b).orElse("-");
    }

    private String warningsText(Set<Warning> set) {
        if (set == null || set.isEmpty()) return "-";
        return set.stream().map(Warning::getNom)
                .reduce((a, b) -> a + " - " + b).orElse("-");
    }

    private StackPane creerImagePane(String urlImage) {
        StackPane imgPane = new StackPane();
        imgPane.setPrefSize(160, 90);
        imgPane.setMinSize(160, 90);
        imgPane.setMaxSize(160, 90);
        imgPane.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 4;");

        if (urlImage != null && !urlImage.isEmpty()) {
            try {
                Image img = new Image(urlImage, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(160); iv.setFitHeight(90); iv.setPreserveRatio(false);
                imgPane.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        Rectangle clip = new Rectangle(160, 90);
        clip.setArcWidth(6); clip.setArcHeight(6);
        imgPane.setClip(clip);
        return imgPane;
    }
}
