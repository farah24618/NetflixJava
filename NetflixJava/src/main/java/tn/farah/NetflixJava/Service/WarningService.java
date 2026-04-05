package tn.farah.NetflixJava.Service;
import java.sql.Connection;

import java.sql.SQLException;
import java.util.List;

import tn.farah.NetflixJava.DAO.WarningDao;
import tn.farah.NetflixJava.Entities.Warning;

public class WarningService {
    private WarningDao warningDao;

    public WarningService(Connection connection) {
        this.warningDao = new WarningDao(connection);
      //hedhi
    }

    /**
     * Met à jour les warnings d'un film (remplace les anciens par les nouveaux)
     * @throws SQLException 
     */
   
    public List<Warning> getAllWarnings() throws SQLException {
        return warningDao.findAll();
    }

    
   
    
}




