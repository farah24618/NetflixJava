package tn.farah.NetflixJava.Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Serie extends Media {

   
    private boolean terminee;
   

   
   
    




	public Serie(int id, String titre, String synopsis, String casting, LocalDate dateSortie, String urlImageCover,
			String urlImageBanner, String urlTeaser, Double ratingMoyen, AgeRating ageRating, TypeMedia type,
			LocalDateTime dateAjout, Set<Category> genres, Set<Warning> warnings,  boolean terminee) {
		super(id, titre, synopsis, casting, dateSortie, urlImageCover, urlImageBanner, urlTeaser, ratingMoyen,
				ageRating, type, dateAjout, genres, warnings);
		
		this.terminee = terminee;
	}



	public Serie(int id, String titre, String synopsis , boolean termine) {
		super(id,titre,synopsis);
		
		this.terminee=termine;
		
	}



	public Serie() {
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
                
                ", terminee=" + terminee +
               
                '}';
    }

    
    
    
    
    
    
    
    
    
 
    
    
    
    
    
    
    
    
    
   
   
      
        
        
        
        
        
        
        
    }
	
