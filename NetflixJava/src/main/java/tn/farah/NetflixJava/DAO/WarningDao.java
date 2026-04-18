package tn.farah.NetflixJava.DAO;
import java.sql.*;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	        String sql = "SELECT * FROM content_warning";
	        try (Statement st = connection.createStatement();
	             ResultSet rs = st.executeQuery(sql)) {
	            while (rs.next()) {
	                list.add(new Warning(rs.getInt("id"), rs.getString("label")));
	            }
	        }
	        return list;
	    }
	    public void save(Warning warning) throws SQLException {
	        String sql = "INSERT INTO content_warning (label) VALUES (?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	            ps.setString(1, warning.getNom());
	            ps.executeUpdate();

	            try (ResultSet rs = ps.getGeneratedKeys()) {
	                if (rs.next()) {
						warning.setId(rs.getInt(1));
					}
	            }
	        }
	    }

	    public Warning findById(int id) throws SQLException {
	        String sql = "SELECT * FROM content_warning WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, id);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    return new Warning(rs.getInt("id"), rs.getString("label"));
	                }
	            }
	        }
	        return null;
	    }

	    public void update(Warning warning) throws SQLException {
	        String sql = "UPDATE content_warning SET label = ? WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, warning.getNom());
	            ps.setInt(2, warning.getId());
	            ps.executeUpdate();
	        }
	    }

	    public void delete(int id) throws SQLException {
	        String sql = "DELETE FROM content_warning WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, id);
	            ps.executeUpdate();
	        }
	    }


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


}
		


}
