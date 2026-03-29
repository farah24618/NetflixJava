package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.CommentaireDAO;
import tn.farah.NetflixJava.Entities.Commentaire;

import java.util.List;

public class CommentaireService {

    public List<Commentaire> getCommentairesByMedia(int mediaId, String mediaType) {
        return CommentaireDAO.findByMedia(mediaId, mediaType);
    }

    public boolean ajouterCommentaire(Commentaire commentaire) {
        if (commentaire == null) {
            return false;
        }

        if (commentaire.getMediaId() <= 0) {
            return false;
        }

        if (commentaire.getMediaType() == null || commentaire.getMediaType().trim().isEmpty()) {
            return false;
        }

        if (commentaire.getContenu() == null || commentaire.getContenu().trim().isEmpty()) {
            return false;
        }

        int id = CommentaireDAO.save(commentaire);
        return id > 0;
    }

    public void ajouterLike(int commentaireId) {
        CommentaireDAO.incrementLike(commentaireId);
    }
}