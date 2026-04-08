package tn.farah.NetflixJava.Service;

import java.util.List;
import tn.farah.NetflixJava.DAO.SubtitleDAO;
import tn.farah.NetflixJava.Entities.Subtitle;

public class SubtitleService {

    private final SubtitleDAO subtitleDAO;

    public SubtitleService(SubtitleDAO subtitleDAO) {
        this.subtitleDAO = subtitleDAO;
    }

    public int addSubtitle(Subtitle subtitle) {
        // Validation de base
        if (subtitle.getLangage() == null || subtitle.getUrl() == null) {
            System.err.println("Données manquantes");
            return 0;
        }
        return subtitleDAO.save(subtitle);
    }

    public List<Subtitle> getSubtitlesForFilm(int filmId) {
        return subtitleDAO.findByFilm(filmId);
    }

    public List<Subtitle> getSubtitlesForEpisode(int episodeId) {
        return subtitleDAO.findByEpisode(episodeId);
    }

    public void deleteSubtitle(int id) {
        if (subtitleDAO.findById(id) != null) {
            subtitleDAO.delete(id);
        } else {
            System.err.println("Sous-titre introuvable");
        }
    }

    public List<Subtitle> getAll() {
        return subtitleDAO.findAll();
    }

    // Méthode utilitaire pour récupérer l'URL selon le type de média
    public String getUrlByCriteria(Integer filmId, Integer episodeId, String langage) {
        List<Subtitle> list;
        if (filmId != null && filmId > 0) {
            list = subtitleDAO.findByFilm(filmId);
        } else if (episodeId != null && episodeId > 0) {
            list = subtitleDAO.findByEpisode(episodeId);
        } else {
            return null;
        }

        return list.stream()
                .filter(s -> s.getLangage().equalsIgnoreCase(langage))
                .map(Subtitle::getUrl)
                .findFirst()
                .orElse(null);
    }
}