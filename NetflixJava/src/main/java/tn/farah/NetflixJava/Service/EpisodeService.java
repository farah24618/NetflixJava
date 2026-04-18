package tn.farah.NetflixJava.Service;

import java.sql.Connection;

import java.util.List;
import tn.farah.NetflixJava.DAO.EpisodeDAO;
import tn.farah.NetflixJava.Entities.Episode;

public class EpisodeService {

    private Connection connection;

    private final EpisodeDAO episodeDAO;

    public EpisodeService(Connection connection) {
        this.connection = connection;
        this.episodeDAO = new EpisodeDAO(connection);
    }

    public int save(Episode episode) {

        if (episode.getTitre() == null || episode.getTitre().trim().isEmpty()) {
            System.out.println("Erreur : le titre de l'épisode est obligatoire");
            return 0;
        }

        if (episode.getNumeroEpisode() <= 0) {
            System.out.println("Erreur : le numéro d'épisode doit être supérieur à 0");
            return 0;
        }

        if (episode.getDuree() <= 0) {
            System.out.println("Erreur : la durée doit être supérieure à 0");
            return 0;
        }

        List<Episode> episodes = episodeDAO.findBySaison(episode.getSaisonId());
        for (Episode e : episodes) {
            if (e.getNumeroEpisode() == episode.getNumeroEpisode()) {
                System.out.println("Erreur : l'épisode " + episode.getNumeroEpisode()
                                   + " existe déjà dans cette saison");
                return 0;
            }
        }

        return episodeDAO.save(episode);
    }
    public void update(Episode episode) {
        Episode existing = episodeDAO.findById(episode.getId());
        if (existing == null) {
            System.out.println("Erreur : épisode introuvable");
            return;
        }

        if (episode.getTitre() == null || episode.getTitre().trim().isEmpty()) {
            System.out.println("Erreur : le titre de l'épisode est obligatoire");
            return;
        }

        episodeDAO.update(episode);
    }

    public void delete(int id) {

        Episode existing = episodeDAO.findById(id);
        if (existing == null) {
            System.out.println("Erreur : épisode introuvable");
            return;
        }

        episodeDAO.delete(id);
    }

    public List<Episode> findAll() {
        return episodeDAO.findAll();
    }
    public Episode findById(int id) {
        return episodeDAO.findById(id);
    }
    public List<Episode> findBySaison(int saisonId) {
        List<Episode> episodes = episodeDAO.findBySaison(saisonId);

        if (episodes.isEmpty()) {
            System.out.println("Aucun épisode trouvé pour la saison " + saisonId);
        }

        return episodes;
    }

    public int countBySaison(int saisonId) {
        return episodeDAO.countBySaison(saisonId);
    }

    public Episode getNextEpisode(int saisonId, int numeroEpisode) {
        Episode next = episodeDAO.findNextEpisode(saisonId, numeroEpisode);

        if (next == null) {
            System.out.println("Fin de la saison " + saisonId);
            return null;
        }

        return next;
    }

    public boolean exists(int id) {
        return episodeDAO.findById(id) != null;
    }

    public int getDureeTotaleSaison(int saisonId) {
        List<Episode> episodes = episodeDAO.findBySaison(saisonId);
        int total = 0;

        for (Episode e : episodes) {
            total += e.getDuree();
        }

        return total;
    }

    public void incrementerVues (int idEpisode) {
    	episodeDAO.incrementerVues(idEpisode);
    }

	public List<Episode> findBySaisonId(int id) {
	
		return episodeDAO.findBySaisonId(id);
	}

}