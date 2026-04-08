package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    // --- CRÉATION ---
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (prenom, nom, email, password_hash, role, created_at, last_login, is_active, birth_date, phone, pseudo, estPaye) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getRole().name());
            
            // Gestion des dates avec vérification de nullité
            pstmt.setTimestamp(6, user.getCreatedAt() != null ? Timestamp.valueOf(user.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            
            if (user.getLastLogin() != null) pstmt.setTimestamp(7, Timestamp.valueOf(user.getLastLogin()));
            else pstmt.setNull(7, Types.TIMESTAMP);

            pstmt.setBoolean(8, user.isActive());

            if (user.getBirthDate() != null) pstmt.setDate(9, Date.valueOf(user.getBirthDate()));
            else pstmt.setNull(9, Types.DATE);

            pstmt.setString(10, user.getPhone());
            pstmt.setString(11, user.getPseudo());
            pstmt.setBoolean(12, user.isEstPaye());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- LECTURE ---
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapResultSetToUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }
    public boolean isPseudoTaken(final String pseudo, final int userId) {
        final String sql =
            "SELECT COUNT(*) FROM users WHERE pseudo = ? AND id != ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pseudo);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean updatePseudo(final int userId, final String newPseudo) {
        final String sql = "UPDATE users SET pseudo = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPseudo);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findByPseudo(String pseudo) {
        String sql = "SELECT * FROM users WHERE pseudo = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, pseudo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // --- MISE À JOUR ---
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET prenom = ?, nom = ?, email = ?, role = ?, is_active = ?, birth_date = ?, phone = ?, pseudo = ?, estPaye = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getRole().name());
            pstmt.setBoolean(5, user.isActive());
            
            if (user.getBirthDate() != null) pstmt.setDate(6, Date.valueOf(user.getBirthDate()));
            else pstmt.setNull(6, Types.DATE);

            pstmt.setString(7, user.getPhone());
            pstmt.setString(8, user.getPseudo());
            pstmt.setBoolean(9, user.isEstPaye());
            pstmt.setInt(10, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    public boolean updatePassword(String email, String hashedPass) {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, hashedPass);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updatePaymentStatus(int userId, boolean status) {
        String sql = "UPDATE users SET estPaye = ? WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setBoolean(1, status);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- SUPPRESSION ---
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- AUTHENTIFICATION ---
    public User login(String email, String passwordHash) {
        String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    updateLastLogin(user.getId());
                    return user;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- AUDIT LOGS ---
    public boolean addAuditLog(int adminId, String description) {
        String sql = "INSERT INTO audit_logs (admin_id, action_description, action_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, adminId);
            pstmt.setString(2, description);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<String> getAdminLogs(int adminId) {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT action_description, action_date FROM audit_logs WHERE admin_id = ? ORDER BY action_date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getTimestamp("action_date").toString();
                    String desc = rs.getString("action_description");
                    logs.add("[" + date + "] " + desc);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return logs;
    }

    // --- MAPPER PRIVÉ ---
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setPrenom(rs.getString("prenom"));
        user.setNom(rs.getString("nom"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPseudo(rs.getString("pseudo"));
        user.setPhone(rs.getString("phone"));
        user.setEstPaye(rs.getBoolean("estPaye"));
        user.setActive(rs.getInt("is_active") == 1);
        
        String roleStr = rs.getString("role");
        if (roleStr != null) user.setRole(UserRole.valueOf(roleStr.toUpperCase()));

        if (rs.getDate("birth_date") != null) {
            user.setBirthDate(rs.getDate("birth_date").toLocalDate());
        }
        if (rs.getTimestamp("created_at") != null) {
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("last_login") != null) {
            user.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
        }
        
        return user;
    }
}
        
        
        
        
        
        
        
        
        
        
        
        
