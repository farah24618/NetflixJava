package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.utils.ConxDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {

    private Connection connection;

    public CommentaireDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Commentaire> findByMedia(int mediaId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT c.*, u.nom " +
                     "FROM comment c " + 
                     "JOIN users u ON c.user_id = u.id " + 
                     "WHERE c.media_id = ? " + 
                     "ORDER BY c.date_publication DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, mediaId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setMediaId(rs.getInt("media_id"));
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

    public int save(Commentaire commentaire) {
        int commentaireId = 0;

        String sql = "INSERT INTO comment (media_id, user_id, contenu, contient_spoils, likes, date_publication) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

    public void incrementLike(int commentaireId) {
        String sql = "UPDATE comment SET likes = likes + 1 WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, commentaireId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // ==========================================
    // MÉTHODES CRUD SUPPLÉMENTAIRES
    // ==========================================

    /**
     * Récupérer un commentaire spécifique par son ID
     */
    public Commentaire findById(int id) {
        Commentaire c = null;
        String sql = "SELECT c.*, u.nom " +
                     "FROM comment c " + 
                     "JOIN users u ON c.user_id = u.id " + 
                     "WHERE c.id = ?";
                     
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setMediaId(rs.getInt("media_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setUsername(rs.getString("nom"));
                c.setContenu(rs.getString("contenu"));
                c.setLikes(rs.getInt("likes"));
                c.setSpoiler(rs.getBoolean("contient_spoils"));
                c.setSignale(rs.getBoolean("est_signale"));
                Timestamp ts = rs.getTimestamp("date_publication");
                if (ts != null) {
                    c.setDateCommentaire(ts.toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return c;
    }

    /**
     * Récupérer tous les commentaires de la base de données
     */
    public List<Commentaire> findAll() {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT c.*, u.nom " +
                     "FROM comment c " + 
                     "JOIN users u ON c.user_id = u.id " + 
                     "ORDER BY c.date_publication DESC";
                     
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setMediaId(rs.getInt("media_id"));
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

    /**
     * Mettre à jour un commentaire existant (ex: modifier le texte ou le statut spoiler)
     * Retourne true si la mise à jour a réussi, false sinon.
     */
    public boolean update(Commentaire commentaire) {
        String sql = "UPDATE comment SET contenu = ?, contient_spoils = ? ,est_signale = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, commentaire.getContenu());
            ps.setBoolean(2, commentaire.isSpoiler());
            ps.setInt(3, commentaire.getId());
            ps.setBoolean(4, commentaire.isSignale());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Supprimer un commentaire par son ID
     * Retourne true si la suppression a réussi, false sinon.
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM comment WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    
}