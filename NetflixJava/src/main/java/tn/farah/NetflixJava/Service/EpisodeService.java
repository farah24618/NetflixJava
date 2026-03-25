package tn.farah.NetflixJava.Service;

import java.util.List;

import tn.farah.NetflixJava.DAO.EpisodeDAO;
import tn.farah.NetflixJava.Entities.Episode;

public class EpisodeService {

	 // ─────────────────────────────────
    //  AJOUTER UN EPISODE
    // ─────────────────────────────────
    public static int save(Episode episode) {

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
        List<Episode> episodes = EpisodeDAO.findBySaison(episode.getSaisonId());
        for (Episode e : episodes) {
            if (e.getNumeroEpisode() == episode.getNumeroEpisode()) {
                System.out.println("Erreur : l'épisode " + episode.getNumeroEpisode()
                                   + " existe déjà dans cette saison");
                return 0;
            }
        }

        return EpisodeDAO.save(episode);
    }

    // ─────────────────────────────────
    //  MODIFIER UN EPISODE
    // ─────────────────────────────────
    public static void update(Episode episode) {

        // Règle métier : vérifier que l'épisode existe avant de modifier
        Episode existing = EpisodeDAO.findById(episode.getId());
        if (existing == null) {
            System.out.println("Erreur : épisode introuvable");
            return;
        }

        // Règle métier : le titre ne doit pas être vide
        if (episode.getTitre() == null || episode.getTitre().trim().isEmpty()) {
            System.out.println("Erreur : le titre de l'épisode est obligatoire");
            return;
        }

        EpisodeDAO.update(episode);
    }

    // ─────────────────────────────────
    //  SUPPRIMER UN EPISODE
    // ─────────────────────────────────
    public static void delete(int id) {

        // Règle métier : vérifier que l'épisode existe avant de supprimer
        Episode existing = EpisodeDAO.findById(id);
        if (existing == null) {
            System.out.println("Erreur : épisode introuvable");
            return;
        }

        EpisodeDAO.delete(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER TOUS LES EPISODES
    // ─────────────────────────────────
    public static List<Episode> findAll() {
        return EpisodeDAO.findAll();
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER PAR ID
    // ─────────────────────────────────
    public static Episode findById(int id) {
        return EpisodeDAO.findById(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER LES EPISODES D'UNE SAISON
    // ─────────────────────────────────
    public static List<Episode> findBySaison(int saisonId) {
        List<Episode> episodes = EpisodeDAO.findBySaison(saisonId);

        if (episodes.isEmpty()) {
            System.out.println("Aucun épisode trouvé pour la saison " + saisonId);
        }

        return episodes;
    }

    // ─────────────────────────────────
    //  NOMBRE D'EPISODES D'UNE SAISON
    // ─────────────────────────────────
    public static int countBySaison(int saisonId) {
        return EpisodeDAO.countBySaison(saisonId);
    }

    // ──────────────────────────────────────────────
    //  BINGE-WATCHING : épisode suivant
    //  Appelé à la fin d'un épisode
    //  Retourne null s'il n'y a plus d'épisode
    // ──────────────────────────────────────────────
    public static Episode getNextEpisode(int saisonId, int numeroEpisode) {
        Episode next = EpisodeDAO.findNextEpisode(saisonId, numeroEpisode);

        // Règle métier : s'il n'y a plus d'épisode dans cette saison
        // → retourner null (le Controller affichera "fin de saison")
        if (next == null) {
            System.out.println("Fin de la saison " + saisonId);
            return null;
        }

        return next;
    }

    // ──────────────────────────────────────────────
    //  VÉRIFIER SI UN EPISODE EXISTE
    // ──────────────────────────────────────────────
    public static boolean exists(int id) {
        return EpisodeDAO.findById(id) != null;
    }

    // ──────────────────────────────────────────────
    //  DURÉE TOTALE D'UNE SAISON en minutes
    //  Somme des durées de tous les épisodes
    // ──────────────────────────────────────────────
    public static int getDureeTotaleSaison(int saisonId) {
        List<Episode> episodes = EpisodeDAO.findBySaison(saisonId);
        int total = 0;

        for (Episode e : episodes) {
            total += e.getDuree();
        }

        return total;
    }
}
