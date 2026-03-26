package tn.farah.NetflixJava.Entities;
import java.time.LocalDate;
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
    private LocalDate birthDate;
    private String phone; 

   
    public User() {
    	 this.role = UserRole.USER;
    	 this.isActive = true;
    	 this.createdAt = LocalDateTime.now();
    }

    // 🔹 Constructeur simple
    public User(String email, String passwordHash, String fullName, LocalDate birthDate, String phone) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phone = phone;
    }

    // 🔹 Constructeur complet
    public User(int id, String email, String passwordHash, String fullName,
                UserRole role, LocalDateTime createdAt, LocalDateTime lastLogin,
                boolean isActive, LocalDate birthDate, String phone) {

        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.birthDate = birthDate;
        this.phone = phone;
    }

    // ==================== GETTERS ====================
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
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
    public void setFullName(String fullName) { this.fullName = fullName; }
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

    public String getFirstName() {
        if (fullName != null && fullName.contains(" ")) {
            return fullName.substring(0, fullName.indexOf(" "));
        }
        return fullName;
    }

    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", birthDate=" + birthDate +
                ", phone=" + phone +
                ", createdAt=" + createdAt +
                '}';
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
}