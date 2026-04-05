package tn.farah.NetflixJava.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Subtitle;

public class SubtitleDAO {
    
    private final Connection connection;

    public SubtitleDAO(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────
    //  FIND ALL
    // ─────────────────────────────────
    public List<Subtitle> findAll() {
        List<Subtitle> subtitles = new ArrayList<>();
        String SQL = "SELECT * FROM subtitle";
        
        try (Statement stml = connection.createStatement();
             ResultSet rs = stml.executeQuery(SQL)) {
            
            while (rs.next()) {
                subtitles.add(new Subtitle(
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getInt(3),
                    rs.getString(4)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtitles;
    }

    // ─────────────────────────────────
    //  SAVE (INSERT)
    // ─────────────────────────────────
    public int save(Subtitle subtitle) {
        int subtitleId = 0;
        String sql = "INSERT INTO subtitle(langage, idMedia, url) VALUES (?,?,?)";
        
        try (PreparedStatement pstml = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstml.setString(1, subtitle.getLangage());
            pstml.setInt(2, subtitle.getIdMedia());
            pstml.setString(3, subtitle.getUrl());
            pstml.executeUpdate();
            
            try (ResultSet rs = pstml.getGeneratedKeys()) {
                if (rs.next()) {
                    subtitleId = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de la sauvegarde du sous-titre : " + ex.getMessage());
        }
        return subtitleId;
    }

    // ─────────────────────────────────
    //  FIND BY ID
    // ─────────────────────────────────
    public Subtitle findById(int id) {
        String sql = "SELECT * FROM subtitle WHERE id = ?";
        
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, id);
            try (ResultSet rs = pstml.executeQuery()) {
                if (rs.next()) {
                    return new Subtitle(
                        id,
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getString(4)
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────
    //  FIND BY MEDIA (Tous les sous-titres d'un media)
    // ─────────────────────────────────
    public List<Subtitle> findByMedia(int idMedia) {
        List<Subtitle> subtitles = new ArrayList<>();
        String sql = "SELECT * FROM subtitle WHERE idMedia = ?";
        
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, idMedia);
            try (ResultSet rs = pstml.executeQuery()) {
                while (rs.next()) {
                    subtitles.add(new Subtitle(
                        rs.getInt(1),
                        rs.getString(2),
                        idMedia,
                        rs.getString(4)
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subtitles;
    }

    // ─────────────────────────────────
    //  FIND BY MEDIA + LANGAGE
    // ─────────────────────────────────
    public Subtitle findByMediaAndLangage(int idMedia, String langage) {
        String sql = "SELECT * FROM subtitle WHERE idMedia = ? AND langage = ?";
        
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, idMedia);
            pstml.setString(2, langage);
            try (ResultSet rs = pstml.executeQuery()) {
                if (rs.next()) {
                    return new Subtitle(
                        rs.getInt(1),
                        langage,
                        idMedia,
                        rs.getString(4)
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────
    public void update(Subtitle subtitle) {
        String sql = "UPDATE subtitle SET langage = ?, idMedia = ?, url = ? WHERE id = ?";
        
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setString(1, subtitle.getLangage());
            pstml.setInt(2, subtitle.getIdMedia());
            pstml.setString(3, subtitle.getUrl());
            pstml.setInt(4, subtitle.getId());
            pstml.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    //  DELETE
    // ─────────────────────────────────
    public void delete(int id) {
        String sql = "DELETE FROM subtitle WHERE id = ?";
        
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, id);
            pstml.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}