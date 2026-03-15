package tn.farah.NetflixJava.Service;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import tn.farah.NetflixJava.DAO.WarningDao;
import tn.farah.NetflixJava.Entities.ContientWarning;

public class WarningService {
    private WarningDao warningDao;

    public WarningService(Connection connection) {
        this.warningDao = new WarningDao(connection);
    }

    /**
     * Met à jour les warnings d'un film (remplace les anciens par les nouveaux)
     */
    public void updateFilmWarnings(int filmId, List<ContientWarning> warnings) throws SQLException {
        if (warnings == null) return;
        
        // On nettoie d'abord les anciens pour éviter les doublons
        warningDao.deleteAllForFilm(filmId);
        
        // On insère les nouveaux
        if (!warnings.isEmpty()) {
            warningDao.saveWarnings(filmId, warnings);
        }
    }

    public List<ContientWarning> getWarningsForFilm(int filmId) throws SQLException {
        return warningDao.getWarningsByFilm(filmId);
    }
}




