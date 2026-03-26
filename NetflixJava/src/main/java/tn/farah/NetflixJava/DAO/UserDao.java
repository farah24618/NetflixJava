package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    // 1️⃣ AJOUTER UN UTILISATEUR
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (email, password_hash, full_name, role, created_at, last_login, is_active, birth_date, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());

            pstmt.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));

            if (user.getLastLogin() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(user.getLastLogin()));
            } else {
                pstmt.setNull(6, Types.TIMESTAMP);
            }

            pstmt.setBoolean(7, user.isActive());

            if (user.getBirthDate() != null) {
                pstmt.setDate(8, Date.valueOf(user.getBirthDate()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }

            pstmt.setString(9, user.getPhone());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2️⃣ RÉCUPÉRER UN UTILISATEUR PAR ID
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

    // 3️⃣ RÉCUPÉRER TOUS LES UTILISATEURS
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) users.add(mapResultSetToUser(rs));

        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    // 4️⃣ METTRE À JOUR UN UTILISATEUR
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email = ?, password_hash = ?, full_name = ?, role = ?, is_active = ?, birth_date = ?, phone = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setBoolean(5, user.isActive());

            if (user.getBirthDate() != null) pstmt.setDate(6, Date.valueOf(user.getBirthDate()));
            else pstmt.setNull(6, Types.DATE);

            pstmt.setString(7, user.getPhone());

            pstmt.setInt(8, user.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5️⃣ SUPPRIMER UN UTILISATEUR
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 6️⃣ LOGIN
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

    // 🔧 PRIVÉ
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
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));

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

        return user;
    }

    // 🔹 Rechercher par email
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
}