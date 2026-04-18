package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.CommentaireDAO;
import tn.farah.NetflixJava.Entities.Commentaire;

import java.sql.Connection;
import java.util.List;

public class CommentaireService {

    private  CommentaireDAO commentaireDAO ;
    
    public CommentaireService (Connection cnx) {
    	commentaireDAO=new CommentaireDAO(cnx);
    }

    public List<Commentaire> getCommentairesByMedia(int mediaId, String mediaType) {
        
        return commentaireDAO.findByMedia(mediaId);
    }

    public boolean ajouterCommentaire(Commentaire commentaire) {
        if (commentaire == null) {
            return false;
        }

        if (commentaire.getMediaId() <= 0) {
            return false;
        }

        

        if (commentaire.getContenu() == null || commentaire.getContenu().trim().isEmpty()) {
            return false;
        }

        int id = commentaireDAO.save(commentaire);
        return id > 0;
    }

    public void ajouterLike(int commentaireId) {
      
        commentaireDAO.incrementLike(commentaireId);
    }
    public Commentaire findById(int commentId) {
    	return commentaireDAO.findById(commentId);
    }
    public boolean update(Commentaire c) {
    	return commentaireDAO.update(c);
    }
    public boolean delet (int commentId) {
    	return commentaireDAO.deleteById(commentId);
    	
    }
    public List<Commentaire> findAll () {
    	return commentaireDAO.findAll();
    }
}