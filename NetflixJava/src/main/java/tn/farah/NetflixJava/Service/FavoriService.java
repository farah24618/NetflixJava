package tn.farah.NetflixJava.Service;

import java.util.List;

import tn.farah.NetflixJava.DAO.FavoriDAO;
import tn.farah.NetflixJava.Entities.Favori;

public class FavoriService {

    // Ajouter un favori
    public boolean ajouterFavori(Favori favori) {

        // vérifier s'il existe déjà
        if ((favori == null) || favori.getUserId() <= 0 || favori.getMediaId() <= 0 || FavoriDAO.existe(favori.getUserId(), favori.getMediaId())) {
            return false;
        }

        int id = FavoriDAO.save(favori);
        return id > 0;
    }

    // Supprimer un favori
    public boolean supprimerFavori(int userId, int mediaId) {

        if (userId <= 0 || mediaId <= 0) {
            return false;
        }

        Favori favori = FavoriDAO.findByUserIdAndMediaId(userId, mediaId);

        if (favori == null) {
            return false;
        }

        int rows = FavoriDAO.delete(favori.getId());
        return rows > 0;
    }

    // Vérifier si un média est déjà en favori
    public boolean estFavori(int userId, int mediaId) {

        if (userId <= 0 || mediaId <= 0) {
            return false;
        }

        return FavoriDAO.existe(userId, mediaId);
    }

    // Récupérer les favoris d’un utilisateur
    public List<Favori> getFavorisByUser(int userId) {
        return FavoriDAO.getFavorisByUser(userId);
    }
}