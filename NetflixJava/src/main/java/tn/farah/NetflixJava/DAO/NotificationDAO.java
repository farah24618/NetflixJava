package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private Connection connection;

    public NotificationDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Notification> getUserNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? OR type IN ('MISE_A_JOUR', 'NOUVEAUTE') ORDER BY date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
  
    public List<Notification> getUserNotificationsAdmin() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE type = 'SIGNALEMENT' ORDER BY date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Notification> getAllNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Notification> getUnreadNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE type = 'SIGNALEMENT' AND is_read = 0 ORDER BY date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Notification> getSentNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE type = 'SENT' ORDER BY date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications " +
                     "WHERE is_read = 0 AND (user_id = ? OR type IN ('MISE_A_JOUR', 'NEW'))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 " +
                     "WHERE is_read = 0 AND (user_id = ? OR type IN ('MISE_A_JOUR', 'NOUVEAUTE'))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addNotification(Notification n) {
        String sql = "INSERT INTO notifications (user_id, type, title, message, date, important, is_read) " +
                     "VALUES (?, ?, ?, ?, NOW(), ?, 0)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getType());
            ps.setString(3, n.getTitle());
            ps.setString(4, n.getMessage());
            ps.setBoolean(5, n.isImportant());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) n.setId(keys.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> search(int userId, String query) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? " +
                     "AND (LOWER(message) LIKE ? OR LOWER(title) LIKE ?) ORDER BY date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, "%" + query.toLowerCase() + "%");
            ps.setString(3, "%" + query.toLowerCase() + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        return new Notification(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("type"),
            rs.getString("title"),
            rs.getString("message"),
            rs.getString("date"),
            rs.getBoolean("important"),
            rs.getBoolean("is_read")
        );
    }

    public boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countAllAdmin() {
        String sql = "SELECT COUNT(*) FROM notifications WHERE type = 'SIGNALEMENT'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Notification> getSentByAdmin(int adminId) {
        String sql = "SELECT * FROM notifications WHERE type = 'INFO' ORDER BY date DESC";
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countSentByAdmin() {
        String sql = "SELECT COUNT(*) FROM notifications WHERE type = 'INFO'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void deleteNotification(int id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}