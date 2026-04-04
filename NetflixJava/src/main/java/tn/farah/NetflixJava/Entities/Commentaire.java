package tn.farah.NetflixJava.Entities;

import java.time.LocalDateTime;

public class Commentaire {

    private int id;
    private int mediaId;
    private boolean signale; // film, serie, episode
    private int userId;
    private String username;
    private String contenu;
    private int likes;
    private boolean spoiler;
    private LocalDateTime dateCommentaire;

    public Commentaire() {
    }

    public Commentaire(int id, int mediaId, boolean signale, int userId, String username,
                       String contenu, int likes, boolean spoiler, LocalDateTime dateCommentaire) {
        this.id = id;
        this.mediaId = mediaId;
        this.signale=signale;
        this.userId = userId;
        this.username = username;
        this.contenu = contenu;
        this.likes = likes;
        this.spoiler = spoiler;
        this.dateCommentaire = dateCommentaire;
    }

    public boolean isSignale() {
		return signale;
	}

	public void setSignale(boolean signale) {
		this.signale = signale;
	}

	public Commentaire(int mediaId, String mediaType, int userId, String username,
                       String contenu, boolean spoiler) {
        this.mediaId = mediaId;
       
        this.userId = userId;
        this.username = username;
        this.contenu = contenu;
        this.likes = 0;
        this.spoiler = spoiler;
        this.dateCommentaire = LocalDateTime.now();
    }

    public Commentaire(int userId2,int mediaid ,int i, String texte, boolean isSpoiler) {
   this.userId=userId2;
   this.mediaId=mediaid;
   this.likes=i;
   this.contenu=texte;
   this.spoiler=isSpoiler;
    }

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

  

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    public void setSpoiler(boolean spoiler) {
        this.spoiler = spoiler;
    }

    public LocalDateTime getDateCommentaire() {
        return dateCommentaire;
    }

    public void setDateCommentaire(LocalDateTime dateCommentaire) {
        this.dateCommentaire = dateCommentaire;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", mediaId=" + mediaId +
               
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", contenu='" + contenu + '\'' +
                ", likes=" + likes +
                ", spoiler=" + spoiler +
                ", dateCommentaire=" + dateCommentaire +
                '}';
    }
}


