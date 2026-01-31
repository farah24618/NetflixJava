package tn.farah.NetflixJava.Entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Serie {
	private Long id;                  
    private String titre;             
    private String genre;             
    private String acteursGlobaux;    
    private String synopsis;          
    private Integer anneeSortie;      
    private String coverUrl;          
    private String trailerUrl;        
    private LocalDateTime createdAt;  

    private List<Saison> saisons = new ArrayList<>();

    public Serie() {
    }

    public Serie(Long id, String titre, String genre, String acteursGlobaux) {
        this.id = id;
        this.titre = titre;
        this.genre = genre;
        this.acteursGlobaux = acteursGlobaux;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getActeursGlobaux() { return acteursGlobaux; }
    public void setActeursGlobaux(String acteursGlobaux) { this.acteursGlobaux = acteursGlobaux; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public Integer getAnneeSortie() { return anneeSortie; }
    public void setAnneeSortie(Integer anneeSortie) { this.anneeSortie = anneeSortie; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Saison> getSaisons() { return saisons; }
    public void setSaisons(List<Saison> saisons) {
        this.saisons = (saisons == null) ? new ArrayList<>() : saisons;
    }

    public void addSaison(Saison saison) {
        if (saison != null) saisons.add(saison);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Serie)) return false;
        Serie serie = (Serie) o;
        return id != null && Objects.equals(id, serie.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Serie{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", genre='" + genre + '\'' +
                '}';
    }

}
