package tn.farah.NetflixJava.Entities;

public class AdminStats {

    private int nbFilms;
    private int nbSeries;
    private int nbEpisodes;
    private int nbUsers;
    private int nbComments;
    private int nbFavorites;

    public AdminStats() {
    }

    public AdminStats(int nbFilms, int nbSeries, int nbEpisodes, int nbUsers, int nbComments, int nbFavorites) {
        this.nbFilms = nbFilms;
        this.nbSeries = nbSeries;
        this.nbEpisodes = nbEpisodes;
        this.nbUsers = nbUsers;
        this.nbComments = nbComments;
        this.nbFavorites = nbFavorites;
    }

    public int getNbFilms() {
        return nbFilms;
    }

    public void setNbFilms(int nbFilms) {
        this.nbFilms = nbFilms;
    }

    public int getNbSeries() {
        return nbSeries;
    }

    public void setNbSeries(int nbSeries) {
        this.nbSeries = nbSeries;
    }

    public int getNbEpisodes() {
        return nbEpisodes;
    }

    public void setNbEpisodes(int nbEpisodes) {
        this.nbEpisodes = nbEpisodes;
    }

    public int getNbUsers() {
        return nbUsers;
    }

    public void setNbUsers(int nbUsers) {
        this.nbUsers = nbUsers;
    }

    public int getNbComments() {
        return nbComments;
    }

    public void setNbComments(int nbComments) {
        this.nbComments = nbComments;
    }

    public int getNbFavorites() {
        return nbFavorites;
    }

    public void setNbFavorites(int nbFavorites) {
        this.nbFavorites = nbFavorites;
    }
}