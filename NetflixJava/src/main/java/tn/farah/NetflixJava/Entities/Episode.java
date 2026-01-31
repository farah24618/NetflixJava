package tn.farah.NetflixJava.Entities;
public class Episode {
	private int id;
    private int saisonId;
    private String titre;
    private int numeroEpisode;
    private String videoUrl; 
    private int duree;
    private String resume;
    private String miniatureUrl; 
    
    public Episode() {
    	
    }
    
    public Episode(int id, int saisonId, String titre,
    		int numeroEpisode, String videoUrl,
    		int duree, String resume, String miniatureUrl) {
        this.id = id;
        this.saisonId = saisonId;
        this.titre = titre;
        this.numeroEpisode = numeroEpisode;
        this.videoUrl = videoUrl;
        this.duree = duree;
        this.resume = resume;
        this.miniatureUrl = miniatureUrl;
    }

   
    public Episode(int saisonId, String titre, int numeroEpisode, String videoUrl, int duree, String resume,
			String miniatureUrl) {
		this.saisonId = saisonId;
		this.titre = titre;
		this.numeroEpisode = numeroEpisode;
		this.videoUrl = videoUrl;
		this.duree = duree;
		this.resume = resume;
		this.miniatureUrl = miniatureUrl;
	}

	public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSaisonId() { return saisonId; }
    public void setSaisonId(int saisonId) { this.saisonId = saisonId; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public int getNumeroEpisode() { return numeroEpisode; }
    public void setNumeroEpisode(int numeroEpisode) { this.numeroEpisode = numeroEpisode; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    public String getMiniatureUrl() { return miniatureUrl; }
    public void setMiniatureUrl(String miniatureUrl) { this.miniatureUrl = miniatureUrl; }

	@Override
	public String toString() {
		return "Episode [id=" + id + ", saisonId=" + saisonId + ", titre=" + titre + ", numeroEpisode=" + numeroEpisode
				+ ", videoUrl=" + videoUrl + ", duree=" + duree + ", resume=" + resume + ", miniatureUrl="
				+ miniatureUrl + "]";
	}
    
}
	
	
	
	


