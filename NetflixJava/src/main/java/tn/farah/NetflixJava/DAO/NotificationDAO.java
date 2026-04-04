package tn.farah.NetflixJava.DAO;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Notification; // Le bon import est bien là
import tn.farah.NetflixJava.utils.DatabaseConnection;

public class NotificationDAO {

    public List<Notification> getUserNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id=? ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Notification n = new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getString("date"),
                        rs.getBoolean("important"),
                        rs.getBoolean("is_read")
                );
                list.add(n);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // 🔴 compter les notifications non lues
    public int countUnread(int userId) {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ✔️ marquer comme lu
    public void markAsRead(int notificationId) {
        String query = "UPDATE notifications SET is_read=TRUE WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, notificationId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ➕ ajouter une notification (CORRIGÉ ICI)
    public void addNotification(Notification notification) {
        String query = "INSERT INTO notifications (user_id, type, title, message, date, important, is_read) VALUES (?, ?, ?, ?, NOW(), ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getType());
            ps.setString(3, notification.getTitle());
            ps.setString(4, notification.getMessage());
            ps.setBoolean(5, notification.isImportant());
            ps.setBoolean(6, false); // toujours non lu au début

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // Dans NotificationDAO.java
    public int countAll(int userId) {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}	