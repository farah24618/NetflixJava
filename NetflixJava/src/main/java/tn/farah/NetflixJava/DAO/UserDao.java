package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final Connection connection;

    // Le constructeur reçoit désormais la connexion
    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (prenom, nom, email, password_hash, role, created_at, last_login, is_active, birth_date, phone, pseudo, estPaye) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getRole().name());
            pstmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            
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

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET prenom = ?, nom = ?, email = ?, password_hash = ?, role = ?, is_active = ?, birth_date = ?, phone = ?, pseudo=?, estPaye=? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getRole().name());
            pstmt.setBoolean(6, user.isActive());

            if (user.getBirthDate() != null) pstmt.setDate(7, Date.valueOf(user.getBirthDate()));
            else pstmt.setNull(7, Types.DATE);

            pstmt.setString(8, user.getPhone());
            pstmt.setString(9, user.getPseudo());
            pstmt.setBoolean(10, user.isEstPaye());
            pstmt.setInt(11, user.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

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

    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setPrenom(rs.getString("prenom"));
        user.setNom(rs.getString("nom"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));

        String roleStr = rs.getString("role");
        if (roleStr != null) user.setRole(UserRole.valueOf(roleStr));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) user.setCreatedAt(createdTs.toLocalDateTime());

        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        if (lastLoginTs != null) user.setLastLogin(lastLoginTs.toLocalDateTime());

        user.setActive(rs.getBoolean("is_active"));

        Date birthDateSql = rs.getDate("birth_date");
        if (birthDateSql != null) user.setBirthDate(birthDateSql.toLocalDate());

        user.setPhone(rs.getString("phone"));
        user.setPseudo(rs.getString("pseudo"));
        user.setEstPaye(rs.getBoolean("estPaye"));

        return user;
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

    public boolean updatePaymentStatus(int userId, boolean status) {
        String sql = "UPDATE users SET estPaye = ? WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setBoolean(1, status);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            return false;
        }
    }
}