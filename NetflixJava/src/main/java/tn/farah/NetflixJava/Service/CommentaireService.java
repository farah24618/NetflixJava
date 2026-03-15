package tn.farah.NetflixJava.Service;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import tn.farah.NetflixJava.DAO.CommentaireDAO;
import tn.farah.NetflixJava.Entities.Commentaire;

public class CommentaireService {
	

	
	    private CommentaireDAO commentaireDao;

	    public CommentaireService(Connection connection) {
	        this.commentaireDao = new CommentaireDAO(connection);
	    }

	    /**
	     * Publie un nouveau commentaire avec validation
	     */
	    public void publierCommentaire(String contenu, int idUser, int idMedia, boolean containsSpoilers) throws Exception {
	        // Validation : Un commentaire ne peut pas être vide
	        if (contenu == null || contenu.trim().isEmpty()) {
	            throw new Exception("Le commentaire ne peut pas être vide.");
	        }

	        // Validation : Limite de caractères (ex: 500)
	        if (contenu.length() > 500) {
	            throw new Exception("Le commentaire est trop long (max 500 caractères).");
	        }

	        Commentaire nouveau = new Commentaire(
	            0, 
	            contenu, 
	            LocalDateTime.now(), 
	            idUser, 
	            idMedia, 
	            false, // Non signalé par défaut
	            containsSpoilers
	        );

	        commentaireDao.create(nouveau);
	    }

	    /**
	     * Récupère les commentaires d'un média en filtrant les signalements pour les utilisateurs normaux
	     */
	    public List<Commentaire> getCommentairesPourMedia(int idMedia, boolean isAdmin) throws SQLException {
	        List<Commentaire> tous = commentaireDao.findByMedia(idMedia);

	        if (isAdmin) {
	            return tous; // L'admin voit tout, même les messages signalés
	        }

	        // Un utilisateur normal ne voit pas les messages déjà signalés (modération automatique)
	        return tous.stream()
	                   .filter(c -> !c.isEstSignale())
	                   .collect(Collectors.toList());
	    }

	    /**
	     * Signaler un contenu inapproprié
	     */
	    public void signalerCommentaire(int idCommentaire) throws SQLException {
	        commentaireDao.signaler(idCommentaire);
	    }
	
}
