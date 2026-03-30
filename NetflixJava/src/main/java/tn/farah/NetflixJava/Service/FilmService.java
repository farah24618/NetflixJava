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
	        try {
	            connection.setAutoCommit(false);

	            // 1. Sauvegarder le Film (et le Media via l'ID généré)
	            filmDao.create(film);

	            int idGenere = film.getId();

	            // 2. On utilise la liste INTERNE du film !
	            if (film.getGenres() != null) {
	                for (Category cat : film.getGenres()) {
	                    categoryDao.lierMedia(idGenere, cat.getId());
	                    categoryDao.save(cat);
	                }
	            }

	            // On fait pareil pour les warnings s'ils sont aussi dans l'objet Film
	            if (film.getWarnings() != null) {
	                for (Warning warn : film.getWarnings()) {
	                    warningDao.lierMedia(idGenere, warn.getId());
	                    warningDao.save(warn);

	                }
	            }

	            connection.commit();
	        } catch (SQLException e) {
	            connection.rollback();
	            throw e;
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

}
