package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.utils.ConxDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap; // <--- Import très important pour garder l'ordre des dates
import java.util.Map;

public class AdminDashboardDAO {
    private Connection cnx;

    public AdminDashboardDAO() {
        
        cnx = ConxDB.getInstance(); 
    }


    public Map<String, Integer> getFilmsByCategory() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = "SELECT c.nom, COUNT(mc.media_id) as total " +
                       "FROM category c " +
                       "JOIN media_category mc ON c.id = mc.category_id " +
                       "JOIN film f ON mc.media_id = f.id " +
                       "GROUP BY c.nom ORDER BY total DESC";
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("nom"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur films par catégorie : " + e.getMessage());
        }
        return data;
    }

   
    public Map<String, Integer> getTop5FilmsVus() {
        Map<String, Integer> data = new LinkedHashMap<>();
        String query = "SELECT m.titre, f.nbre_vues " +
                       "FROM film f " +
                       "JOIN media m ON f.id = m.id " +
                       "ORDER BY f.nbre_vues DESC LIMIT 5";
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString("titre"), rs.getInt("nbre_vues"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur top 5 films : " + e.getMessage());
        }
        return data;
    }
    public int getCount(String tableName) {
        int count = 0;

        String query = "SELECT COUNT(*) FROM " + tableName; 
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL (" + tableName + ") : " + e.getMessage());
        }
        return count;
    }

    public Map<String, Integer> getContentByYear() {
        Map<String, Integer> data = new HashMap<>();
        

       
        String query = "SELECT date_sortie, COUNT(*) as total FROM media GROUP BY release_year ORDER BY release_year ASC LIMIT 10";


      

        
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {

                
                String year = rs.getString("date_sortie") != null ? rs.getString("date_sortie") : "Inconnu";

        
              
                data.put(year, rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement graph années : " + e.getMessage());
        }
        return data;
    }


    public Map<String, Integer> getCommentsByType() {

        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = """
            SELECT 
                CASE 
                    WHEN contient_spoils = 1 THEN 'Spoiler'
                    WHEN est_signale = 1 THEN 'Signalé'
                    ELSE 'Normal'
                END AS type_commentaire,
                COUNT(*) AS total
            FROM comment
            GROUP BY type_commentaire
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                data.put(rs.getString("type_commentaire"), rs.getInt("total"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

       

        return data;
    }


    public Map<String, Integer> getInscriptionsData() {

        
        Map<String, Integer> data = new LinkedHashMap<>(); 
      

     


        String query = "SELECT DATE(created_at) as jour, COUNT(*) as total " +
                       "FROM users GROUP BY DATE(created_at) ORDER BY jour DESC LIMIT 7";

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String jour = rs.getString("jour");
                int total = rs.getInt("total");
                data.put(jour, total);
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement graph inscriptions : " + e.getMessage());
        }
        return data;
    }
}