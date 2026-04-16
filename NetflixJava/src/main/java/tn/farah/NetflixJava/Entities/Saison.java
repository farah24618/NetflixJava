package tn.farah.NetflixJava.Entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Saison {
	private int id;
    private int idSerie;
    private int numeroSaison;
    private String nom;
    private LocalDateTime dateSortie;
    private List<Episode> episodes = new ArrayList<>();
    public Saison() {
    }

   



	




	
    





	public Saison(int idSerie, int numeroSaison, String nom, LocalDateTime dateSortie) {
		
		this.idSerie = idSerie;
		this.numeroSaison = numeroSaison;
		this.nom = nom;
		this.dateSortie = dateSortie;
		
	}

















	public Saison(int id, int idSerie, int numeroSaison, String nom, LocalDateTime dateSortie ) {
		this.id = id;
		this.idSerie = idSerie;
		this.numeroSaison = numeroSaison;
		this.nom = nom;
		this.dateSortie = dateSortie;
		
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





	public String getNom() {
		return nom;
	}





	public void setNom(String nom) {
		this.nom = nom;
	}





	public LocalDateTime getDateSortie() {
		return dateSortie;
	}





	public void setDateSortie(LocalDateTime dateSortie) {
		this.dateSortie = dateSortie;
	}





	




	public List<Episode> getEpisodes() {
		return episodes;
	}





	public void setEpisodes(List<Episode> episodes) {
		this.episodes = episodes;
	}

















	@Override
	public String toString() {
		return "Saison [id=" + id + ", idSerie=" + idSerie + ", numeroSaison=" + numeroSaison + ", nom=" + nom
				+ ", dateSortie=" + dateSortie + ", episodes=" + episodes + "]";
	}





	



	

   

}
