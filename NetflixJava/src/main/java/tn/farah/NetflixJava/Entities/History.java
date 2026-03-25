package tn.farah.NetflixJava.Entities;

import java.time.LocalDateTime;

public class History {
	private int id;
	private int idUser;
	private int idMedia;
	private LocalDateTime dateVisionnage;
	private int tempsArret;
	private boolean estTermine;
	public History(int idUser, int idMedia, LocalDateTime dateVisionnage, int tempsArret, boolean estTermine) {
		this.idUser = idUser;
		this.idMedia = idMedia;
		this.dateVisionnage = dateVisionnage;
		this.tempsArret = tempsArret;
		this.estTermine = estTermine;
	}

	public History(int id, int idUser, int idMedia, LocalDateTime dateVisionnage, int tempsArret, boolean estTermine) {
		this.id = id;
		this.idUser = idUser;
		this.idMedia = idMedia;
		this.dateVisionnage = dateVisionnage;
		this.tempsArret = tempsArret;
		this.estTermine = estTermine;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
	public LocalDateTime getDateVisionnage() {
		return dateVisionnage;
	}
	public void setDateVisionnage(LocalDateTime dateVisionnage) {
		this.dateVisionnage = dateVisionnage;
	}
	public int getTempsArret() {
		return tempsArret;
	}
	public void setTempsArret(int tempsArret) {
		this.tempsArret = tempsArret;
	}
	public boolean getEstTermine() {
		return estTermine;
	}
	public void setEstTermine(boolean estTermine) {
		this.estTermine = estTermine;
	}

	@Override
	public String toString() {
		return "History [id=" + id + ", idUser=" + idUser + ", idMedia=" + idMedia + ", dateVisionnage="
				+ dateVisionnage + ", tempsArret=" + tempsArret + ", estTermine=" + estTermine + "]";
	}

}
