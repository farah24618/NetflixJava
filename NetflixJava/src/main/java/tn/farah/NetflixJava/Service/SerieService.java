package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.SerieDAO;
import tn.farah.NetflixJava.Entities.Serie;

import java.util.List;

public class SerieService {

    public boolean ajouterSerie(Serie serie) {
        if (serie == null) {
            return false;
        }

        if (serie.getTitre() == null || serie.getTitre().trim().isEmpty()) {
            return false;
        }

        if (serie.getGenre() == null || serie.getGenre().trim().isEmpty()) {
            return false;
        }

        if (serie.getDureeIntro() < 0) {
            return false;
        }

        int id = SerieDAO.save(serie);
        return id > 0;
    }

    public boolean modifierSerie(Serie serie) {
        if (serie == null) {
            return false;
        }

        if (serie.getId() <= 0) {
            return false;
        }

        if (serie.getTitre() == null || serie.getTitre().trim().isEmpty()) {
            return false;
        }

        if (serie.getGenre() == null || serie.getGenre().trim().isEmpty()) {
            return false;
        }

        if (serie.getDureeIntro() < 0) {
            return false;
        }

        int rows = SerieDAO.update(serie);
        return rows > 0;
    }

    public boolean supprimerSerie(int id) {
        if (id <= 0) {
            return false;
        }

        int rows = SerieDAO.delete(id);
        return rows > 0;
    }

    public Serie getSerieById(int id) {
        if (id <= 0) {
            return null;
        }

        return SerieDAO.findById(id);
    }

    public List<Serie> getAllSeries() {
        return SerieDAO.findAll();
    }
}
