package tn.farah.NetflixJava.DAO;

import java.sql.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Saison;

public class SaisonDAO {

    private Connection connection;

    public SaisonDAO(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────
    // FIND ALL
    // ─────────────────────────────────
    public List<Saison> findAll() {
        List<Saison> saisons = new ArrayList<>();
        String SQL = "SELECT id, serie_id, numero, nom, date_sortie FROM season";

        try (Statement stml = connection.createStatement();
             ResultSet rs = stml.executeQuery(SQL)) {

            while (rs.next()) {
                saisons.add(mapResultSetToSaison(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return saisons;
    }

    // ─────────────────────────────────
    // SAVE (INSERT)
    // ─────────────────────────────────
    public int save(Saison saison) {
        int saisonId = 0;
        String sql = "INSERT INTO season(serie_id, numero, nom, date_sortie) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstml = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstml.setInt(1, saison.getIdSerie());
            pstml.setInt(2, saison.getNumeroSaison());
            pstml.setString(3, saison.getNom());
            pstml.setTimestamp(4, Timestamp.valueOf(saison.getDateSortie()));
            
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
    public Saison findById(int id) {
        String sql = "SELECT id, serie_id, numero, nom, date_sortie FROM season WHERE id = ?";

        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, id);
            try (ResultSet rs = pstml.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSaison(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // ─────────────────────────────────
    // UPDATE
    // ─────────────────────────────────
    public void update(Saison saison) {
        String sql = "UPDATE season SET serie_id = ?, numero = ?, nom = ?, date_sortie = ? WHERE id = ?";

        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, saison.getIdSerie());
            pstml.setInt(2, saison.getNumeroSaison());
            pstml.setString(3, saison.getNom());
            pstml.setTimestamp(4, Timestamp.valueOf(saison.getDateSortie()));
            pstml.setInt(5, saison.getId());
            pstml.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    // DELETE
    // ─────────────────────────────────
    public void delete(int id) {
        String sql = "DELETE FROM season WHERE id = ?";
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, id);
            pstml.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    // FIND BY SERIE
    // ─────────────────────────────────
    public List<Saison> findBySerieId(int idSerie) {
        List<Saison> saisons = new ArrayList<>();
        String sql = "SELECT id, serie_id, numero, nom, date_sortie FROM season WHERE serie_id = ? ORDER BY numero ASC";

        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, idSerie);
            try (ResultSet rs = pstml.executeQuery()) {
                while (rs.next()) {
                    saisons.add(mapResultSetToSaison(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return saisons;
    }
   
    // ─────────────────────────────────
    // METHODES UTILITAIRES / COMPLEMENTAIRES
    // ─────────────────────────────────

    private Saison mapResultSetToSaison(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date_sortie");
        LocalDateTime dateSortie = (ts != null) ? ts.toLocalDateTime() : null;
        
        return new Saison(
            rs.getInt("id"),
            rs.getInt("serie_id"),
            rs.getInt("numero"),
            rs.getString("nom"),
            dateSortie
        );
    }

    public int countBySerie(int idSerie) {
        String sql = "SELECT COUNT(*) FROM season WHERE serie_id = ?";
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, idSerie);
            ResultSet rs = pstml.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getSerieIdBySaison(int saisonId) {
        String query = "SELECT serie_id FROM season WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, saisonId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("serie_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public Saison getSaisonbyIdEpidsode(int idEpisode) {
        String sql = "SELECT s.* FROM season s JOIN episode e ON s.id = e.season_id WHERE e.id = ?";
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
            pstml.setInt(1, idEpisode);
            try (ResultSet rs = pstml.executeQuery()) {
                if (rs.next()) return mapResultSetToSaison(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean deleteLastSaison(int serieId, int numeroSaison) {
        String query = "DELETE FROM season WHERE serie_id = ? AND numero = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setInt(1, serieId);
            ps.setInt(2, numeroSaison);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public int findFirstSeasonIdBySerie(int serieId) {
        int firstSeasonId = -1; // Valeur par défaut si rien n'est trouvé
        
        // On récupère la saison avec le numéro le plus petit pour cette série
        String sql = "SELECT id FROM season WHERE serie_id = ? ORDER BY numero ASC LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setInt(1, serieId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    firstSeasonId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la première saison : " + e.getMessage());
            e.printStackTrace();
        }
        
        return firstSeasonId;
    }
   
    // Pour le compteur
    public int countBySerieId(int serieId) {
        String sql = "SELECT COUNT(*) FROM saison WHERE serie_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, serieId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}

