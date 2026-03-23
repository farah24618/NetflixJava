package tn.farah.NetflixJava.DAO;


import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.utils.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SerieDAO {

    private static Connection conn = ConxDB.getInstance();

    public static List<Serie> findAll() {
        List<Serie> series = new ArrayList<>();
        String sql = "SELECT * FROM serie";

        try {
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);

            while (rs.next()) {
                Serie serie = new Serie();
                serie.setId(rs.getInt("id"));
                serie.setTitre(rs.getString("titre"));
                serie.setSynopsis(rs.getString("synopsis"));
                serie.setGenre(rs.getString("genre"));
                serie.setTerminee(rs.getBoolean("terminee"));
                serie.setDureeIntro(rs.getInt("duree_intro"));

                series.add(serie);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return series;
    }

    public static Serie findById(int id) {
        String sql = "SELECT * FROM serie WHERE id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Serie serie = new Serie();
                serie.setId(rs.getInt("id"));
                serie.setTitre(rs.getString("titre"));
                serie.setSynopsis(rs.getString("synopsis"));
                serie.setGenre(rs.getString("genre"));
                serie.setTerminee(rs.getBoolean("terminee"));
                serie.setDureeIntro(rs.getInt("duree_intro"));

                return serie;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int save(Serie serie) {
        int serieId = 0;

        String sql = "INSERT INTO serie(titre, synopsis, genre, terminee, duree_intro) VALUES(?,?,?,?,?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, serie.getTitre());
            ps.setString(2, serie.getSynopsis());
            ps.setString(3, serie.getGenre());
            ps.setBoolean(4, serie.isTerminee());
            ps.setInt(5, serie.getDureeIntro());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                serieId = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return serieId;
    }

    public static int update(Serie serie) {
        int rows = 0;

        String sql = "UPDATE serie SET titre = ?, synopsis = ?, genre = ?, terminee = ?, duree_intro = ? WHERE id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, serie.getTitre());
            ps.setString(2, serie.getSynopsis());
            ps.setString(3, serie.getGenre());
            ps.setBoolean(4, serie.isTerminee());
            ps.setInt(5, serie.getDureeIntro());
            ps.setInt(6, serie.getId());

            rows = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }

    public static int delete(int id) {
        int rows = 0;

        String sql = "DELETE FROM serie WHERE id = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            rows = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows;
    }
}