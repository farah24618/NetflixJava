package tn.farah.NetflixJava.DAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Category;

public class CategoryDAO {
	//hedhi

	
	    private Connection connection;

	    public CategoryDAO(Connection connection) {
	        this.connection = connection;
	    }

	    // CREATE : Ajouter une nouvelle catégorie
	    public void save(Category category) throws SQLException {
	        String query = "INSERT INTO category (id, name) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, category.getId());
	            ps.setString(2, category.getName());
	            ps.executeUpdate();
	        }
	    }

	    // READ : Récupérer une catégorie par son ID
	    public Category findById(int id) throws SQLException {
	        String query = "SELECT * FROM category WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, id);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next()) {
	                    return new Category(rs.getInt("id"), rs.getString("name"));
	                }
	            }
	        }
	        return null;
	    }

	    // READ ALL : Très utile pour remplir tes menus de navigation Netflix
	    public List<Category> findAll() throws SQLException {
	        List<Category> categories = new ArrayList<>();
	        String query = "SELECT * FROM category ORDER BY name ASC";
	        try (Statement st = connection.createStatement();
	             ResultSet rs = st.executeQuery(query)) {
	            while (rs.next()) {
	                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
	            }
	        }
	        return categories;
	    }

	    // UPDATE : Modifier le nom d'une catégorie
	    public void update(Category category) throws SQLException {
	        String query = "UPDATE category SET name = ? WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setString(1, category.getName());
	            ps.setInt(2, category.getId());
	            ps.executeUpdate();
	        }
	    }

	    // DELETE : Supprimer une catégorie
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
	        String sql = "INSERT INTO media_category (film_id, category_id) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, mediaId);
	            ps.setInt(2, categoryId);
	            ps.executeUpdate();
	}
	    }}

