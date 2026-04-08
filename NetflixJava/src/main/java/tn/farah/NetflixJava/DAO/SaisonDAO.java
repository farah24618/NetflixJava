package tn.farah.NetflixJava.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.utils.ConxDB;

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
        String SQL = "SELECT * FROM season";

        try (Statement stml = connection.createStatement();
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
    public int save(Saison saison) {
        int saisonId = 0;
        String sql = "INSERT INTO season(serie_id, numero) VALUES (?, ?)";

        try (PreparedStatement pstml = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
    public Saison findById(int id) {
        String sql = "SELECT * FROM season WHERE id = ?";

        try (PreparedStatement pstml = connection.prepareStatement(sql)) {

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
    public List<Saison> findBySerie(int idSerie) {
        List<Saison> saisons = new ArrayList<>();
        String sql = "SELECT * FROM season WHERE serie_id = ? ORDER BY numero ASC";

        try (PreparedStatement pstml = connection.prepareStatement(sql)) {

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
    public void update(Saison saison) {
        String sql = "UPDATE season SET serie_id = ?, numero = ? WHERE id = ?";

        try (PreparedStatement pstml = connection.prepareStatement(sql)) {

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
    // COUNT BY SERIE
    // ─────────────────────────────────
    public int countBySerie(int idSerie) {
        String sql = "SELECT COUNT(*) FROM season WHERE serie_id = ?";

        try (PreparedStatement pstml = connection.prepareStatement(sql)) {

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
    public int getSerieIdBySaison(int saisonId) {
        String query = "SELECT serie_id FROM season WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

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

    // ─────────────────────────────────
    // GET SAISON BY EPISODE ID
    // ─────────────────────────────────
    public Saison getSaisonbyIdEpidsode(int idEpisode) {
        String sql = "SELECT * FROM season s JOIN episode e ON s.id=e.season_id WHERE e.id = ?";
        
        try (PreparedStatement pstml = connection.prepareStatement(sql)) {
             
            pstml.setInt(1, idEpisode);
            try (ResultSet rs = pstml.executeQuery()) {
                if (rs.next()) {
                    int idSaison = rs.getInt(1);
                    int idSerie = rs.getInt(2);
                    int numeroSaison = rs.getInt(3);
                    return new Saison(idSaison, idSerie, numeroSaison);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
    
    
    
    
    
    
    
    
    
    
    
    
    
 // Dans SaisonDAO.java
    public List<Saison> findBySerieId(int serieId) {
        List<Saison> saisons = new ArrayList<>();
        // Utilise le nom de table correct (season ou saison)
        String sql = "SELECT * FROM season WHERE serie_id = ? ORDER BY numero ASC"; 
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, serieId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                // Assure-toi que les noms de colonnes correspondent à ta DB
                saisons.add(new Saison(
                    rs.getInt("id"), 
                    rs.getInt("serie_id"), 
                    rs.getInt("numero")
                ));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return saisons;
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
    
    
    public boolean deleteLastSaison(int serieId, int numeroSaison) {
        String query = "DELETE FROM saison WHERE id_serie = ? AND numero_saison = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setInt(1, serieId);
            ps.setInt(2, numeroSaison);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Dans EpisodeDAO.java
   
    
    
    
    
    
}