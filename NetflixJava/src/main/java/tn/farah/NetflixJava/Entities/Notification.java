package tn.farah.NetflixJava.Entities;


	public class Notification {

	    private int id;
	    private int userId;
	    private String type;
	    private String title;
	    private String message;
	    private String date;
	    private boolean important;
	    private boolean isRead;

	    public Notification(int id, int userId, String type, String title,
	                        String message, String date,
	                        boolean important, boolean isRead) {

	        this.id = id;
	        this.userId = userId;
	        this.type = type;
	        this.title = title;
	        this.message = message;
	        this.date = date;
	        this.important = important;
	        this.isRead = isRead;
	    }

	    public String getType() { return type; }
	    public boolean isRead() { return isRead; }
	    public int getId() { return id; }
	    public String getTitle() { return title; }
	    public String getMessage() { return message; }
	    public String getDate() { return date; }
	    public boolean isImportant() { return important; }

	    public void setRead(boolean read) {
	        isRead = read;
	    }

	    @Override
	    public String toString() {
	        return title + " - " + message;
	    }
	    public int getUserId() {
	        return userId;
	    }

		public void setId(int id) {
			this.id = id;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

		public void setType(String type) {
			this.type = type;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public void setImportant(boolean important) {
			this.important = important;
		}
	}