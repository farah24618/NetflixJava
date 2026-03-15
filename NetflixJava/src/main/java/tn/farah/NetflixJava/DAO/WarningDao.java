package tn.farah.NetflixJava.DAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.ContientWarning;
public class WarningDao {


	
	    private Connection connection;

	    public WarningDao(Connection connection) {
	        this.connection = connection;
	    }

	    // Sauvegarder la liste des enums pour un film
	    public void saveWarnings(int filmId, List<ContientWarning> warnings) throws SQLException {
	        String query = "INSERT INTO film_warnings (film_id, warning_name) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            for (ContientWarning w : warnings) {
	                ps.setInt(1, filmId);
	                ps.setString(2, w.name()); // Convertit l'Enum en String
	                ps.addBatch();
	            }
	            ps.executeBatch();
	        }
	    }

	    // Récupérer les enums depuis la base
	    public List<ContientWarning> getWarningsByFilm(int filmId) throws SQLException {
	        List<ContientWarning> warnings = new ArrayList<>();
	        String query = "SELECT warning_name FROM film_warnings WHERE film_id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, filmId);
	            try (ResultSet rs = ps.executeQuery()) {
	                while (rs.next()) {
	                    // Convertit le String de la DB en objet Enum Java
	                    warnings.add(ContientWarning.valueOf(rs.getString("warning_name")));
	                }
	            }
	        }
	        return warnings;
	    }

	    public void deleteAllForFilm(int filmId) throws SQLException {
	        String query = "DELETE FROM film_warnings WHERE film_id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, filmId);
	            ps.executeUpdate();
	        }
	    }
	

}
