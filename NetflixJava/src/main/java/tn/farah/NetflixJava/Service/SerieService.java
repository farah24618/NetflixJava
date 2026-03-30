package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.farah.NetflixJava.DAO.CategoryDAO;
import tn.farah.NetflixJava.DAO.FilmDao;
import tn.farah.NetflixJava.DAO.SerieDAO;
import tn.farah.NetflixJava.DAO.WarningDao;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Warning;

public class SerieService {

	 private SerieDAO serieDao;
	    private Connection connection;
	    private CategoryDAO categoryDao;
	    private WarningDao warningDao;

	    public SerieService(Connection connection) {
	        this.connection = connection;
	        this.serieDao = new SerieDAO(connection);
	        this.categoryDao = new CategoryDAO(connection);
	        this.warningDao=new WarningDao(connection);


	    }

	    /**
	     * Enregistre un film complet avec ses catégories et warnings.
	     * Transactionnel : Tout passe ou tout échoue.
	     */
	    public void enregistrerSerie(Serie film) throws Exception {
	        try {
	            connection.setAutoCommit(false);

	            // 1. Sauvegarder le Film (et le Media via l'ID généré)
	            serieDao.create(film);

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
	            /*if (film.getGenre() != null) {
	                    warningDao.lierMedia(idGenere, .getId());
	                    warningDao.save(warn);

	                }
	            }*/

	            connection.commit();
	        } catch (SQLException e) {
	            connection.rollback();
	            throw e;
	        }
	    }
	    /**
	     * Récupère tous les films triés par date
	     */
	    public List<Serie> getAllFilmsSorted() throws SQLException {
	        return serieDao.findAll(); // Assure-toi d'avoir mis ORDER BY dans le DAO
	    }

	    /**
	     * Recherche par titre
	     */
	    public List<Serie> searchByTitle(String title) throws SQLException {
	        return serieDao.findByTitle(title);
	    }

	    /**
	     * Filtrage par plusieurs catégories
	     */
	    public List<Serie> filterByCategories(List<Category> selectedCategories) throws SQLException {
	        if (selectedCategories == null || selectedCategories.isEmpty()) {
	            return getAllFilmsSorted();
	        }
	        List<Integer> ids = selectedCategories.stream()
	                                               .map(Category::getId)
	                                               .collect(Collectors.toList());
	        return serieDao.findByManyCategories(ids);
	    }

	    // --- Méthodes privées pour les tables de jointure ---

	    private void lierFilmACategorie(int serieId, int catId) throws SQLException {
	        String sql = "INSERT INTO liaison_serie-category (id_serie,id_ category) VALUES (?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setInt(1, serieId);
	            ps.setInt(2, catId);
	            ps.executeUpdate();
	        }
	    }

	    
	   
	    public Map<String, List<Serie>> getAllFilmsByCategory() throws SQLException {
	        return serieDao.findAllGroupedByCategory();
	    }

	    public Serie findById(int mediaId) {
	        try {
	            return serieDao.findById(mediaId);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	    public Serie findByEpisodeId(int episodeId) {
	        try {
	            return serieDao.findByEpisodeId(episodeId);
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
}