package tn.farah.NetflixJava.Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public abstract class  Media {
	private int id;
    private String titre;
    private String synopsis;
    private String casting;
    private LocalDate dateSortie;
    private String urlImageCover;
    private String urlImageBanner;
    private String urlTeaser;
    private Double ratingMoyen = 0.0;
    private AgeRating ageRating;
    private TypeMedia type;
	private Set<Warning> warnings;
    private LocalDateTime dateAjout = LocalDateTime.now();
    private Set<Category> genres = new HashSet<>();

    // ─── Constructeur vide ────────────────────────────────────────────────────
    // obligatoire pour pouvoir faire new Film() et new Serie() dans les DAO
    public Media() {
        this.genres   = new HashSet<>();
        this.warnings = new HashSet<>();
        this.ageRating=AgeRating.ALL;
    }

    // ─── Constructeur complet ─────────────────────────────────────────────────
    public Media(int id, String titre, String synopsis, String casting,
                 LocalDate dateSortie, String urlImageCover, String urlImageBanner,
                 String urlTeaser, Double ratingMoyen, AgeRating ageRating,
                 TypeMedia type, LocalDateTime dateAjout,
                 Set<Category> genres, Set<Warning> warnings) {

        this.id             = id;
        this.titre          = titre;
        this.synopsis       = synopsis;
        this.casting        = casting;
        this.dateSortie     = dateSortie;
        this.urlImageCover  = urlImageCover;
        this.urlImageBanner = urlImageBanner;
        this.urlTeaser      = urlTeaser;
        this.ratingMoyen    = ratingMoyen;
        this.ageRating      = ageRating;
        this.type           = type;
        this.dateAjout      = dateAjout;
        this.genres         = genres;
        this.warnings       = warnings;
    }
    public Set<Warning> getWarnings() {
		return warnings;
		//hedhi
	}

	public void setWarnings(Set<Warning> warnings) {
		this.warnings = warnings;
	}
	public TypeMedia getType() {
		return type;
	}

	public void setType(TypeMedia type) {
		this.type = type;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getSynopsis() {
		return synopsis;
	}
	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}
	public String getCasting() {
		return casting;
	}
	public void setCasting(String casting) {
		this.casting = casting;
	}
	public LocalDate getDateSortie() {
		return dateSortie;
	}
	public void setDateSortie(LocalDate dateSortie) {
		this.dateSortie = dateSortie;
	}
	public String getUrlImageCover() {
		return urlImageCover;
	}
	public void setUrlImageCover(String urlImageCover) {
		this.urlImageCover = urlImageCover;
	}
	public String getUrlImageBanner() {
		return urlImageBanner;
	}
	public void setUrlImageBanner(String urlImageBanner) {
		this.urlImageBanner = urlImageBanner;
	}
	public String getUrlTeaser() {
		return urlTeaser;
	}
	public void setUrlTeaser(String urlTeaser) {
		this.urlTeaser = urlTeaser;
	}
	public Double getRatingMoyen() {
		return ratingMoyen;
	}
	public void setRatingMoyen(Double ratingMoyen) {
		this.ratingMoyen = ratingMoyen;
	}
	public AgeRating getAgeRating() {
		return ageRating;
	}
	public void setAgeRating(AgeRating ageRating) {
		this.ageRating = ageRating;
	}
	public LocalDateTime getDateAjout() {
		return dateAjout;
	}
	public void setDateAjout(LocalDateTime dateAjout) {
		this.dateAjout = dateAjout;
	}
	public Set<Category> getGenres() {
		return genres;
	}
	public void setGenres(Set<Category> genres) {
		this.genres = genres;
	}


}
