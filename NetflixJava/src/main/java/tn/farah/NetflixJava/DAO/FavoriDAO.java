package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.Favori;
import tn.farah.NetflixJava.utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriDAO {

    private static Connection conn = ConxDB.getInstance();

    public static List<Favori> findAll() {

        Statement stm = null;
        ResultSet rs = null;

        List<Favori> favoris = new ArrayList<>();

        String SQL = "SELECT * FROM favori";

        try {

            stm = conn.createStatement();
            rs = stm.executeQuery(SQL);

            while (rs.next()) {

                int id = rs.getInt(1);
                int userId = rs.getInt(2);
                int mediaId = rs.getInt(3);

                Favori favori = new Favori(id, userId, mediaId);

                favoris.add(favori);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return favoris;
    }

    public static int save(Favori favori) {

        int favoriId = 0;
        PreparedStatement pstml = null;
        ResultSet rs = null;

        try {

            String sql = "INSERT INTO favori(userId,mediaId) VALUES(?,?)";

            pstml = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstml.setInt(1, favori.getUserId());
            pstml.setInt(2, favori.getMediaId());

            pstml.executeUpdate();

            rs = pstml.getGeneratedKeys();

            if (rs.next()) {
                favoriId = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return favoriId;
    }

    public static int delete(int id) {

        int rows = 0;

        try {

            String sql = "DELETE FROM favori WHERE id=?";

            PreparedStatement pstml = conn.prepareStatement(sql);

            pstml.setInt(1, id);

            rows = pstml.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }
    public static boolean existe(int userId, int mediaId) {
        String sql = "SELECT * FROM favori WHERE userId = ? AND mediaId = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, mediaId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public static Favori findByUserIdAndMediaId(int userId, int mediaId) {
        String sql = "SELECT * FROM favori WHERE userId = ? AND mediaId = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, mediaId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Favori(
                        rs.getInt("id"),
                        rs.getInt("userId"),
                        rs.getInt("mediaId")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static List<Favori> getFavorisByUser(int userId) {

        List<Favori> favoris = new ArrayList<>();
        String sql = "SELECT * FROM favori WHERE userId = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Favori f = new Favori(
                        rs.getInt("id"),
                        rs.getInt("userId"),
                        rs.getInt("mediaId")
                );
                favoris.add(f);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return favoris;
    }
    
}