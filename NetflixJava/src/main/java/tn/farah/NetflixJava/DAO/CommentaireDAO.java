package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.utils.ConxDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {

    private static Connection conn = ConxDB.getInstance();

    public static List<Commentaire> findByMedia(int mediaId) {
        List<Commentaire> commentaires = new ArrayList<>();
     // Make sure there is a space before FROM and JOIN
        String sql = "SELECT c.*, u.nom " +
                     "FROM comment c " + 
                     "JOIN users u ON c.user_id = u.id " + 
                     "WHERE c.media_id = ? " + 
                     "ORDER BY c.date_publication DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, mediaId);
           

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setMediaId(rs.getInt("media_id"));
                //c.setMediaType(rs.getString("media_type"));
                c.setUserId(rs.getInt("user_id"));
                c.setUsername(rs.getString("nom"));
                c.setContenu(rs.getString("contenu"));
                c.setLikes(rs.getInt("likes"));
                c.setSpoiler(rs.getBoolean("contient_spoils"));

                Timestamp ts = rs.getTimestamp("date_publication");
                if (ts != null) {
                    c.setDateCommentaire(ts.toLocalDateTime());
                }

                commentaires.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commentaires;
    }

    public static int save(Commentaire commentaire) {
        int commentaireId = 0;

        String sql = "INSERT INTO comment (media_id, user_id, contenu, contient_spoils, likes, date_publication) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";

        try {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, commentaire.getMediaId());
            
            ps.setInt(2, commentaire.getUserId());
           
            ps.setString(3, commentaire.getContenu());
            ps.setBoolean(4, commentaire.isSpoiler());
            ps.setInt(5, 0);
           

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                commentaireId = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return commentaireId;
    }

    public static void incrementLike(int commentaireId) {
        String sql = "UPDATE comment SET likes = likes + 1 WHERE id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, commentaireId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}