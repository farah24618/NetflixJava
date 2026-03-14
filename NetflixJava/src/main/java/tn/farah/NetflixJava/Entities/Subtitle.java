package tn.farah.NetflixJava.Entities;

public class Subtitle {
	private int id;
	private String langage;
	private int idMedia;
	
	public Subtitle(String langage, int idMedia) {
		this.langage = langage;
		this.idMedia = idMedia;
	}

	public Subtitle(int id, String langage, int idMedia) {
		this.id = id;
		this.langage = langage;
		this.idMedia = idMedia;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLangage() {
		return langage;
	}

	public void setLangage(String langage) {
		this.langage = langage;
	}

	public int getIdMedia() {
		return idMedia;
	}

	public void setIdMedia(int idMedia) {
		this.idMedia = idMedia;
	}

	@Override
	public String toString() {
		return "Subtitle [id=" + id + ", langage=" + langage + ", idMedia=" + idMedia + "]";
	}

}
