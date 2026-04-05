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

    // Ajouter un favori
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

    // Supprimer un favori par userId + mediaId
    public boolean supprimerFavori(int userId, int mediaId) {
        if (userId <= 0 || mediaId <= 0) return false;
        int rows = favoriDao.supprimerFavori(userId, mediaId);
        return rows > 0;
    }

    // Vérifier si un média est déjà en favori
    public boolean estFavori(int userId, int mediaId) {
        if (userId <= 0 || mediaId <= 0) return false;
        return favoriDao.existe(userId, mediaId);
    }

    // Récupérer les favoris d'un utilisateur
    public List<Favori> getFavorisByUser(int userId) {
        return favoriDao.getFavorisByUser(userId);
    }

    // Récupérer un favori par userId + mediaId
    public Favori findByUserIdAndMediaId(int userId, int mediaId) {
        return favoriDao.findByUserIdAndMediaId(userId, mediaId);
    }
    public boolean exist(int userId,int mediaId) {
    	return favoriDao.existe(userId,mediaId );
    }
}