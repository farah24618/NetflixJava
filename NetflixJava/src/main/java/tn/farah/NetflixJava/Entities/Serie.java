package tn.farah.NetflixJava.Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class Serie extends Media {

    private String genre;
    private boolean terminee;
   

    public Serie() {
    }

   
    
	public Serie(int id, String titre, String synopsis, String casting, LocalDate dateSortie, String urlImageCover,
			String urlImageBanner, String urlTeaser, Double ratingMoyen, AgeRating ageRating, TypeMedia type,
			LocalDateTime dateAjout, Set<Category> genres, Set<Warning> warnings, String genre, boolean terminee) {
		super(id, titre, synopsis, casting, dateSortie, urlImageCover, urlImageBanner, urlTeaser, ratingMoyen,
				ageRating, type, dateAjout, genres, warnings);
		this.genre = genre;
		this.terminee = terminee;
	}



	public Serie(int id, String titre, String synopsis, String genre, boolean termine) {
		super(id,titre,synopsis);
		this.genre=genre;
		this.terminee=termine;
		// TODO Auto-generated constructor stub
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

   

    @Override
    public String toString() {
        return "Serie{" +
                "id=" + getId() +
                ", titre='" + getTitre() + '\'' +
                ", synopsis='" + getSynopsis() + '\'' +
                ", genre='" + genre + '\'' +
                ", terminee=" + terminee +
               
                '}';
    }
}