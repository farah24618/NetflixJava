package tn.farah.NetflixJava.Entities;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {


    private int id;
    private String email;
    private String passwordHash;
    private String prenom;
    private String nom;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean isActive;
    private LocalDate birthDate;
    private String phone;
    private String pseudo;
    private boolean estPaye;

   
    public User() {
    	 this.role = UserRole.USER;
    	 this.isActive = true;
    	 this.estPaye=false;
    	 this.createdAt = LocalDateTime.now();
    }

   

    public User(int id, String email, String passwordHash, String prenom, String nom, UserRole role,
			LocalDateTime createdAt, LocalDateTime lastLogin, boolean isActive, LocalDate birthDate, String phone,String pseudo,boolean estPaye) {
		super();
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.prenom = prenom;
		this.nom = nom;
		this.role = role;
		this.createdAt = createdAt;
		this.lastLogin = lastLogin;
		this.isActive = isActive;
		this.birthDate = birthDate;
		this.phone = phone;
		this.pseudo=pseudo;
		this.estPaye=estPaye;
	}



	

    public User(String email, String passwordHash, String prenom, String nom, UserRole role, LocalDateTime createdAt,
			LocalDateTime lastLogin, boolean isActive, LocalDate birthDate, String phone,String pseudo,boolean estPaye) {
		super();
		this.email = email;
		this.passwordHash = passwordHash;
		this.prenom = prenom;
		this.nom = nom;
		this.role = role;
		this.createdAt = createdAt;
		this.lastLogin = lastLogin;
		this.isActive = isActive;
		this.birthDate = birthDate;
		this.phone = phone;
		this.pseudo=pseudo;
		this.estPaye=estPaye;
	}



	// ==================== GETTERS ====================
    
    public int getId() { return id; }
    public String getPrenom() {
		return prenom;
	}



	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}



	public String getNom() {
		return nom;
	}



	public void setNom(String nom) {
		this.nom = nom;
	}



	public String getPseudo() {
		return pseudo;
	}



	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}



	public boolean isEstPaye() {
		return estPaye;
	}



	public void setEstPaye(boolean estPaye) {
		this.estPaye = estPaye;
	}



	public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
   
    public UserRole getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public boolean isActive() { return isActive; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhone() { return phone; }

    // ==================== SETTERS ====================
    public void setId(int id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public void setRole(UserRole role) { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public void setActive(boolean active) { this.isActive = active; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setPhone(String phone) { this.phone = phone; }

    // ==================== MÉTHODES ====================
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

   

    @Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", passwordHash=" + passwordHash + ", prenom=" + prenom
				+ ", nom=" + nom + ", role=" + role + ", createdAt=" + createdAt + ", lastLogin=" + lastLogin
				+ ", isActive=" + isActive + ", birthDate=" + birthDate + ", phone=" + phone + ", pseudo=" + pseudo
				+ ", estPaye=" + estPaye + "]";
	}



	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //zedtha hajti beha fl admin
 // Dans User.java

    public String getInitial() {
        // On vérifie que le prénom n'est pas nul ET pas vide
        if (prenom != null && !prenom.trim().isEmpty()) {
            return prenom.substring(0, 1).toUpperCase();
        }
        // Si pas de prénom, on essaie l'email
        if (email != null && !email.trim().isEmpty()) {
            return email.substring(0, 1).toUpperCase();
        }
        return "?";
    }

    public String getUsername() {
        // On vérifie que prenom et nom existent et ne sont pas vides
        if (prenom != null && !prenom.trim().isEmpty() && 
            nom != null && !nom.trim().isEmpty()) {
            return prenom + "." + nom.substring(0, 1).toUpperCase();
        }
        // Sinon, on affiche juste le prénom ou l'email par défaut
        return (prenom != null && !prenom.isEmpty()) ? prenom : email;
    }
    
    public String getStatus() {
        if (!isActive) return "Blocked";
        return (role == UserRole.ADMIN) ? "Admin" : "User";
    }
    
    
    
}