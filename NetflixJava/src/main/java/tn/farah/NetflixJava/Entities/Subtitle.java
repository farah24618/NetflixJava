package tn.farah.NetflixJava.Entities;

public class Subtitle {
	private int id;
	private String langage;
	private int idMedia;
	 private String url;       // chemin vers le fichier .srt
     // ex: "subtitles/film1_fr.srt"


	public Subtitle(int id, String langage, int idMedia, String url) {
		this.id = id;
		this.langage = langage;
		this.idMedia = idMedia;
		this.url = url;
	}

	public Subtitle(String langage, int idMedia, String url) {
		this.langage = langage;
		this.idMedia = idMedia;
		this.url = url;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Subtitle [id=" + id + ", langage=" + langage + ", idMedia=" + idMedia + ", url=" + url + "]";
	}

	
}
