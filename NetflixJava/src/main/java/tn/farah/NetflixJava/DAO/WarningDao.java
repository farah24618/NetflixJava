package tn.farah.NetflixJava.DAO;
import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import tn.farah.NetflixJava.Entities.Warning;
public class WarningDao {


	
	    private Connection connection;

	    public WarningDao(Connection connection) {
	        this.connection = connection;
	    }

	   
	    public List<Warning> findAll() throws SQLException {
	        List<Warning> list = new ArrayList<>();
	        String sql = "SELECT * FROM warnings";
	        try (Statement st = connection.createStatement();
	             ResultSet rs = st.executeQuery(sql)) {
	            while (rs.next()) {
	                list.add(new Warning(rs.getInt("id"), rs.getString("nom")));
	            }
	        }
	        return list;
	    }
	    public void save(Warning warning) throws SQLException {
	        String sql = "INSERT INTO warnings (nom) VALUES (?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	            ps.setString(1, warning.getNom());
	            ps.executeUpdate();
	            
	            // On récupère l'ID généré par la DB pour l'objet Java
	            try (ResultSet rs = ps.getGeneratedKeys()) {
	                if (rs.next()) warning.setId(rs.getInt(1));
	            }
	        }
	    }

	    // --- FIND BY ID ---
	    public Warning findById(int id) throws SQLException {
	        String sql = "SELECT * FROM warnings WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, id);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    return new Warning(rs.getInt("id"), rs.getString("nom"));
	                }
	            }
	        }
	        return null;
	    }

	    // --- UPDATE ---
	    public void update(Warning warning) throws SQLException {
	        String sql = "UPDATE warnings SET nom = ? WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, warning.getNom());
	            ps.setInt(2, warning.getId());
	            ps.executeUpdate();
	        }
	    }

	    // --- DELETE ---
	    public void delete(int id) throws SQLException {
	        String sql = "DELETE FROM warnings WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, id);
	            ps.executeUpdate();
	        }
	    }
	

	    // Récupérer les enums depuis la base
	   
	    /**
	     * enregistrer dans tableau media_warning
	     */

		public void lierMedia(int mediaId, int  warnId) throws SQLException{
			String sql = "INSERT INTO media_warning (media_id, warning_id) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, mediaId);
	            ps.setInt(2, warnId);
	            ps.executeUpdate();
			
		}
	

}}
