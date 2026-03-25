package tn.farah.NetflixJava.Service;
import java.util.List;

import tn.farah.NetflixJava.DAO.SaisonDAO;
import tn.farah.NetflixJava.Entities.Saison;

public class SaisonService {

    // ─────────────────────────────────
    //  AJOUTER UNE SAISON
    // ─────────────────────────────────
    public static int save(Saison saison) {

        // Règle métier n°1 : le numéro de saison doit être positif
        if (saison.getNumeroSaison() <= 0) {
            System.out.println("Erreur : le numéro de saison doit être supérieur à 0");
            return 0;
        }

        // Règle métier n°2 : la saison ne doit pas déjà exister
        List<Saison> saisons = SaisonDAO.findBySerie(saison.getIdSerie());
        for (Saison s : saisons) {
            if (s.getNumeroSaison() == saison.getNumeroSaison()) {
                System.out.println("Erreur : la saison " + saison.getNumeroSaison() + " existe déjà");
                return 0;
            }
        }

        return SaisonDAO.save(saison);
    }

    // ─────────────────────────────────
    //  MODIFIER UNE SAISON
    // ─────────────────────────────────
    public static void update(Saison saison) {

        // Règle métier : vérifier que la saison existe avant de modifier
        Saison existing = SaisonDAO.findById(saison.getId());
        if (existing == null) {
            System.out.println("Erreur : saison introuvable");
            return;
        }

        SaisonDAO.update(saison);
    }

    // ─────────────────────────────────
    //  SUPPRIMER UNE SAISON
    // ─────────────────────────────────
    public static void delete(int id) {

        // Règle métier : vérifier que la saison existe avant de supprimer
        Saison existing = SaisonDAO.findById(id);
        if (existing == null) {
            System.out.println("Erreur : saison introuvable");
            return;
        }

        SaisonDAO.delete(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER TOUTES LES SAISONS
    // ─────────────────────────────────
    public static List<Saison> findAll() {
        return SaisonDAO.findAll();
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER PAR ID
    // ─────────────────────────────────
    public static Saison findById(int id) {
        return SaisonDAO.findById(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER LES SAISONS D'UNE SÉRIE
    // ─────────────────────────────────
    public static List<Saison> findBySerie(int idSerie) {

        // Règle métier : retourner les saisons triées par numéro
        List<Saison> saisons = SaisonDAO.findBySerie(idSerie);

        if (saisons.isEmpty()) {
            System.out.println("Aucune saison trouvée pour la série " + idSerie);
        }

        return saisons;
    }

    // ─────────────────────────────────
    //  NOMBRE DE SAISONS D'UNE SÉRIE
    // ─────────────────────────────────
    public static int countBySerie(int idSerie) {
        return SaisonDAO.countBySerie(idSerie);
    }

    // ──────────────────────────────────────────────
    //  VÉRIFIER SI UNE SAISON EXISTE
    //  Utilisé dans le Controller avant d'afficher
    // ──────────────────────────────────────────────
    public static boolean exists(int id) {
        return SaisonDAO.findById(id) != null;
    }

    // ──────────────────────────────────────────────
    //  RÉCUPÉRER LA DERNIÈRE SAISON D'UNE SÉRIE
    //  Utile pour savoir où en est la série
    // ──────────────────────────────────────────────
    public static Saison getLastSaison(int idSerie) {
        List<Saison> saisons = SaisonDAO.findBySerie(idSerie);

        if (saisons.isEmpty()) {
			return null;
		}

        // La liste est déjà triée par numeroSaison ASC dans le DAO
        // donc la dernière saison est le dernier élément
        return saisons.get(saisons.size() - 1);
    }
}