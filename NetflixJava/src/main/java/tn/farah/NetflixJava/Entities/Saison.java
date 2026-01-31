package tn.farah.NetflixJava.Entities;

import java.util.ArrayList;


import java.util.List;

public class Saison {
	private int id;
    private int mediaId;
    private int numeroSaison;
    
    // Liste optionnelle pour faciliter l'affichage JavaFX
    private List<Episode> episodes;
    public Saison() {
        this.episodes = new ArrayList<>();
    }
    
    public Saison(int mediaId, int numeroSaison) {
		this.mediaId = mediaId;
		this.numeroSaison = numeroSaison;
		this.episodes = new ArrayList<>();
	}
    public Saison(int id, int mediaId, int numeroSaison) {
        this.id = id;
        this.mediaId = mediaId;
        this.numeroSaison = numeroSaison;
        this.episodes = new ArrayList<>();
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMediaId() { return mediaId; }
    public void setMediaId(int mediaId) { this.mediaId = mediaId; }

    public int getNumeroSaison() { return numeroSaison; }
    public void setNumeroSaison(int numeroSaison) { this.numeroSaison = numeroSaison; }

    public List<Episode> getEpisodes() { return episodes; }
    public void setEpisodes(List<Episode> episodes) { this.episodes = episodes; }

    @Override
    public String toString() {
        return "Saison " + numeroSaison + " (ID: " + id + ")";
    }
    public void addEpisode(Episode e) {
    	if(!episodes.contains(e)) {
    		episodes.add(e);}
    }
}
