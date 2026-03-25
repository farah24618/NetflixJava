package tn.farah.NetflixJava.Entities;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private String contenu;
    private LocalDateTime datePublication;
    private int idUser;    // ID de l'auteur
    private int idMedia;   // ID du Film ou de la Série
    private boolean estSignale;
  //hedhi
    private boolean contientSpoils;

    public Commentaire(int id, String contenu, LocalDateTime datePublication,
                       int idUser, int idMedia, boolean estSignale, boolean contientSpoils) {
        this.id = id;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.idUser = idUser;
        this.idMedia = idMedia;
        this.estSignale = estSignale;
        this.contientSpoils = contientSpoils;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public LocalDateTime getDatePublication() {
		return datePublication;
	}

	public void setDatePublication(LocalDateTime datePublication) {
		this.datePublication = datePublication;
	}

	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public int getIdMedia() {
		return idMedia;
	}

	public void setIdMedia(int idMedia) {
		this.idMedia = idMedia;
	}

	public boolean isEstSignale() {
		return estSignale;
	}

	public void setEstSignale(boolean estSignale) {
		this.estSignale = estSignale;
	}

	public boolean isContientSpoils() {
		return contientSpoils;
	}

	public void setContientSpoils(boolean contientSpoils) {
		this.contientSpoils = contientSpoils;
	}


}



