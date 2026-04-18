package tn.farah.NetflixJava.Service;


import java.sql.Connection;
import java.util.List;

import tn.farah.NetflixJava.DAO.FavoriDAO;
import tn.farah.NetflixJava.Entities.Favori;

public class FavoriService {

    private FavoriDAO favoriDao;

    public FavoriService(Connection cnx) {
        favoriDao = new FavoriDAO(cnx);
    }

    public boolean ajouterFavori(Favori favori) {
        if (favori == null || favori.getUserId() <= 0 || favori.getMediaId() <= 0) {
            return false;
        }
        if (favoriDao.existe(favori.getUserId(), favori.getMediaId())) {
            return false; // déjà en favori
        }
        int id = favoriDao.save(favori);
        return id > 0;
    }

    public boolean supprimerFavori(int userId, int mediaId) {
        if (userId <= 0 || mediaId <= 0) return false;
        int rows = favoriDao.supprimerFavori(userId, mediaId);
        return rows > 0;
    }
    public boolean estFavori(int userId, int mediaId) {
        if (userId <= 0 || mediaId <= 0) return false;
        return favoriDao.existe(userId, mediaId);
    }

    public List<Favori> getFavorisByUser(int userId) {
        return favoriDao.getFavorisByUser(userId);
    }

    public Favori findByUserIdAndMediaId(int userId, int mediaId) {
        return favoriDao.findByUserIdAndMediaId(userId, mediaId);
    }
    public boolean exist(int userId,int mediaId) {
    	return favoriDao.existe(userId,mediaId );
    }
}