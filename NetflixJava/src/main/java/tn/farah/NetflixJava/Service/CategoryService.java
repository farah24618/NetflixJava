package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import tn.farah.NetflixJava.DAO.CategoryDAO;
import tn.farah.NetflixJava.Entities.Category;

public class CategoryService {
	
	    private CategoryDAO categoryDao;

	    public CategoryService(Connection connection) {
	        this.categoryDao = new CategoryDAO(connection);
	    }

	    public List<Category> getAllCategoriesSorted() throws SQLException {
	        // Logique métier : on s'assure que c'est toujours trié pour l'utilisateur
	        return categoryDao.findAll();
	    }

	    public void createCategory(String name) throws Exception {
	        // Validation métier
	        if (name == null || name.trim().isEmpty()) {
	            throw new Exception("Le nom de la catégorie ne peut pas être vide.");
	        }
	        
	        // Vérification de doublon (logique qu'on ne met pas dans le DAO)
	        if (exists(name)) {
	            throw new Exception("Cette catégorie existe déjà.");
	        }

	        categoryDao.save(new Category(0, name)); // L'ID sera géré par la DB
	    }

	    private boolean exists(String name) throws SQLException {
	        return categoryDao.findAll().stream()
	                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
	    }
	}


