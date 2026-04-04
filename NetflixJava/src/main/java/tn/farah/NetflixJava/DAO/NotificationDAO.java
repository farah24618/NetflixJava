package tn.farah.NetflixJava.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Notification;

public class NotificationDAO {

    private final Connection connection;

    // Harmonisation : on passe la connexion au constructeur
    public NotificationDAO(Connection connection) {
        this.connection = connection;
    }

    // ─────────────────────────────────
    //  RÉCUPÉRER LES NOTIFICATIONS D'UN UTILISATEUR
    // ─────────────────────────────────
    public List<Notification> getUserNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id=? ORDER BY date DESC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getString("date"),
                        rs.getBoolean("important"),
                        rs.getBoolean("is_read")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─────────────────────────────────
    //  COMPTER LES NOTIFICATIONS NON LUES
    // ─────────────────────────────────
    public int countUnread(int userId) {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=FALSE";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────
    //  MARQUER COMME LU
    // ─────────────────────────────────
    public void markAsRead(int notificationId) {
        String query = "UPDATE notifications SET is_read=TRUE WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    //  AJOUTER UNE NOTIFICATION
    // ─────────────────────────────────
    public void addNotification(Notification notification) {
        // Note : NOW() est spécifique à MySQL/MariaDB pour la date actuelle
        String query = "INSERT INTO notifications (user_id, type, title, message, date, important, is_read) VALUES (?, ?, ?, ?, NOW(), ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getType());
            ps.setString(3, notification.getTitle());
            ps.setString(4, notification.getMessage());
            ps.setBoolean(5, notification.isImportant());
            ps.setBoolean(6, false); // Toujours false (non lu) par défaut à la création

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la notification : " + e.getMessage());
        }
    }
}