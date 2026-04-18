package tn.farah.NetflixJava.Service;

import java.sql.*;




import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.farah.NetflixJava.DAO.CategoryDAO;
import tn.farah.NetflixJava.DAO.FilmDao;
import tn.farah.NetflixJava.DAO.WarningDao;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Warning;
public class FilmService {



	    private FilmDao filmDao;
	    private Connection connection;
	    private CategoryDAO categoryDao;
	    private WarningDao warningDao;

	    public FilmService(Connection connection) {
	        this.connection = connection;
	        this.filmDao = new FilmDao(connection);
	        this.categoryDao = new CategoryDAO(connection);
	        this.warningDao=new WarningDao(connection);


	    }

	    /**
	     * Enregistre un film complet avec ses catégories et warnings.
	     * Transactionnel : Tout passe ou tout échoue.
	     */
	    public void enregistrerFilm(Film film) throws Exception {
	        boolean previousAutoCommit = connection.getAutoCommit();
	        try {
	            connection.setAutoCommit(false);

	            // 1. Save the Film (and Media via the generated ID)
	            filmDao.create(film);
	            int idGenere = film.getId();

	            // 2. Link genres
	            if (film.getGenres() != null) {
	                for (Category cat : film.getGenres()) {
	                    categoryDao.lierMedia(idGenere, cat.getId());
	                   
	                }
	            }

	            // 3. Link warnings
	            if (film.getWarnings() != null) {
	                for (Warning warn : film.getWarnings()) {
	                    warningDao.lierMedia(idGenere, warn.getId());
	                    
	                }
	            }

	            connection.commit();

	        } catch (SQLException e) {
	            // Only rollback if autoCommit was actually disabled
	            if (!connection.getAutoCommit()) {
	                connection.rollback();
	            }
	            throw e;
	        } finally {
	            // Always restore the original autoCommit state
	            connection.setAutoCommit(previousAutoCommit);
	        }
	    }
	    /**
	     * Récupère tous les films triés par date
	     */
	    public List<Film> getAllFilmsSorted() throws SQLException {
	        return filmDao.findAll(); // Assure-toi d'avoir mis ORDER BY dans le DAO
	    }

	    /**
	     * Recherche par titre
	     */
	    public List<Film> searchByTitle(String title) throws SQLException {
	        return filmDao.findByTitle(title);
	    }

	    /**
	     * Filtrage par plusieurs catégories
	     */
	    public List<Film> filterByCategories(List<Category> selectedCategories) throws SQLException {
	        if (selectedCategories == null || selectedCategories.isEmpty()) {
	            return getAllFilmsSorted();
	        }
	        List<Integer> ids = selectedCategories.stream()
	                                               .map(Category::getId)
	                                               .collect(Collectors.toList());
	        return filmDao.findByManyCategories(ids);
	    }

	    // --- Méthodes privées pour les tables de jointure ---

	    private void lierFilmACategorie(int filmId, int catId) throws SQLException {
	        String sql = "INSERT INTO film_categories (film_id, category_id) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, filmId);
	            ps.setInt(2, catId);
	            ps.executeUpdate();
	        }
	    }

	    private void lierFilmAWarning(int filmId, Warning warning) throws SQLException {
	        String sql = "INSERT INTO film_warnings (film_id, warning_name) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, filmId);
	            ps.setString(2, warning.getNom());
	            ps.executeUpdate();
	        }
	    }
	   
	    public Map<String, List<Film>> getAllFilmsByCategory() throws SQLException {
	        return filmDao.findAllGroupedByCategory();
	    }
	    public Film getFilmById(int id) throws SQLException {
	        return filmDao.findById(id);
	    }

	    public Film findById(int mediaId) {
	        try {
	            return filmDao.findById(mediaId);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }

	    }
	    public void delete(int id) throws SQLException {
	    	filmDao.delete(id);
	    }
	    public void updateFilm(Film film) throws SQLException {
	    	Film Oldfilm =filmDao.findById(film.getId());
	    	 if (Oldfilm.getGenres() != null) {
	             for (Category cat : Oldfilm.getGenres()) {
	                 
	 					categoryDao.supprimerLiaison(Oldfilm.getId(),cat.getId());
	 				
	             }
	         }

	         if (Oldfilm.getWarnings() != null) {
	             for (Warning warn : Oldfilm.getWarnings()) {
	                 
	 					warningDao.supprimerLiaison(Oldfilm.getId(),warn.getId());
	 				
	             }
	         }
	    	filmDao.update(film);
	    	 if (film.getGenres() != null) {
	             for (Category cat : film.getGenres()) {
	                 
	 					categoryDao.lierMedia(film.getId(), cat.getId());
	 				
	             }
	         }

	         if (film.getWarnings() != null) {
	             for (Warning warn : film.getWarnings()) {
	                 
	 					warningDao.lierMedia(film.getId(), warn.getId());
	 				
	             }
	         }
	    }


	    public void incrementVues (int filmId) {
	filmDao.incrementerVues(filmId);}

	    public void update (Film film) throws SQLException {
	    	filmDao.update(film);
	    }


		public List<Film> findAll() throws SQLException {
			

			return filmDao.findAll();
		}

		public List<Film> findByTitle(String query) throws SQLException {
			
			return filmDao.findByTitle(query);
		}
}

