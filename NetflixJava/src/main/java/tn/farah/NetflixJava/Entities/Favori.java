package tn.farah.NetflixJava.Entities;


public class Favori {

    private int id;
    private int userId;
    private int mediaId;

    public Favori() {
    }

    public Favori(int id, int userId, int mediaId) {
        this.id = id;
        this.userId = userId;
        this.mediaId = mediaId;
    }

    public Favori(int userId, int mediaId) {
        this.userId = userId;
        this.mediaId = mediaId;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }
}