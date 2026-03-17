package tn.farah.NetflixJava.DAO;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//import jdk.javadoc.internal.doclets.formats.html.markup.HtmlAttr.Role;
import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;
import tn.farah.NetflixJava.utils.ConxDB;


//package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private Connection connection;

    // Constructeur avec la connexion à la base de données
    public UserDao(Connection connection) {
        this.connection = connection;
    }

    // 1. AJOUTER un utilisateur
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (email, password_hash, full_name, role, created_at, last_login, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name()); // Convertit l'Enum en String (ex: "USER")
            
            // Conversion de LocalDateTime en Timestamp pour SQL
            pstmt.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            
            // Le lastLogin peut être null au moment de l'inscription
            if (user.getLastLogin() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(user.getLastLogin()));
            } else {
                pstmt.setNull(6, java.sql.Types.TIMESTAMP);
            }
            
            pstmt.setBoolean(7, user.isActive());

            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. RÉCUPÉRER un utilisateur par son ID
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. RÉCUPÉRER tous les utilisateurs
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 4. METTRE À JOUR un utilisateur
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email = ?, password_hash = ?, full_name = ?, role = ?, is_active = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setBoolean(5, user.isActive());
            pstmt.setInt(6, user.getId());

            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. SUPPRIMER un utilisateur
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. LOGIN (Connexion)
    public User login(String email, String passwordHash) {
        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    
                    // Met à jour la date de dernière connexion en base de données
                    updateLastLogin(user.getId()); 
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- MÉTHODES UTILITAIRES PRIVÉES ---

    // Met à jour la date de dernière connexion
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Convertit une ligne de la base de données en objet User Java
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        
        // Convertit le texte de la BDD ("USER" ou "ADMIN") en Enum Java
        String roleStr = rs.getString("role");
        if (roleStr != null) {
            user.setRole(UserRole.valueOf(roleStr));
        }
        
        // Gestion des dates : SQL Timestamp -> Java LocalDateTime
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            user.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        if (lastLoginTs != null) {
            user.setLastLogin(lastLoginTs.toLocalDateTime());
        }
        
        user.setActive(rs.getBoolean("is_active"));
        
        return user;
    }
}