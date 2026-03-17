package tn.farah.NetflixJava.Service;


import tn.farah.NetflixJava.DAO.RatingDao;
import tn.farah.NetflixJava.Entities.Rating;

import java.util.List;

public class RatingService {

    private RatingDao ratingDao;

    public RatingService(RatingDao ratingDao) {
        this.ratingDao = ratingDao;
    }

    public boolean addRating(Rating rating) {
        if (rating.getScore() < 1 || rating.getScore() > 5) {
            System.out.println("Erreur : La note doit être comprise entre 1 et 5 étoiles !");
            return false;
        }

        if (rating.getUserId() <= 0 || rating.getFilmId() <= 0) {
            System.out.println("Erreur : Utilisateur ou Film invalide.");
            return false;
        }

        return ratingDao.addRating(rating);
    }

    public List<Rating> getRatingsForFilm(int filmId) {
        if (filmId <= 0) {
            System.out.println("Erreur : ID de film invalide.");
            return null;
        }
        return ratingDao.getRatingsByFilmId(filmId);
    }

    public double getFilmAverage(int filmId) {
        if (filmId <= 0) {
            return 0.0;
        }
        
        double average = ratingDao.getAverageScoreForFilm(filmId);
        return Math.round(average * 10.0) / 10.0; 
    }
}