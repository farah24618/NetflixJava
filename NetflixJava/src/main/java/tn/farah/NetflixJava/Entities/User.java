

	package tn.farah.NetflixJava.Entities;

	import java.time.LocalDateTime;



	public class User {

	    private int id;
	    private String email;
	    private String passwordHash;
	    private String fullName;
	    private UserRole role;
	    private LocalDateTime createdAt;
	    private LocalDateTime lastLogin;
	    private boolean isActive;

	    public User() {
	        this.role = UserRole.USER; // Rôle par défaut
	        this.isActive = true;
	        this.createdAt = LocalDateTime.now();
	    }

	    public User(String email, String passwordHash, String fullName) {
	        this();
	        this.email = email;
	        this.passwordHash = passwordHash;
	        this.fullName = fullName;
	    }

	    public User(int id, String email, String passwordHash, String fullName,
	                UserRole role, LocalDateTime createdAt, LocalDateTime lastLogin,
	                boolean isActive) {
	        this.id = id;
	        this.email = email;
	        this.passwordHash = passwordHash;
	        this.fullName = fullName;
	        this.role = role;
	        this.createdAt = createdAt;
	        this.lastLogin = lastLogin;
	        this.isActive = isActive;
	    }

	    // ==================== GETTERS ====================
	    public int getId() {
	        return id;
	    }

	    public String getEmail() {
	        return email;
	    }

	    public String getPasswordHash() {
	        return passwordHash;
	    }

	    public String getFullName() {
	        return fullName;
	    }

	    public UserRole getRole() {
	        return role;
	    }

	    public LocalDateTime getCreatedAt() {
	        return createdAt;
	    }

	    public LocalDateTime getLastLogin() {
	        return lastLogin;
	    }

	    public boolean isActive() {
	        return isActive;
	    }


	    // ==================== SETTERS ====================
	    public void setId(int id) {
	        this.id = id;
	    }

	    public void setEmail(String email) {
	        this.email = email;
	    }

	    public void setPasswordHash(String passwordHash) {
	        this.passwordHash = passwordHash;
	    }

	    public void setFullName(String fullName) {
	        this.fullName = fullName;
	    }

	    public void setActive(boolean active) {
	        this.isActive = active;
	    }

	    public void setRole(UserRole role) {
	        this.role = role;
	    }

	    // CORRIGÉ : Cette méthode était vide
	    public void setCreatedAt(LocalDateTime createdAt) {
	        this.createdAt = createdAt;
	    }

	    // AJOUTÉ : Il manquait le setter pour lastLogin
	    public void setLastLogin(LocalDateTime lastLogin) {
	        this.lastLogin = lastLogin;
	    }


	    // ==================== MÉTHODES UTILITAIRES ====================

	    /**
	     * Vérifie si l'utilisateur a le rôle administrateur
	     * * @return true si l'utilisateur est admin, false sinon
	     */
	    public boolean isAdmin() {
	        return this.role == UserRole.ADMIN;
	    }

	    /**
	     * Met à jour la date de dernière connexion à maintenant
	     */
	    public void updateLastLogin() {
	        this.lastLogin = LocalDateTime.now();
	    }

	    public String getFirstName() {
	        if (fullName != null && fullName.contains(" ")) {
	            return fullName.substring(0, fullName.indexOf(" "));
	        }
	        return fullName;
	    }

	    @Override
	    public String toString() {
	        return "User{" +
	                "id=" + id +
	                ", email='" + email + '\'' +
	                ", fullName='" + fullName + '\'' +
	                ", role=" + role +
	                ", isActive=" + isActive +
	                ", createdAt=" + createdAt +
	                '}';
	    }

	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj) {
				return true;
			}
	        if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
	        User user = (User) obj;
	        return id == user.id;
	    }

	    @Override
	    public int hashCode() {
	        return Integer.hashCode(id);
	    }
	}