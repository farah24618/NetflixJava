package tn.farah.NetflixJava.Entities;

import java.time.LocalDateTime;

public class Saison {
	private int id;
    private int idSerie;
    private int numeroSaison;
    private String nom;
    private LocalDateTime dateSortie;
    
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

















	@Override
	public String toString() {
		return  nom;
	}





	




	














	



	



	

   

}
