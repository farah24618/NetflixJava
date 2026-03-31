package tn.farah.NetflixJava.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.utils.ConxDB;

public class EpisodeDAO {

    // ─────────────────────────────────
    // FIND ALL
    // ─────────────────────────────────
    public List<Episode> findAll() {
        List<Episode> episodes = new ArrayList<>();
        String SQL = "SELECT * FROM episode";

        try (Connection conn = ConxDB.getInstance();
             Statement stml = conn.createStatement();
             ResultSet rs = stml.executeQuery(SQL)) {

            while (rs.next()) {
                episodes.add(new Episode(
                    rs.getInt(1),
                    rs.getInt(2),
                    rs.getString(3),
                    rs.getInt(4),
                    rs.getString(7),
                    rs.getInt(5),
                    rs.getString(6),
                    rs.getString(8),
                    rs.getInt(9)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return episodes;
    }

    // ─────────────────────────────────
    // SAVE
    // ─────────────────────────────────
    public int save(Episode episode) {
        int episodeId = 0;
        String sql = "INSERT INTO episode(season_id, titre, numero, duree_minutes, " +
                     "resume, url_video, url_image, duree_intro_sec) VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstml.setInt(1, episode.getSaisonId());
            pstml.setString(2, episode.getTitre());
            pstml.setInt(3, episode.getNumeroEpisode());
            pstml.setInt(4, episode.getDuree());
            pstml.setString(5, episode.getResume());
            pstml.setString(6, episode.getVideoUrl());
            pstml.setString(7, episode.getMiniatureUrl());
            pstml.setInt(8, episode.getDurreeIntro());

            pstml.executeUpdate();

            ResultSet rs = pstml.getGeneratedKeys();
            if (rs.next()) episodeId = rs.getInt(1);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return episodeId;
    }

    // ─────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────
    public Episode findById(int id) {
        String sql = "SELECT * FROM episode WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, id);
            ResultSet rs = pstml.executeQuery();

            if (rs.next()) {
                return new Episode(
                    id,
                    rs.getInt(2),
                    rs.getString(3),
                    rs.getInt(4),
                    rs.getString(7),
                    rs.getInt(5),
                    rs.getString(6),
                    rs.getString(8),
                    rs.getInt(9)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────
    // FIND BY SAISON (Correction: Non-statique)
    // ─────────────────────────────────
    public List<Episode> findBySaison(int saisonId) {
        List<Episode> episodes = new ArrayList<>();
        String sql = "SELECT * FROM episode WHERE season_id = ? ORDER BY numero ASC";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, saisonId);
            ResultSet rs = pstml.executeQuery();

            while (rs.next()) {
                episodes.add(new Episode(
                    rs.getInt(1),
                    saisonId,
                    rs.getString(3),
                    rs.getInt(4),
                    rs.getString(7),
                    rs.getInt(5),
                    rs.getString(6),
                    rs.getString(8),
                    rs.getInt(9)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return episodes;
    }

    // ─────────────────────────────────
    // FIND NEXT EPISODE
    // ─────────────────────────────────
    public Episode findNextEpisode(int saisonId, int numeroEpisode) {
        String sql = "SELECT * FROM episode WHERE season_id = ? AND numero = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, saisonId);
            pstml.setInt(2, numeroEpisode + 1);

            ResultSet rs = pstml.executeQuery();

            if (rs.next()) {
                return new Episode(
                    rs.getInt(1),
                    saisonId,
                    rs.getString(3),
                    numeroEpisode + 1,
                    rs.getString(7),
                    rs.getInt(5),
                    rs.getString(6),
                    rs.getString(8),
                    rs.getInt(9)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────
    // UPDATE
    // ─────────────────────────────────
    public void update(Episode episode) {
        String sql = "UPDATE episode SET season_id=?, titre=?, numero=?, duree_minutes=?, " +
                     "resume=?, url_video=?, url_image=?, duree_intro_sec=? WHERE id=?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, episode.getSaisonId());
            pstml.setString(2, episode.getTitre());
            pstml.setInt(3, episode.getNumeroEpisode());
            pstml.setInt(4, episode.getDuree());
            pstml.setString(5, episode.getResume());
            pstml.setString(6, episode.getVideoUrl());
            pstml.setString(7, episode.getMiniatureUrl());
            pstml.setInt(8, episode.getDurreeIntro());
            pstml.setInt(9, episode.getId());

            pstml.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    // DELETE
    // ─────────────────────────────────
    public void delete(int id) {
        String sql = "DELETE FROM episode WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, id);
            pstml.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    // COUNT
    // ─────────────────────────────────
    public int countBySaison(int saisonId) {
        String sql = "SELECT COUNT(*) FROM episode WHERE season_id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, saisonId);
            ResultSet rs = pstml.executeQuery();

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}