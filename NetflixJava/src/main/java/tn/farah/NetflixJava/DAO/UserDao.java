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
                if (rs.next()) return mapResultSetToUser1(rs);
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
            while (rs.next()) users.add(mapResultSetToUser1(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

 // 4️⃣ METTRE À JOUR UN UTILISATEUR
  /*  public boolean updateUser(User user) {
        // La requête SQL définie
        String sql = "UPDATE users SET prenom = ?, nom = ?, email = ?, password_hash = ?, role = ?, is_active = ?, birth_date = ?, phone = ?, pseudo = ?, estPaye = ? WHERE id = ?";
        
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
            pstmt.setString(9, user.getPseudo());  // Index 9 pour le pseudo
            pstmt.setBoolean(10, user.isEstPaye()); // Index 10 pour estPaye
            pstmt.setInt(11, user.getId());        // Index 11 pour le WHERE id

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }  }*/
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
                    User user = mapResultSetToUser1(rs);
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
                if (rs.next()) return mapResultSetToUser1(rs);
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
    private User mapResultSetToUser1(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setPrenom(rs.getString("prenom"));
        user.setNom(rs.getString("nom"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        
        // Conversion du rôle String (base) vers Enum (Java)
        String roleStr = rs.getString("role");
        user.setRole(UserRole.valueOf(roleStr.toUpperCase()));

        // is_active est un TinyInt(1) dans MySQL -> boolean en Java
        user.setActive(rs.getInt("is_active") == 1);
        
        // SUPPRIME OU COMMENTE CETTE LIGNE :
        // user.setPseudo(rs.getString("pseudo")); 
        
        return user;
    }
 // 8️⃣ RECHERCHER PAR PSEUDO
    public User findByPseudo(String pseudo) {
        String sql = "SELECT * FROM users WHERE pseudo = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            
            pstmt.setString(1, pseudo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser1(rs); // ✅ réutilisation propre
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
    public boolean updatePassword(String email, String hashedPass) {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, hashedPass);
            pstmt.setString(2, email);
            
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;}
        }
        
       //hehdi zedtha 
        private User mapResultSetToUser(ResultSet rs) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setPrenom(rs.getString("prenom"));
            user.setNom(rs.getString("nom"));
            user.setEmail(rs.getString("email"));
            user.setPasswordHash(rs.getString("password_hash"));
            
            String roleStr = rs.getString("role");
            user.setRole(UserRole.valueOf(roleStr.toUpperCase()));
            user.setActive(rs.getInt("is_active") == 1);

            // AJOUTE CES DEUX LIGNES :
            if (rs.getDate("birth_date") != null) user.setBirthDate(rs.getDate("birth_date").toLocalDate());
            user.setPhone(rs.getString("phone"));
            
            return user;
        }
            
            
            
            
            
            
            
            
        public boolean updateUser(User user) {
            // On ne garde que les colonnes essentielles pour éviter les erreurs SQL
            String sql = "UPDATE users SET prenom = ?, nom = ?, email = ?, role = ?, is_active = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, user.getPrenom());
                pstmt.setString(2, user.getNom());
                pstmt.setString(3, user.getEmail());
                pstmt.setString(4, user.getRole().name());
                pstmt.setBoolean(5, user.isActive());
                pstmt.setInt(6, user.getId());

                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) { 
                e.printStackTrace(); 
                return false; 
            }
        }
        
        
        
        
        
        
        
       
        
        
        
        
      
            
            
            
            
            
            
            
        
    
        
        
        
        
        
        
        
/*
public boolean addAuditLog(int adminId, String description) {
 String sql = "INSERT INTO audit_logs (admin_id, action_description, action_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
 try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
     pstmt.setInt(1, adminId);
     pstmt.setString(2, description);
     return pstmt.executeUpdate() > 0;
 } catch (SQLException e) {
     e.printStackTrace();
     return false;
 }
}*/

/*//--- MISE À JOUR DE LA MÉTHODE EXISTANTE POUR PLUS DE CLARTÉ ---
public List<String> getAdminLogs(int adminId) {
 List<String> logs = new ArrayList<>();
 // On récupère les actions triées par date la plus récente
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
 } catch (SQLException e) { 
     e.printStackTrace(); 
 }
 return logs;
}*/
        
        
        
        
        
        
        
        
        public boolean addAuditLog(int adminId, String description) {
            String sql = "INSERT INTO audit_logs (admin_id, action_description, action_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, adminId);
                pstmt.setString(2, description);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }







public List<String> getAdminLogs(int adminId) {
    List<String> logs = new ArrayList<>();
    // On récupère les actions triées par date la plus récente
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
    } catch (SQLException e) { 
        e.printStackTrace(); 
    }
    return logs;
}




}
        
        
        
        
        
        
        
        
        
        
        
        
        
        
