package tn.farah.NetflixJava.Service;
import java.sql.Connection;
import java.util.List;

import tn.farah.NetflixJava.DAO.ActeurDAO;
import tn.farah.NetflixJava.Entities.Acteur;
public class ActeurService {

	    private ActeurDAO acteurDao;

	    public ActeurService(Connection conn) {
	        // Initialisation du DAO avec la connexion passée au service
	        this.acteurDao = new ActeurDAO(conn);
	    }

	    // 1️⃣ Ajouter un acteur avec validation
	    public boolean ajouterActeur(Acteur acteur) {
	        // Validation métier : nom et prénom ne doivent pas être vides
	        if (acteur == null || 
	            acteur.getNom() == null || acteur.getNom().trim().isEmpty() || 
	            acteur.getPrenom() == null || acteur.getPrenom().trim().isEmpty()) {
	            System.out.println("Données de l'acteur invalides.");
	            return false;
	        }
	        
	        return acteurDao.addActeur(acteur);
	    }

	    // 2️⃣ Modifier un acteur
	    public boolean modifierActeur(Acteur acteur) {
	        if (acteur == null || acteur.getId() <= 0) {
	            return false;
	        }
	        return acteurDao.updateActeur(acteur);
	    }

	    // 3️⃣ Supprimer un acteur par son ID
	    public boolean supprimerActeur(int id) {
	        if (id <= 0) return false;
	        return acteurDao.deleteActeur(id);
	    }

	    // 4️⃣ Récupérer tous les acteurs
	    public List<Acteur> getAllActeurs() {
	        return acteurDao.getAllActeurs();
	    }

	    // 5️⃣ Trouver un acteur par son ID
	    public Acteur getActeurById(int id) {
	        if (id <= 0) return null;
	        return acteurDao.getActeurById(id);
	    }

	    // 6️⃣ Rechercher des acteurs par nom
	    public List<Acteur> rechercherParNom(String nom) {
	        if (nom == null || nom.trim().isEmpty()) {
	            return acteurDao.getAllActeurs();
	        }
	        return acteurDao.findByNom(nom);
	    }
	}

