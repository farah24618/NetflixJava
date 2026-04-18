package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import tn.farah.NetflixJava.DAO.HistoryDAO;
import tn.farah.NetflixJava.Entities.History;

public class HistoryService {

    private HistoryDAO historyDAO;

    public HistoryService(Connection conn) {
        this.historyDAO = new HistoryDAO(conn);
    }

    public List<History> findByUser(int userId) {
        return historyDAO.findByUser(userId);
    }


    public History findByUserAndFilm(int userId, int filmId) {
        return historyDAO.findByUserAndFilm(userId, filmId);
    }

    public void saveProgressionFilm(int userId, int filmId, int tempsArret, boolean estTermine) {
        History existing = historyDAO.findByUserAndFilm(userId, filmId);
        if (existing != null) {
            existing.setTempsArret(tempsArret);
            existing.setEstTermine(estTermine);
            existing.setDateVisionnage(LocalDateTime.now());
            historyDAO.update(existing);
        } else {
            historyDAO.save(new History(userId, filmId, null, LocalDateTime.now(), tempsArret, estTermine));
        }
    }

    public int getResumePositionFilm(int userId, int filmId) {
        History h = historyDAO.findByUserAndFilm(userId, filmId);
        return h != null ? h.getTempsArret() : 0;
    }

    public boolean isFilmTermine(int userId, int filmId) {
        History h = historyDAO.findByUserAndFilm(userId, filmId);
        return h != null && h.getEstTermine();
    }

    public History findByUserAndEpisode(int userId, int episodeId) {
        return historyDAO.findByUserAndEpisode(userId, episodeId);
    }

    public void saveProgressionEpisode(int userId, int episodeId, int tempsArret, boolean estTermine) {
        History existing = historyDAO.findByUserAndEpisode(userId, episodeId);
        if (existing != null) {
            existing.setTempsArret(tempsArret);
            existing.setEstTermine(estTermine);
            existing.setDateVisionnage(LocalDateTime.now());
            historyDAO.update(existing);
        } else {
            historyDAO.save(new History(userId, null, episodeId, LocalDateTime.now(), tempsArret, estTermine));
        }
    }

    public int getResumePositionEpisode(int userId, int episodeId) {
        History h = historyDAO.findByUserAndEpisode(userId, episodeId);
        return h != null ? h.getTempsArret() : 0;
    }

    public boolean isEpisodeTermine(int userId, int episodeId) {
        History h = historyDAO.findByUserAndEpisode(userId, episodeId);
        return h != null && h.getEstTermine();
    }

    public List<History> getTop5MostWatched(int userId) {
        return historyDAO.findTop5MostWatched(userId);
    }

    public void delete(int historyId) {
        historyDAO.delete(historyId);
    }


    public int getRemainingSecondsFilm(int historyid) {
        var history = historyDAO.findById(historyid);
        if (history == null) return 0;
        
        int totalMinutes = historyDAO.findTempsTotalFilmByUserAndFilm(history.getIdUser(), history.getFilmId());
        int totalSeconds = totalMinutes * 60;
        
        return Math.max(0, totalSeconds - history.getTempsArret());
    }

    public int getRemainingSecondsSerie(int historyid) {
        var history = historyDAO.findById(historyid);
        if (history == null) return 0;
        
        int totalMinutes = historyDAO.findTempsTotaEpisodeByUserAndEpisode(history.getIdUser(), history.getEpisodeId());
        int totalSeconds = totalMinutes * 60;
        
        return Math.max(0, totalSeconds - history.getTempsArret());
    }

    public double getProgressPercentFilm(int historyId) {
        var history = historyDAO.findById(historyId);
        if (history == null) return 0.0;
        
        int totalMinutes = historyDAO.findTempsTotalFilmByUserAndFilm(history.getIdUser(), history.getFilmId());
        System.out.println("Durée totale en minutes : " + totalMinutes);
        
        if (totalMinutes <= 0) return 0.0;
        
        int totalSeconds = totalMinutes * 60;
        
        double prc = (double) history.getTempsArret() / totalSeconds;
        System.out.println("Pourcentage calculé : " + prc);
        
        return Math.min(1.0, prc);
    }

    public double getProgressPercentEpisode(int historyId) {
        var history = historyDAO.findById(historyId);
        if (history == null) return 0.0;
        
        int totalMinutes = historyDAO.findTempsTotaEpisodeByUserAndEpisode(history.getIdUser(), history.getEpisodeId());
        if (totalMinutes <= 0) return 0.0;
        
        int totalSeconds = totalMinutes * 60;
        
        double prc = (double) history.getTempsArret() / totalSeconds;
        System.out.println("Pourcentage calculé : " + prc);
        
        return Math.min(1.0, prc);
    }
   
}