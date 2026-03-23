package tn.farah.NetflixJava.Entities;

public class Serie extends Media {

    private String genre;
    private boolean terminee;
    private int dureeIntro;

    public Serie() {
    }

    public Serie(String genre, boolean terminee, int dureeIntro) {
        this.genre = genre;
        this.terminee = terminee;
        this.dureeIntro = dureeIntro;
    }

    public Serie(int id, String titre, String synopsis, String genre, boolean terminee, int dureeIntro) {
        this.setId(id);
        this.setTitre(titre);
        this.setSynopsis(synopsis);
        this.genre = genre;
        this.terminee = terminee;
        this.dureeIntro = dureeIntro;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isTerminee() {
        return terminee;
    }

    public void setTerminee(boolean terminee) {
        this.terminee = terminee;
    }

    public int getDureeIntro() {
        return dureeIntro;
    }

    public void setDureeIntro(int dureeIntro) {
        this.dureeIntro = dureeIntro;
    }

    @Override
    public String toString() {
        return "Serie{" +
                "id=" + getId() +
                ", titre='" + getTitre() + '\'' +
                ", synopsis='" + getSynopsis() + '\'' +
                ", genre='" + genre + '\'' +
                ", terminee=" + terminee +
                ", dureeIntro=" + dureeIntro +
                '}';
    }
}