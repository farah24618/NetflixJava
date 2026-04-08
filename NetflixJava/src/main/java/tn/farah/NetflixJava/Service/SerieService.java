package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.farah.NetflixJava.DAO.CategoryDAO;
import tn.farah.NetflixJava.DAO.SerieDAO;
import tn.farah.NetflixJava.DAO.WarningDao;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Episode;
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
        this.warningDao = new WarningDao(connection);
    }

    public void enregistrerSerie(Serie serie) throws SQLException {
    	 boolean previousAutoCommit = connection.getAutoCommit();
        try {
        
	            connection.setAutoCommit(false);
			serieDao.create(serie);
			  int idGenere = serie.getId();
		
			

        if (serie.getGenres() != null) {
            for (Category cat : serie.getGenres()) {
                
					categoryDao.lierMedia(idGenere, cat.getId());
				
            }
        }

        if (serie.getWarnings() != null) {
            for (Warning warn : serie.getWarnings()) {
                
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

    public void updateSerie(Serie serie) {
        serieDao.update(serie);
    }

    public void deleteSerie(int id) {
        serieDao.delete(id);
    }

    public List<Serie> getAllSeries() {
        return serieDao.findAll();
    }

    public List<Serie> searchByTitle(String title) {
        return serieDao.findByTitle(title);
    }

    public List<Serie> filterByCategories(List<Category> selectedCategories) {
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return getAllSeries();
        }
        List<Integer> ids = selectedCategories.stream()
                                               .map(Category::getId)
                                               .collect(Collectors.toList());
        return serieDao.findByManyCategories(ids);
    }

    public Map<String, List<Serie>> getAllSeriesByCategory() {
        return serieDao.findAllGroupedByCategory();
    }

    public Serie findById(int id) {
        return serieDao.findById(id);
    }

    public Serie findByEpisodeId(int episodeId) {
        return serieDao.findByEpisodeId(episodeId);
    }

    public List<Serie> findByYear(int year) {
        return serieDao.findByYear(year);
    }

    /**
     * Retourne le nombre d'épisodes dans une saison donnée.
     */
    public int countEpisodesBySaison(int saisonId) {
        return serieDao.countEpisodesBySaison(saisonId);
    }
    public List<Episode> findEpisodeBySaison(int saisonId) {
        // Vous pouvez ajouter de la logique métier ici si nécessaire avant d'appeler le DAO
        // Par exemple : vérifier que saisonId est > 0
        if (saisonId <= 0) {
            throw new IllegalArgumentException("L'ID de la saison doit être supérieur à 0");
        }
        
        return serieDao.findEpisodeBySaison(saisonId);
    }
}