package tn.farah.NetflixJava.Entities;

import java.time.LocalDate;
import tn.farah.NetflixJava.Entities.Warning;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class Film extends Media{
	private String urlVedio;
	private int duree;
	private int nbreVue;
	//hedhi


    // ─── Constructeur complet ─────────────────────────────────────────────────
    public Film(int id, String titre, String synopsis, String casting,
                LocalDate dateSortie, String urlImageCover, String urlImageBanner,
                String urlTeaser, Double ratingMoyen, AgeRating ageRating,
                TypeMedia type, LocalDateTime dateAjout,
                Set<Category> genres, Set<Warning> warnings,
                String urlVedio, int duree, int nbreVue) {

        // appelle le constructeur complet de Media
        super(id, titre, synopsis, casting, dateSortie, urlImageCover,
              urlImageBanner, urlTeaser, ratingMoyen, ageRating,
              type, dateAjout, genres, warnings);

        this.urlVedio = urlVedio;
        this.duree    = duree;
        this.nbreVue  = nbreVue;
    }

	

	public Film(int id, String titre, String synopsis, String casting, LocalDate dateSortie, String urlImageCover,
			String urlImageBanner, String urlTeaser, Double ratingMoyen, AgeRating ageRating, TypeMedia type,
			LocalDateTime dateAjout, Set<Category> genres, Set<Warning> warnings) {
		super(id, titre, synopsis, casting, dateSortie, urlImageCover, urlImageBanner, urlTeaser, ratingMoyen, ageRating, type,
				dateAjout, genres, warnings);
		// TODO Auto-generated constructor stub
	}



	public Film(int id, String titre, String synopsis, String casting, LocalDate dateSortie, String urlImageCover,
			String urlImageBanner, String urlTeaser, Double ratingMoyen, AgeRating ageRating, TypeMedia type,
			String producteur, List<Acteur> acteurs, Set<Warning> warnings, LocalDateTime dateAjout,
			Set<Category> genres) {
		super(id, titre, synopsis, casting, dateSortie, urlImageCover, urlImageBanner, urlTeaser, ratingMoyen, ageRating, type,
				producteur, warnings, dateAjout, genres);
		// TODO Auto-generated constructor stub
	}



	public Film(int id2, String titre2, String synopsis2) {
		super(id2, titre2, synopsis2);
		// TODO Auto-generated constructor stub
	}



	public Film () {
		super();
	}

	public String getUrlVedio() {
		return urlVedio;
	}
	public void setUrlVedio(String urlVedio) {
		this.urlVedio = urlVedio;
	}
	public int getDuree() {
		return duree;
	}
	public void setDuree(int duree) {
		this.duree = duree;
	}
	public int getNbreVue() {
		return nbreVue;
	}
	public void setNbreVue(int nbreVue) {
		this.nbreVue = nbreVue;
	}



}
