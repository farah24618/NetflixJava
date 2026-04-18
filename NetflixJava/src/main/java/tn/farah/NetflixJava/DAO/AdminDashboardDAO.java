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
        

        String query = "SELECT release_year, COUNT(*) as total FROM film GROUP BY release_year ORDER BY release_year ASC LIMIT 10";
        
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
        
                String year = rs.getString("release_year") != null ? rs.getString("release_year") : "Inconnu";
                data.put(year, rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement graph années : " + e.getMessage());
        }
        return data;
    }


    public Map<String, Integer> getCommentsByType() {
        Map<String, Integer> data = new HashMap<>();
     
        data.put("Sur les Films", getCount("comment")); 
        data.put("Sur les Séries", 0); 
        
        return data;
    }

    public Map<String, Integer> getInscriptionsData() {
   
        Map<String, Integer> data = new LinkedHashMap<>(); 

        String query = "SELECT DATE(created_at) as jour, COUNT(*) as total " +
                       "FROM users GROUP BY DATE(created_at) ORDER BY jour ASC LIMIT 7";

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