package tn.farah.NetflixJava.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Category;

public class CategoryDAO {


	    private Connection connection;

	    public CategoryDAO(Connection connection) {
	        this.connection = connection;
	    }

	    public void save(Category category) throws SQLException {
	        String query = "INSERT INTO category ( nom) VALUES ( ?)";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	           
	            ps.setString(1, category.getName());
	            ps.executeUpdate();
	        }
	    }

	    public Category findById(int id) throws SQLException {
	        String query = "SELECT * FROM category WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, id);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    return new Category(rs.getInt("id"), rs.getString("nom"));
	                }
	            }
	        }
	        return null;
	    }

	    public List<Category> findAll() throws SQLException {
	        List<Category> categories = new ArrayList<>();
	        String query = "SELECT * FROM category ORDER BY nom ASC";
	        try (Statement st = connection.createStatement();
	             ResultSet rs = st.executeQuery(query)) {
	            while (rs.next()) {
	                categories.add(new Category(rs.getInt("id"), rs.getString("nom")));
	            }
	        }
	        return categories;
	    }

	    public void update(Category category) throws SQLException {
	        String query = "UPDATE category SET nom = ? WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setString(1, category.getName());
	            ps.setInt(2, category.getId());
	            ps.executeUpdate();
	        }
	    }

	    public void delete(int id) throws SQLException {
	        String query = "DELETE FROM category WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, id);
	            ps.executeUpdate();}
	        }
	    /**
	     * enregistrer dans tableau media_category
	     */
	    public void lierMedia(int mediaId, int categoryId) throws SQLException {
	        String sql = "INSERT INTO media_category (media_id, category_id) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, mediaId);
	            ps.setInt(2, categoryId);
	            ps.executeUpdate();
	}
	    }
	 


}

