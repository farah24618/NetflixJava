package tn.farah.NetflixJava.Entities;

import java.util.ArrayList;
import java.util.List;

public class Saison {
	private int id;
    private int idSerie;
    private int numeroSaison;
    public Saison() {
    }

    public Saison(int idSerie, int numeroSaison) {
		this.idSerie = idSerie;
		this.numeroSaison = numeroSaison;

	}
    public Saison(int id, int idSerie, int numeroSaison) {
        this.id = id;
        this.idSerie = idSerie;
        this.numeroSaison = numeroSaison;

    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }



    public int getIdSerie() {
		return idSerie;
	}

	public void setIdSerie(int idSerie) {
		this.idSerie = idSerie;
	}

	public int getNumeroSaison() { return numeroSaison; }
    public void setNumeroSaison(int numeroSaison) { this.numeroSaison = numeroSaison; }



    @Override
    public String toString() {
        return "Saison " + numeroSaison + " (ID: " + id + ")";
    }

    private List<Episode> episodes = new ArrayList<>();

 // La méthode doit retourner une List<Episode> et non <Serie>
 public List<Episode> getEpisodes() {
     return episodes;
 }

 public void setEpisodes(List<Episode> episodes) {
     this.episodes = episodes;
 }
}
