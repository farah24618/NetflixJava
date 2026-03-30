package tn.farah.NetflixJava.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.utils.ConxDB;

public class SaisonDAO {

    // ─────────────────────────────────
    // FIND ALL
    // ─────────────────────────────────
    public static List<Saison> findAll() {
        List<Saison> saisons = new ArrayList<>();
        String SQL = "SELECT * FROM season";

        try (Connection conn = ConxDB.getInstance();
             Statement stml = conn.createStatement();
             ResultSet rs = stml.executeQuery(SQL)) {

            while (rs.next()) {
                saisons.add(new Saison(
                    rs.getInt(1),
                    rs.getInt(2),
                    rs.getInt(3)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return saisons;
    }

    // ─────────────────────────────────
    // SAVE (INSERT)
    // ─────────────────────────────────
    public static int save(Saison saison) {
        int saisonId = 0;
        String sql = "INSERT INTO season(idSerie, numeroSaison) VALUES (?, ?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstml.setInt(1, saison.getIdSerie());
            pstml.setInt(2, saison.getNumeroSaison());
            pstml.executeUpdate();

            ResultSet rs = pstml.getGeneratedKeys();
            if (rs.next()) {
                saisonId = rs.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return saisonId;
    }

    // ─────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────
    public static Saison findById(int id) {
        String sql = "SELECT * FROM season WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, id);
            ResultSet rs = pstml.executeQuery();

            if (rs.next()) {
                return new Saison(
                    id,
                    rs.getInt(2),
                    rs.getInt(3)
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────
    // FIND BY SERIE
    // ─────────────────────────────────
    public static List<Saison> findBySerie(int idSerie) {
        List<Saison> saisons = new ArrayList<>();
        String sql = "SELECT * FROM season WHERE idSerie = ? ORDER BY numeroSaison ASC";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, idSerie);
            ResultSet rs = pstml.executeQuery();

            while (rs.next()) {
                saisons.add(new Saison(
                    rs.getInt(1),
                    idSerie,
                    rs.getInt(3)
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return saisons;
    }

    // ─────────────────────────────────
    // UPDATE
    // ─────────────────────────────────
    public static void update(Saison saison) {
        String sql = "UPDATE season SET idSerie = ?, numeroSaison = ? WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, saison.getIdSerie());
            pstml.setInt(2, saison.getNumeroSaison());
            pstml.setInt(3, saison.getId());
            pstml.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    // DELETE
    // ─────────────────────────────────
    public static void delete(int id) {
        String sql = "DELETE FROM season WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, id);
            pstml.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    // COUNT BY SERIE
    // ─────────────────────────────────
    public static int countBySerie(int idSerie) {
        String sql = "SELECT COUNT(*) FROM season WHERE idSerie = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstml = conn.prepareStatement(sql)) {

            pstml.setInt(1, idSerie);
            ResultSet rs = pstml.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────
    // GET SERIE ID BY SAISON
    // ─────────────────────────────────
    public static int getSerieIdBySaison(int saisonId) {
        String query = "SELECT serie_id FROM season WHERE id = ?";

        try (Connection con = ConxDB.getInstance();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, saisonId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("serie_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}