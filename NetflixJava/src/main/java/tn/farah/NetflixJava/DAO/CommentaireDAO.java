package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.Commentaire;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {

    private Connection conn;

    public CommentaireDAO(Connection conn) {
        this.conn = conn;
    }

    /**
     * Récupère les commentaires d'un film.
     * Utilise directement la colonne 'username' de la table 'comment'.
     */
    public List<Commentaire> findByFilmId(int filmId) {
        List<Commentaire> commentaires = new ArrayList<>();
        // On sélectionne directement depuis 'comment' car 'username' y est déjà
        String sql = "SELECT * FROM comment WHERE media_id = ? ORDER BY date_publication DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, filmId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setMediaId(rs.getInt("media_id"));
                c.setUserId(rs.getInt("user_id"));
                // On utilise la colonne username de la table comment
                c.setUsername(rs.getString("username")); 
                c.setContenu(rs.getString("contenu"));
                
                // On vérifie les deux colonnes possibles pour le flag signalement
                boolean isSpoiler = rs.getBoolean("contient_spoils") || rs.getBoolean("is_spoiler");
                c.setSpoiler(isSpoiler);

                Timestamp ts = rs.getTimestamp("date_publication");
                if (ts != null) {
                    c.setDateCommentaire(ts.toLocalDateTime());
                }
                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur findByFilmId : " + e.getMessage());
        }
        return commentaires;
    }

    /**
     * Sauvegarde un nouveau commentaire
     */
    public int save(Commentaire commentaire) {
        int generatedId = 0;
        String sql = "INSERT INTO comment (media_id, user_id, username, contenu, contient_spoils, date_publication) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, commentaire.getMediaId());
            ps.setInt(2, commentaire.getUserId());
            ps.setString(3, commentaire.getUsername());
            ps.setString(4, commentaire.getContenu());
            ps.setBoolean(5, commentaire.isSpoiler());

            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                generatedId = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur save : " + e.getMessage());
        }
        return generatedId;
    }

    /**
     * Supprime un commentaire (Action Admin)
     */
    public void delete(int id) {
        String sql = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Commentaire " + id + " supprimé.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression : " + e.getMessage());
        }
    }
}