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
        // Assurez-vous que votre problème de NullPointerException est réglé (XAMPP allumé, driver Maven présent)
        cnx = ConxDB.getInstance(); 
    }

    // 1. Requête pour les cartes du haut (Films, Séries, etc.)
    public int getCount(String tableName) {
        int count = 0;
        // Requête exacte : SELECT COUNT(*) FROM nom_de_la_table
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

    // 2. Requête pour le graphique "Contenu par année"
    public Map<String, Integer> getContentByYear() {
        Map<String, Integer> data = new HashMap<>();
        
        // ⚠️ REMPLACEZ 'release_year' PAR LE VRAI NOM DE VOTRE COLONNE DANS LA TABLE 'film'
        String query = "SELECT release_year, COUNT(*) as total FROM film GROUP BY release_year ORDER BY release_year ASC LIMIT 10";
        
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                // Si l'année est null dans la DB, on met "Inconnu"
                String year = rs.getString("release_year") != null ? rs.getString("release_year") : "Inconnu";
                data.put(year, rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement graph années : " + e.getMessage());
        }
        return data;
    }

    // 3. Requête pour le graphique "Commentaires par type"
    public Map<String, Integer> getCommentsByType() {
        Map<String, Integer> data = new HashMap<>();
        
        // Si vous n'avez pas de colonne "type" pour différencier film/série dans les commentaires,
        // Voici une requête simplifiée qui compte juste les commentaires liés aux films vs séries s'ils ont des id séparés.
        // Si vous avez juste une table comment globale, voici comment on peut simuler en attendant :
        data.put("Sur les Films", getCount("comment")); // À adapter selon la structure de votre table 'comment'
        data.put("Sur les Séries", 0); 
        
        return data;
    }

    // 4. --- NOUVELLE MÉTHODE --- Requête pour le graphique des Inscriptions
    public Map<String, Integer> getInscriptionsData() {
        // LinkedHashMap préserve l'ordre des dates récupérées depuis la base
        Map<String, Integer> data = new LinkedHashMap<>(); 
        
        // On utilise la table 'user' (selon votre capture phpMyAdmin)
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