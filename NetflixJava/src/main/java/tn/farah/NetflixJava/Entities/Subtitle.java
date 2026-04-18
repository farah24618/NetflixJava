package tn.farah.NetflixJava.Entities;

public class Subtitle {
	private int id;
	private String langage;
	private int filmId;
	private int episodeId;
	 private String url;       



	
	public Subtitle(String langage, int filmId, int episodeId, String url) {
		this.langage = langage;
		this.filmId = filmId;
		this.episodeId = episodeId;
		this.url = url;
	}
	public Subtitle(int id, String langage, int filmId, int episodeId, String url) {
		this.id = id;
		this.langage = langage;
		this.filmId = filmId;
		this.episodeId = episodeId;
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

	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public int getFilmId() {
		return filmId;
	}
	public void setFilmId(int filmId) {
		this.filmId = filmId;
	}
	public int getEpisodeId() {
		return episodeId;
	}
	public void setEpisodeId(int episodeId) {
		this.episodeId = episodeId;
	}
	@Override
	public String toString() {
		return "Subtitle [id=" + id + ", langage=" + langage + ", filmId=" + filmId + ", episodeId=" + episodeId
				+ ", url=" + url + "]";
	}

	


}
