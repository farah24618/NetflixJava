package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {

    private static Connection conn = ConxDB.getInstance();

    public static List<Commentaire> findByMedia(int mediaId, String mediaType) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE media_id = ? AND media_type = ? ORDER BY date_commentaire DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, mediaId);
            ps.setString(2, mediaType);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setMediaId(rs.getInt("media_id"));
                c.setMediaType(rs.getString("media_type"));
                c.setUserId(rs.getInt("user_id"));
                c.setUsername(rs.getString("username"));
                c.setContenu(rs.getString("contenu"));
                c.setLikes(rs.getInt("likes"));
                c.setSpoiler(rs.getBoolean("spoiler"));

                Timestamp ts = rs.getTimestamp("date_commentaire");
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

        String sql = "INSERT INTO commentaire (media_id, media_type, user_id, username, contenu, spoiler, likes, date_commentaire) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, commentaire.getMediaId());
            ps.setString(2, commentaire.getMediaType());
            ps.setInt(3, commentaire.getUserId());
            ps.setString(4, commentaire.getUsername());
            ps.setString(5, commentaire.getContenu());
            ps.setBoolean(6, commentaire.isSpoiler());
            ps.setInt(7, 0);

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
        String sql = "UPDATE commentaire SET likes = likes + 1 WHERE id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, commentaireId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}