package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.User;
import tn.farah.NetflixJava.Entities.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
//...
public class UserDao {

    private Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (prenom, nom, email, password_hash, role, created_at, last_login, is_active, birth_date, phone, pseudo, estPaye) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // ✅ Ajout de Statement.RETURN_GENERATED_KEYS
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
                // ✅ RÉCUPÉRATION DE L'ID GÉNÉRÉ
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1)); // On injecte le vrai ID dans l'objet
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
 /*   public boolean addUser(User user) {
        String sql = "INSERT INTO users (prenom, nom, email, password_hash, role, created_at, last_login, is_active, birth_date, phone,pseudo,estPaye) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getRole().name());

            pstmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));

            if (user.getLastLogin() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(user.getLastLogin()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setBoolean(8, user.isActive());

            if (user.getBirthDate() != null) {
                pstmt.setDate(9, Date.valueOf(user.getBirthDate()));
            } else {
                pstmt.setNull(9, Types.DATE);
            }

            pstmt.setString(10, user.getPhone());
            pstmt.setString(11, user.getPseudo());
            pstmt.setBoolean(12, user.isEstPaye());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }*/

    // 1️⃣ AJOUTER UN UTILISATEUR
    public boolean addUser2(User user) {
        String sql = "INSERT INTO users (prenom, nom, email, password_hash, role, created_at, last_login, is_active, birth_date, phone,pseudo,estPaye) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getPrenom());
            pstmt.setString(2, user.getNom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getRole().name());

            pstmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));

            if (user.getLastLogin() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(user.getLastLogin()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            pstmt.setBoolean(8, user.isActive());

            if (user.getBirthDate() != null) {
                pstmt.setDate(9, Date.valueOf(user.getBirthDate()));
            } else {
                pstmt.setNull(9, Types.DATE);
            }

            pstmt.setString(10, user.getPhone());
            pstmt.setString(11, user.getPseudo());
            pstmt.setBoolean(12, user.isEstPaye());

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
            pstmt.setInt(9, user.getId());
            pstmt.setString(10, user.getPseudo());
            pstmt.setBoolean(11, user.isEstPaye());
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

    // 7️⃣ RECHERCHER PAR EMAIL
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

    // 🔧 PRIVÉ : mettre à jour last_login
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 🔧 PRIVÉ : mapper ResultSet → User
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
 // 8️⃣ RECHERCHER PAR PSEUDO
    public User findByPseudo(String pseudo) {
        String sql = "SELECT * FROM users WHERE pseudo = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, pseudo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs); // ✅ réutilisation propre
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    public boolean updatePaymentStatus(int userId, boolean status) {
        // La requête SQL pour mettre à jour la colonne estPaye selon l'ID
        String sql = "UPDATE users SET estPaye = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            // Remplacement des paramètres (?)
            st.setBoolean(1, status);
            st.setInt(2, userId);

            // Exécute la mise à jour et vérifie si au moins une ligne a été modifiée
            int rowsUpdated = st.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            // Log de l'erreur pour le débogage
            System.err.println("Erreur lors de la mise à jour du statut de paiement : " + e.getMessage());
            return false;
        }
    }
}