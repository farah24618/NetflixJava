package tn.farah.NetflixJava.Entities;


	public class Rating {
	    
	    private int id;
	    private int userId;
	    private int filmId;
	    private int score;

	    public Rating() {
	    }

	    public Rating(int userId, int filmId, int score) {
	        this.userId = userId;
	        this.filmId = filmId;
	        this.score = score;
	    }

	    public Rating(int id, int userId, int filmId, int score) {
	        this.id = id;
	        this.userId = userId;
	        this.filmId = filmId;
	        this.score = score;
	    }

	    public int getId() { return id; }
	    public void setId(int id) { this.id = id; }

	    public int getUserId() { return userId; }
	    public void setUserId(int userId) { this.userId = userId; }

	    public int getFilmId() { return filmId; }
	    public void setFilmId(int filmId) { this.filmId = filmId; }

	    public int getScore() { return score; }
	    public void setScore(int score) { this.score = score; }
	}


