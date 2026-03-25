package tn.farah.NetflixJava.Service;


import java.time.LocalDateTime;
import java.util.List;

import tn.farah.NetflixJava.DAO.HistoryDAO;
import tn.farah.NetflixJava.Entities.History;
import tn.farah.NetflixJava.utils.ConxDB;

public class HistoryService {
	    private static HistoryDAO historyDAO = new HistoryDAO(ConxDB.getInstance());

	    //  SAUVEGARDER / METTRE À JOUR
	    public static int save(History history) {
	        return HistoryDAO.save(history);
	    }

	    //  RÉCUPÉRER TOUT L'HISTORIQUE
	    public static List<History> findAll() {
	        return HistoryDAO.findAll();
	    }

	    //  HISTORIQUE D'UN UTILISATEUR
	    public static List<History> findByUser(int userId) {
	        return HistoryDAO.findByUser(userId);
	    }

	    //  TROUVER PAR ID
	    public static History findById(int id) {
	        return HistoryDAO.findById(id);
	    }

	    //  SUPPRIMER
	    public static void delete(int id) {
	        HistoryDAO.delete(id);
	    }

	    //  TOP 5 LES PLUS VUS
	    public static List<Object[]> getTop5MostWatched() {
	        return HistoryDAO.findTop5MostWatched();
	    }

	    //  ENREGISTRER LA PROGRESSION PENDANT LA LECTURE
	    //  Appelé toutes les X secondes depuis le player
	    // ──────────────────────────────────────────────────
	    public static void saveProgression(int idUser, int idMedia,
	                                       int tempsArret, int dureeTotale) {

	        // Règle métier : si > 90% regardé → marquer comme terminé
	        boolean estTermine = dureeTotale > 0
	                && tempsArret >= dureeTotale * 0.90;

	        // Cherche si une entrée existe déjà pour ce user + ce media
	        History existing = HistoryDAO.findByUserAndMedia(idUser, idMedia);

	        if (existing == null) {
	            // Première fois → on crée une nouvelle entrée
	            History newHistory = new History(
	                    idUser,
	                    idMedia,
	                    LocalDateTime.now(),
	                    tempsArret,
	                    estTermine
	            );
	            HistoryDAO.save(newHistory);
	        } else {
	            // Déjà vu → on met à jour la position
	            existing.setTempsArret(tempsArret);
	            existing.setDateVisionnage(LocalDateTime.now());
	            // Ne jamais repasser estTermine à false si déjà terminé
	            if (estTermine) {
					existing.setEstTermine(true);
				}
	            HistoryDAO.save(existing);
	        }
	    }

	    // ──────────────────────────────────────────────────
	    //  REPRISE INTELLIGENTE
	    //  Retourne la position en secondes pour reprendre
	    //  là où l'utilisateur s'est arrêté
	    // ──────────────────────────────────────────────────
	    public static int getResumePosition(int idUser, int idMedia) {
	        History h = HistoryDAO.findByUserAndMedia(idUser, idMedia);

	        // Jamais regardé ou déjà terminé → reprendre au début
	        if (h == null || h.getEstTermine()) {
				return 0;
			}

	        // Sinon → reprendre à la position sauvegardée
	        return h.getTempsArret();
	    }

	    // ──────────────────────────────────────────────────
	    //  STATUT D'UN MEDIA pour un utilisateur
	    //  Retourne "NON_VU", "EN_COURS" ou "TERMINE"
	    //  Utilisé pour les badges sur la page d'accueil
	    // ──────────────────────────────────────────────────
	    public static String getMediaStatus(int idUser, int idMedia) {
	        History h = HistoryDAO.findByUserAndMedia(idUser, idMedia);

	        if (h == null) {
				return "NON_VU";
			}
	        if (h.getEstTermine()) {
				return "TERMINE";
			}
	        if (h.getTempsArret() > 0) {
				return "EN_COURS";
			}

	        return "NON_VU";
	    }}
