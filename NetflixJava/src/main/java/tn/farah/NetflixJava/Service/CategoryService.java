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
	      
	        return categoryDao.findAll();
	    }

	    public void saveCategory(String name) throws Exception {
	       
	        if (name == null || name.trim().isEmpty()) {
	            throw new Exception("Le nom de la catégorie ne peut pas être vide.");
	        }

	        if (exists(name)) {
	            throw new Exception("Cette catégorie existe déjà.");
	        }

	        categoryDao.save(new Category(0, name)); 
	    }

	    private boolean exists(String name) throws SQLException {
	        return categoryDao.findAll().stream()
	                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
	    }
	    public void updateCategory(Category cat) throws Exception {
	        categoryDao.update(cat);
	    }
	    public void deleteCategory(int id) throws Exception {
	        categoryDao.delete(id);
	    }	    
	



}


