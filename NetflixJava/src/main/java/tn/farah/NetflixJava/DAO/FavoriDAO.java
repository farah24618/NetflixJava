package tn.farah.NetflixJava.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.utils.ConxDB;


public class FavoriDAO {

    private Connection connection;

    public FavoriDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Favori> findAll() {
        List<Favori> favoris = new ArrayList<>();
        String sql = "SELECT * FROM favorite";
        
        try (Statement stm = connection.createStatement();
             ResultSet rs = stm.executeQuery(sql)) {
            
            while (rs.next()) {
                favoris.add(new Favori(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("media_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoris;
    }

    public int save(Favori favori) {
        int favoriId = 0;
        String sql = "INSERT INTO favorite (user_id, media_id) VALUES(?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, favori.getUserId());
            ps.setInt(2, favori.getMediaId());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) favoriId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoriId;
    }

    public int delete(int id) {
        int rows = 0;
        String sql = "DELETE FROM favorite WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            rows = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public boolean existe(int userId, int mediaId) {
        String sql = "SELECT id FROM favorite WHERE user_id = ? AND media_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, mediaId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Favori findByUserIdAndMediaId(int userId, int mediaId) {
        String sql = "SELECT * FROM favorite WHERE user_id = ? AND media_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, mediaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Favori(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("media_id")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Favori> getFavorisByUser(int userId) {
        List<Favori> favoris = new ArrayList<>();
        String sql = "SELECT * FROM favorite WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    favoris.add(new Favori(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("media_id")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoris;
    }

    public int supprimerFavori(int userId, int mediaId) {
        int rows = 0;
        String sql = "DELETE FROM favorite WHERE user_id = ? AND media_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, mediaId);
            rows = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}