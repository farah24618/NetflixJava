package tn.farah.NetflixJava.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tn.farah.NetflixJava.Entities.Subtitle;

public class SubtitleDAO {
    
    private final Connection connection;

    public SubtitleDAO(Connection connection) {
        this.connection = connection;
    }

    private Subtitle mapResultSetToSubtitle(ResultSet rs) throws SQLException {
        return new Subtitle(
            rs.getInt("id"),
            rs.getString("langue"),
            rs.getInt("film_id"),
            rs.getInt("episode_id"),
            rs.getString("url_file")
        );
    }

    // Retrouver tous les sous-titres
    public List<Subtitle> findAll() {
        List<Subtitle> subtitles = new ArrayList<>();
        String sql = "SELECT * FROM subtitle";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                subtitles.add(mapResultSetToSubtitle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtitles;
    }

    // Sauvegarder (Insert)
    public int save(Subtitle subtitle) {
        String sql = "INSERT INTO subtitle(langue, url_file, film_id, episode_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, subtitle.getLangage());
            pstmt.setString(2, subtitle.getUrl());
            
            // Gestion des IDs qui peuvent être NULL en base de données
            if (subtitle.getFilmId() > 0) pstmt.setInt(3, subtitle.getFilmId()); 
            else pstmt.setNull(3, Types.INTEGER);
            
            if (subtitle.getEpisodeId() > 0) pstmt.setInt(4, subtitle.getEpisodeId()); 
            else pstmt.setNull(4, Types.INTEGER);

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public Subtitle findById(int id) {
        String sql = "SELECT * FROM subtitle WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToSubtitle(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Trouver par Film
    public List<Subtitle> findByFilm(int filmId) {
        List<Subtitle> subtitles = new ArrayList<>();
        String sql = "SELECT * FROM subtitle WHERE film_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, filmId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subtitles.add(mapResultSetToSubtitle(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtitles;
    }

    // Trouver par Episode
    public List<Subtitle> findByEpisode(int episodeId) {
        List<Subtitle> subtitles = new ArrayList<>();
        String sql = "SELECT * FROM subtitle WHERE episode_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, episodeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subtitles.add(mapResultSetToSubtitle(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtitles;
    }

    public void update(Subtitle subtitle) {
        String sql = "UPDATE subtitle SET langue = ?, url_file = ?, film_id = ?, episode_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, subtitle.getLangage());
            pstmt.setString(2, subtitle.getUrl());
            pstmt.setInt(3, subtitle.getFilmId());
            pstmt.setInt(4, subtitle.getEpisodeId());
            pstmt.setInt(5, subtitle.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM subtitle WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}