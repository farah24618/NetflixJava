package tn.farah.NetflixJava.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.History;

public class HistoryDAO {

    private Connection conn;

    public HistoryDAO(Connection conn) {
        this.conn = conn;
    }

    public List<History> findAll() {
        List<History> list = new ArrayList<>();
        String sql = "SELECT * FROM visionnage";
        try {
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int save(History h) {
        String sql = "INSERT INTO visionnage(user_id, film_id, episode_id, date_visionnage, temps_arret_sec, est_termine) VALUES(?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, h.getIdUser());
            // film_id nullable
            if (h.getFilmId() != null) ps.setInt(2, h.getFilmId());
            else ps.setNull(2, Types.INTEGER);
            // episode_id nullable
            if (h.getEpisodeId() != null) ps.setInt(3, h.getEpisodeId());
            else ps.setNull(3, Types.INTEGER);
            ps.setTimestamp(4, Timestamp.valueOf(h.getDateVisionnage()));
            ps.setInt(5, h.getTempsArret());
            ps.setBoolean(6, h.getEstTermine());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) { h.setId(rs.getInt(1)); return h.getId(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int update(History h) {
        String sql = "UPDATE visionnage SET date_visionnage=?, temps_arret_sec=?, est_termine=? WHERE id=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(h.getDateVisionnage()));
            ps.setInt(2, h.getTempsArret());
            ps.setBoolean(3, h.getEstTermine());
            ps.setInt(4, h.getId());
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<History> findByUser(int userId) {
        List<History> list = new ArrayList<>();
        String sql = "SELECT * FROM history WHERE user_id = ? ORDER BY date_visionnage DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public History findById(int id) {
        String sql = "SELECT * FROM history WHERE id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Recherche par film
    public History findByUserAndFilm(int userId, int filmId) {
        String sql = "SELECT * FROM history WHERE user_id = ? AND film_id = ? ORDER BY date_visionnage DESC LIMIT 1";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, filmId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Recherche par épisode
    public History findByUserAndEpisode(int userId, int episodeId) {
        String sql = "SELECT * FROM history WHERE user_id = ? AND episode_id = ? ORDER BY date_visionnage DESC LIMIT 1";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, episodeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<History> findTop5MostWatched(int userId) {
        List<History> list = new ArrayList<>();
        String sql = """
            SELECT id, user_id, film_id, episode_id,
                   MAX(date_visionnage) as date_visionnage,
                   temps_arret_sec, est_termine
            FROM history
            WHERE user_id = ?
            GROUP BY COALESCE(film_id, episode_id)
            ORDER BY COUNT(*) DESC
            LIMIT 5
            """;
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void delete(int id) {
        String sql = "DELETE FROM history WHERE id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Mapper ──────────────────────────────────────────────────────────────
    private History map(ResultSet rs) throws SQLException {
        // film_id et episode_id peuvent être NULL
        int filmId = rs.getInt("film_id");
        Integer filmIdVal = rs.wasNull() ? null : filmId;

        int episodeId = rs.getInt("episode_id");
        Integer episodeIdVal = rs.wasNull() ? null : episodeId;

        return new History(
            rs.getInt("id"),
            rs.getInt("user_id"),
            filmIdVal,
            episodeIdVal,
            rs.getTimestamp("date_visionnage").toLocalDateTime(),
            rs.getInt("temps_arret_sec"),
            rs.getBoolean("est_termine")
        );
    }
}