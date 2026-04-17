/*package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.Acteur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActeurDAO {
	

	    private Connection connection;

	    public ActeurDAO(Connection connection) {
	        this.connection = connection;
	    }

	    // 1️⃣ AJOUTER UN ACTEUR
	    public boolean addActeur(Acteur acteur) {
	        String sql = "INSERT INTO acteur (nom, prenom) VALUES (?, ?)";
	        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	            pstmt.setString(1, acteur.getNom());
	            pstmt.setString(2, acteur.getPrenom());
	            
	            int affectedRows = pstmt.executeUpdate();
	            
	            // Récupérer l'ID généré si nécessaire
	            if (affectedRows > 0) {
	                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
	                    if (generatedKeys.next()) {
	                        acteur.setId(generatedKeys.getInt(1));
	                    }
	                }
	                return true;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return false;
	    }

	    // 2️⃣ RÉCUPÉRER UN ACTEUR PAR ID
	    public Acteur getActeurById(int id) {
	        String sql = "SELECT * FROM acteur WHERE id = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setInt(1, id);
	            try (ResultSet rs = pstmt.executeQuery()) {
	                if (rs.next()) return mapResultSetToActeur(rs);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // 3️⃣ RÉCUPÉRER TOUS LES ACTEURS
	    public List<Acteur> getAllActeurs() {
	        List<Acteur> acteurs = new ArrayList<>();
	        String sql = "SELECT * FROM acteur";
	        try (Statement stmt = connection.createStatement();
	             ResultSet rs = stmt.executeQuery(sql)) {
	            while (rs.next()) {
	                acteurs.add(mapResultSetToActeur(rs));
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return acteurs;
	    }

	    // 4️⃣ METTRE À JOUR UN ACTEUR
	    public boolean updateActeur(Acteur acteur) {
	        String sql = "UPDATE acteur SET nom = ?, prenom = ? WHERE id = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setString(1, acteur.getNom());
	            pstmt.setString(2, acteur.getPrenom());
	            pstmt.setInt(3, acteur.getId());

	            return pstmt.executeUpdate() > 0;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    // 5️⃣ SUPPRIMER UN ACTEUR
	    public boolean deleteActeur(int id) {
	        String sql = "DELETE FROM acteur WHERE id = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setInt(1, id);
	            return pstmt.executeUpdate() > 0;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    // 6️⃣ RECHERCHER PAR NOM (Similaire à findByEmail)
	    public List<Acteur> findByNom(String nom) {
	        List<Acteur> acteurs = new ArrayList<>();
	        String sql = "SELECT * FROM acteur WHERE nom LIKE ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            pstmt.setString(1, "%" + nom + "%");
	            try (ResultSet rs = pstmt.executeQuery()) {
	                while (rs.next()) {
	                    acteurs.add(mapResultSetToActeur(rs));
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return acteurs;
	    }

	    // 🔧 PRIVÉ : Mapping ResultSet -> Objet Acteur
	    private Acteur mapResultSetToActeur(ResultSet rs) throws SQLException {
	        return new Acteur(
	            rs.getInt("id"),
	            rs.getString("nom"),
	            rs.getString("prenom")
	        );
	    }
	}
*/
