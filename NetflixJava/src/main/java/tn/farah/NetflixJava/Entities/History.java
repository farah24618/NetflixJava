package tn.farah.NetflixJava.Entities;

import java.time.LocalDateTime;

public class History {
    private int id;
    private int idUser;
    private Integer filmId;      // NULL si c'est un épisode
    private Integer episodeId;   // NULL si c'est un film
    private LocalDateTime dateVisionnage;
    private int tempsArret;
    private boolean estTermine;

    // Constructeur sans id (pour INSERT)
    public History(int idUser, Integer filmId, Integer episodeId,
                   LocalDateTime dateVisionnage, int tempsArret, boolean estTermine) {
        this.idUser = idUser;
        this.filmId = filmId;
        this.episodeId = episodeId;
        this.dateVisionnage = dateVisionnage;
        this.tempsArret = tempsArret;
        this.estTermine = estTermine;
    }

    // Constructeur avec id (pour SELECT)
    public History(int id, int idUser, Integer filmId, Integer episodeId,
                   LocalDateTime dateVisionnage, int tempsArret, boolean estTermine) {
        this.id = id;
        this.idUser = idUser;
        this.filmId = filmId;
        this.episodeId = episodeId;
        this.dateVisionnage = dateVisionnage;
        this.tempsArret = tempsArret;
        this.estTermine = estTermine;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public Integer getFilmId() { return filmId; }
    public void setFilmId(Integer filmId) { this.filmId = filmId; }

    public Integer getEpisodeId() { return episodeId; }
    public void setEpisodeId(Integer episodeId) { this.episodeId = episodeId; }

    public LocalDateTime getDateVisionnage() { return dateVisionnage; }
    public void setDateVisionnage(LocalDateTime dateVisionnage) { this.dateVisionnage = dateVisionnage; }

    public int getTempsArret() { return tempsArret; }
    public void setTempsArret(int tempsArret) { this.tempsArret = tempsArret; }

    public boolean getEstTermine() { return estTermine; }
    public void setEstTermine(boolean estTermine) { this.estTermine = estTermine; }

    // ── Utilitaires ──────────────────────────────────────────────────────────

    public boolean isFilm() { return filmId != null; }
    public boolean isEpisode() { return episodeId != null; }

    @Override
    public String toString() {
        return "History [id=" + id + ", idUser=" + idUser +
               ", filmId=" + filmId + ", episodeId=" + episodeId +
               ", dateVisionnage=" + dateVisionnage +
               ", tempsArret=" + tempsArret + ", estTermine=" + estTermine + "]";
    }
}