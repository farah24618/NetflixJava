package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Service.FilmService;
import tn.farah.NetflixJava.utils.ConxDB;
import tn.farah.NetflixJava.utils.Screen;
import tn.farah.NetflixJava.utils.ScreenManager;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class FilmViewController {

    @FXML
    private TextField searchField;

    @FXML
    private HBox profilBox;

    @FXML
    private Label filmTitleVideo;

    @FXML
    private Label currentTime;

    @FXML
    private Label totalTime;

    @FXML
    private Label currentTimeSmall;

    @FXML
    private Slider progressSlider;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Button btnPlayPause;

    @FXML
    private Button btnRewind;

    @FXML
    private Button btnForward;

    @FXML
    private Button btnSubtitles;

    @FXML
    private Button btnVolume;

    @FXML
    private Button btnFullscreen;

    @FXML
    private Label filmTitle;

    @FXML
    private Label labelAnnee;

    @FXML
    private Label labelDuree;

    @FXML
    private Label labelGenre;

    @FXML
    private Label filmDesc;

    @FXML
    private Button btnLike;

    @FXML
    private Button btnDislike;

    @FXML
    private Button btnShare;

    @FXML
    private Button btnDownload;

    @FXML
    private VBox castList;

    @FXML
    private VBox tabApropos;

    @FXML
    private VBox tabBandes;

    @FXML
    private VBox tabCommentaires;

    @FXML
    private VBox tabSimilaires;

    @FXML
    private VBox contenuOnglet;

    @FXML
    private VBox panelApropos;

    @FXML
    private VBox panelBandes;

    @FXML
    private VBox panelSimilaires;

    private final Connection connection = ConxDB.getInstance();
    private final FilmService filmService = new FilmService(connection);

    private Film currentFilm;
    private int currentUserId = 1;
    private String currentUsername = "Utilisateur";

    @FXML
    public void initialize() {
        chargerFilmDepuisBase(1);
    }

    public void chargerFilmDepuisBase(int filmId) {
        Film film = filmService.getFilmById(filmId);

        if (film == null) {
            filmTitle.setText("Film introuvable");
            filmTitleVideo.setText("Film introuvable");
            filmDesc.setText("");
            castList.getChildren().clear();
            return;
        }

        this.currentFilm = film;
        afficherFilm(film);
    }

    public void afficherFilm(Film film) {
        filmTitle.setText(valueOrDefault(film.getTitre(), "Titre inconnu"));
        filmTitleVideo.setText(valueOrDefault(film.getTitre(), "Titre inconnu"));
        filmDesc.setText(valueOrDefault(film.getSynopsis(), "Aucune description disponible."));

        if (film.getDateSortie() != null) {
            labelAnnee.setText(String.valueOf(film.getDateSortie().getYear()));
        } else {
            labelAnnee.setText("----");
        }

        labelDuree.setText(getDureeText(film));
        labelGenre.setText(getGenresText(film.getGenres()));

        remplirActeurs(film.getCasting());
    }

    private void remplirActeurs(String casting) {
        castList.getChildren().clear();

        if (casting == null || casting.trim().isEmpty()) {
            Label empty = new Label("Aucun acteur disponible");
            empty.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
            castList.getChildren().add(empty);
            return;
        }

        String[] acteurs = Arrays.stream(casting.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        for (String acteur : acteurs) {
            HBox actorRow = new HBox();
            actorRow.setSpacing(10);

            Label actorLabel = new Label("• " + acteur);
            actorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

            actorRow.getChildren().add(actorLabel);
            castList.getChildren().add(actorRow);
        }
    }

    private String getGenresText(Set<Category> genres) {
        if (genres == null || genres.isEmpty()) {
            return "Genre inconnu";
        }

        return genres.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));
    }

    private String getDureeText(Film film) {
        return film.getDuree() + " min";
    }

    private String valueOrDefault(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    @FXML
    public void onPlayPause() {
        System.out.println("Play / Pause");
    }

    @FXML
    public void onRewind() {
        System.out.println("Rewind");
    }

    @FXML
    public void onForward() {
        System.out.println("Forward");
    }

    @FXML
    public void onSubtitles() {
        System.out.println("Subtitles");
    }

    @FXML
    public void onVolume() {
        System.out.println("Volume");
    }

    @FXML
    public void onFullscreen() {
        System.out.println("Fullscreen");
    }

    @FXML
    public void onLike() {
        System.out.println("Like film");
    }

    @FXML
    public void onDislike() {
        System.out.println("Dislike film");
    }

    @FXML
    public void onShare() {
        System.out.println("Share film");
    }

    @FXML
    public void onDownload() {
        System.out.println("Download film");
    }

    @FXML
    public void onTabApropos() {
        panelApropos.setVisible(true);
        panelApropos.setManaged(true);

        panelBandes.setVisible(false);
        panelBandes.setManaged(false);

        panelSimilaires.setVisible(false);
        panelSimilaires.setManaged(false);
    }

    @FXML
    public void onTabBandes() {
        panelApropos.setVisible(false);
        panelApropos.setManaged(false);

        panelBandes.setVisible(true);
        panelBandes.setManaged(true);

        panelSimilaires.setVisible(false);
        panelSimilaires.setManaged(false);
    }

    @FXML
    public void onTabSimilaires() {
        panelApropos.setVisible(false);
        panelApropos.setManaged(false);

        panelBandes.setVisible(false);
        panelBandes.setManaged(false);

        panelSimilaires.setVisible(true);
        panelSimilaires.setManaged(true);
    }

    @FXML
    public void onTabCommentaires() {
        if (currentFilm == null) {
            return;
        }

        EpisodeCommentsController controller =
                ScreenManager.getInstance().navigateAndGetController(Screen.episodeComments);

        if (controller != null) {
            controller.initData(
                    currentFilm.getId(),
                    "film",
                    currentFilm.getTitre(),
                    currentFilm.getSynopsis(),
                    currentUserId,
                    currentUsername
            );
        }
    }

    public void setCurrentUser(int userId, String username) {
        this.currentUserId = userId;
        this.currentUsername = username;
    }
}
