package tn.farah.NetflixJava.Service;

import java.sql.Connection;

import java.util.List;
import tn.farah.NetflixJava.DAO.SaisonDAO;
import tn.farah.NetflixJava.Entities.Saison;

public class SaisonService {

    private final SaisonDAO saisonDAO;

    public SaisonService(Connection cnx) {
        this.saisonDAO = new SaisonDAO(cnx);
    }

    public int save(Saison saison) {
       
        if (saison.getNumeroSaison() <= 0) {
            System.out.println("Erreur : le numéro de saison doit être supérieur à 0");
            return 0;
        }
        List<Saison> saisons = saisonDAO.findBySerieId(saison.getIdSerie());
        for (Saison s : saisons) {
            if (s.getNumeroSaison() == saison.getNumeroSaison()) {
                System.out.println("Erreur : la saison " + saison.getNumeroSaison() + " existe déjà pour cette série.");
                return 0;
            }
        }
        if (saison.getNom() == null || saison.getNom().isEmpty()) {
            saison.setNom("Saison " + saison.getNumeroSaison());
        }

        return saisonDAO.save(saison);
    }

    public void update(Saison saison) {
        Saison existing = saisonDAO.findById(saison.getId());
        if (existing == null) {
            System.out.println("Erreur : saison introuvable (ID: " + saison.getId() + ")");
            return;
        }

        saisonDAO.update(saison);
    }
    public void delete(int id) {
        if (!exists(id)) {
            System.out.println("Erreur : impossible de supprimer, saison introuvable.");
            return;
        }
        saisonDAO.delete(id);
    }

    public List<Saison> findAll() {
        return saisonDAO.findAll();
    }

    public Saison findById(int id) {
        return saisonDAO.findById(id);
    }

    public List<Saison> findBySerie(int idSerie) {
        List<Saison> saisons = saisonDAO.findBySerieId(idSerie);
        if (saisons.isEmpty()) {
            System.out.println("Aucune saison trouvée pour la série " + idSerie);
        }
        return saisons;
    }

    public int countBySerie(int idSerie) {
        return saisonDAO.countBySerie(idSerie);
    }

    public boolean exists(int id) {
        return saisonDAO.findById(id) != null;
    }

    public Saison getLastSaison(int idSerie) {
        List<Saison> saisons = saisonDAO.findBySerieId(idSerie);
        if (saisons.isEmpty()) {
            return null;
        }
      
        return saisons.get(saisons.size() - 1);
    }

    public Saison getSaisonbyEpisodeId(int idEp) {
        return saisonDAO.getSaisonbyIdEpidsode(idEp);
    }

    public int findFirstSeasonIdBySerie(int serieId) {
        return saisonDAO.findFirstSeasonIdBySerie(serieId);
    }

    public boolean deleteLastSaison(int serieId, int numeroSaison) {
        return saisonDAO.deleteLastSaison(serieId, numeroSaison);
    }
 

	public List<Saison> findBySerieId(int id) {
		
		return saisonDAO.findBySerieId(id);
	}

	public int getSerieIdBySaison(int saisonId) {
		
		return saisonDAO.getSerieIdBySaison(saisonId);
	}
}