package tn.farah.NetflixJava.DAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Commentaire;

public class CommentaireDAO {
	//hedhi

	
	    private Connection connection;

	    public CommentaireDAO(Connection connection) {
	        this.connection = connection;
	    }

	    public void create(Commentaire comment) throws SQLException {
	        String query = "INSERT INTO commentaires (contenu, date_pub, id_user, id_media, est_signale, contient_spoils) " +
	                       "VALUES (?, ?, ?, ?, ?, ?)";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setString(1, comment.getContenu());
	            ps.setTimestamp(2, Timestamp.valueOf(comment.getDatePublication()));
	            ps.setInt(3, comment.getIdUser());
	            ps.setInt(4, comment.getIdMedia());
	            ps.setBoolean(5, comment.isEstSignale());
	            ps.setBoolean(6, comment.isContientSpoils());
	            ps.executeUpdate();
	        }
	    }

	    // Récupérer tous les commentaires d'un média (Film/Série)
	    public List<Commentaire> findByMedia(int idMedia) throws SQLException {
	        List<Commentaire> comments = new ArrayList<>();
	        String query = "SELECT * FROM commentaires WHERE id_media = ? ORDER BY date_pub DESC";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, idMedia);
	            try (ResultSet rs = ps.executeQuery()) {
	                while (rs.next()) {
	                    comments.add(new Commentaire(
	                        rs.getInt("id"),
	                        rs.getString("contenu"),
	                        rs.getTimestamp("date_pub").toLocalDateTime(),
	                        rs.getInt("id_user"),
	                        rs.getInt("id_media"),
	                        rs.getBoolean("est_signale"),
	                        rs.getBoolean("contient_spoils")
	                    ));
	                }
	            }
	        }
	        return comments;
	    }

	    // Signaler un commentaire (Update spécifique)
	    public void signaler(int idCommentaire) throws SQLException {
	        String query = "UPDATE commentaires SET est_signale = true WHERE id = ?";
	        try (PreparedStatement ps = connection.prepareStatement(query)) {
	            ps.setInt(1, idCommentaire);
	            ps.executeUpdate();
	        }
	    }
	

}
