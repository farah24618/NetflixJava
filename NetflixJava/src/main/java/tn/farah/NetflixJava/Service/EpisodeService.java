package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.util.List;
import tn.farah.NetflixJava.DAO.EpisodeDAO;
import tn.farah.NetflixJava.Entities.Episode;

public class EpisodeService {

    // Attribut privé pour la connexion
    private Connection connection;

    // Attribut privé pour accéder à la couche de données
    private final EpisodeDAO episodeDAO;

    /**
     * Constructeur : On initialise l'instance de EpisodeDAO avec la connexion.
     */
    public EpisodeService(Connection connection) {
        this.connection = connection;
        this.episodeDAO = new EpisodeDAO(connection);
    }

    // ─────────────────────────────────
    //  AJOUTER UN EPISODE
    // ─────────────────────────────────
    public int save(Episode episode) {

        // Règle métier n°1 : le titre ne doit pas être vide
        if (episode.getTitre() == null || episode.getTitre().trim().isEmpty()) {
            System.out.println("Erreur : le titre de l'épisode est obligatoire");
            return 0;
        }

        // Règle métier n°2 : le numéro d'épisode doit être positif
        if (episode.getNumeroEpisode() <= 0) {
            System.out.println("Erreur : le numéro d'épisode doit être supérieur à 0");
            return 0;
        }

        // Règle métier n°3 : la durée doit être positive
        if (episode.getDuree() <= 0) {
            System.out.println("Erreur : la durée doit être supérieure à 0");
            return 0;
        }

        // Règle métier n°4 : l'épisode ne doit pas déjà exister dans la saison
        // Utilisation de l'instance episodeDAO
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

    // ─────────────────────────────────
    //  MODIFIER UN EPISODE
    // ─────────────────────────────────
    public void update(Episode episode) {

        // Règle métier : vérifier que l'épisode existe avant de modifier
        Episode existing = episodeDAO.findById(episode.getId());
        if (existing == null) {
            System.out.println("Erreur : épisode introuvable");
            return;
        }

        // Règle métier : le titre ne doit pas être vide
        if (episode.getTitre() == null || episode.getTitre().trim().isEmpty()) {
            System.out.println("Erreur : le titre de l'épisode est obligatoire");
            return;
        }

        episodeDAO.update(episode);
    }

    // ─────────────────────────────────
    //  SUPPRIMER UN EPISODE
    // ─────────────────────────────────
    public void delete(int id) {

        // Règle métier : vérifier que l'épisode existe avant de supprimer
        Episode existing = episodeDAO.findById(id);
        if (existing == null) {
            System.out.println("Erreur : épisode introuvable");
            return;
        }

        episodeDAO.delete(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER TOUS LES EPISODES
    // ─────────────────────────────────
    public List<Episode> findAll() {
        return episodeDAO.findAll();
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER PAR ID
    // ─────────────────────────────────
    public Episode findById(int id) {
        return episodeDAO.findById(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER LES EPISODES D'UNE SAISON
    // ─────────────────────────────────
    public List<Episode> findBySaison(int saisonId) {
        List<Episode> episodes = episodeDAO.findBySaison(saisonId);

        if (episodes.isEmpty()) {
            System.out.println("Aucun épisode trouvé pour la saison " + saisonId);
        }

        return episodes;
    }

    // ─────────────────────────────────
    //  NOMBRE D'EPISODES D'UNE SAISON
    // ─────────────────────────────────
    public int countBySaison(int saisonId) {
        return episodeDAO.countBySaison(saisonId);
    }

    // ──────────────────────────────────────────────
    //  BINGE-WATCHING : épisode suivant
    // ──────────────────────────────────────────────
    public Episode getNextEpisode(int saisonId, int numeroEpisode) {
        Episode next = episodeDAO.findNextEpisode(saisonId, numeroEpisode);

        if (next == null) {
            System.out.println("Fin de la saison " + saisonId);
            return null;
        }

        return next;
    }

    // ──────────────────────────────────────────────
    //  VÉRIFIER SI UN EPISODE EXISTE
    // ──────────────────────────────────────────────
    public boolean exists(int id) {
        return episodeDAO.findById(id) != null;
    }

    // ──────────────────────────────────────────────
    //  DURÉE TOTALE D'UNE SAISON en minutes
    // ──────────────────────────────────────────────
    public int getDureeTotaleSaison(int saisonId) {
        List<Episode> episodes = episodeDAO.findBySaison(saisonId);
        int total = 0;

        for (Episode e : episodes) {
            total += e.getDuree();
        }

        return total;
    }
}