package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.util.List;
import tn.farah.NetflixJava.DAO.SaisonDAO;
import tn.farah.NetflixJava.Entities.Saison;

public class SaisonService {

    // Attribut pour accéder aux données
    private final SaisonDAO saisonDAO;

    // Constructeur pour initialiser le DAO
    public SaisonService(Connection cnx) {
        this.saisonDAO = new SaisonDAO(cnx);
    }

    // ─────────────────────────────────
    //  AJOUTER UNE SAISON
    // ─────────────────────────────────
    public int save(Saison saison) {

        // Règle métier n°1 : le numéro de saison doit être positif
        if (saison.getNumeroSaison() <= 0) {
            System.out.println("Erreur : le numéro de saison doit être supérieur à 0");
            return 0;
        }

        // Règle métier n°2 : la saison ne doit pas déjà exister
        List<Saison> saisons = saisonDAO.findBySerie(saison.getIdSerie());
        for (Saison s : saisons) {
            if (s.getNumeroSaison() == saison.getNumeroSaison()) {
                System.out.println("Erreur : la saison " + saison.getNumeroSaison() + " existe déjà");
                return 0;
            }
        }

        return saisonDAO.save(saison);
    }

    // ─────────────────────────────────
    //  MODIFIER UNE SAISON
    // ─────────────────────────────────
    public void update(Saison saison) {

        // Règle métier : vérifier que la saison existe avant de modifier
        Saison existing = saisonDAO.findById(saison.getId());
        if (existing == null) {
            System.out.println("Erreur : saison introuvable");
            return;
        }

        saisonDAO.update(saison);
    }

    // ─────────────────────────────────
    //  SUPPRIMER UNE SAISON
    // ─────────────────────────────────
    public void delete(int id) {

        // Règle métier : vérifier que la saison existe avant de supprimer
        Saison existing = saisonDAO.findById(id);
        if (existing == null) {
            System.out.println("Erreur : saison introuvable");
            return;
        }

        saisonDAO.delete(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER TOUTES LES SAISONS
    // ─────────────────────────────────
    public List<Saison> findAll() {
        return saisonDAO.findAll();
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER PAR ID
    // ─────────────────────────────────
    public Saison findById(int id) {
        return saisonDAO.findById(id);
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER LES SAISONS D'UNE SÉRIE
    // ─────────────────────────────────
    public List<Saison> findBySerie(int idSerie) {

        // Règle métier : retourner les saisons triées par numéro
        List<Saison> saisons = saisonDAO.findBySerie(idSerie);

        if (saisons.isEmpty()) {
            System.out.println("Aucune saison trouvée pour la série " + idSerie);
        }

        return saisons;
    }

    // ─────────────────────────────────
    //  NOMBRE DE SAISONS D'UNE SÉRIE
    // ─────────────────────────────────
    public int countBySerie(int idSerie) {
        return saisonDAO.countBySerie(idSerie);
    }

    // ──────────────────────────────────────────────
    //  VÉRIFIER SI UNE SAISON EXISTE
    // ──────────────────────────────────────────────
    public boolean exists(int id) {
        return saisonDAO.findById(id) != null;
    }

    // ──────────────────────────────────────────────
    //  RÉCUPÉRER LA DERNIÈRE SAISON D'UNE SÉRIE
    // ──────────────────────────────────────────────
    public Saison getLastSaison(int idSerie) {
        List<Saison> saisons = saisonDAO.findBySerie(idSerie);

        if (saisons.isEmpty()) {
            return null;
        }

        // La liste est déjà triée par numeroSaison ASC dans le DAO
        return saisons.get(saisons.size() - 1);
    }

    // ──────────────────────────────────────────────
    //  RÉCUPÉRER LA SAISON PAR ID EPISODE
    // ──────────────────────────────────────────────
    public Saison getSaisonbyEpisodeId(int idEp) {
    	
        return saisonDAO.getSaisonbyIdEpidsode(idEp);
    }
    public int findFirstSeasonIdBySerie(int serieId) {
    	return saisonDAO.findFirstSeasonIdBySerie(serieId);
    }
}