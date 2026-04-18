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
     
    }

   
    public List<Warning> getAllWarnings() throws SQLException {
        return warningDao.findAll();
    }
    public void save(Warning w) throws SQLException {
    	warningDao.save(w);
    }
    public void updateWarning(Warning w) throws SQLException {
        warningDao.update(w);
    }
    public void deleteWarning(int id) throws SQLException {
        warningDao.delete(id);
    }

    
   
    
}




