package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.util.List;
import tn.farah.NetflixJava.DAO.NotificationDAO;
import tn.farah.NetflixJava.Entities.Notification;

public class NotificationService {

    private NotificationDAO dao;

    private static final int ADMIN_USER_ID = 1;

    public NotificationService(Connection c) {
        dao = new NotificationDAO(c);
    }


    public List<Notification> getUserNotifications(int userId) {
        return dao.getUserNotifications(userId);
    }
    public List<Notification> getUserNotificationsAdmin(){
    	return dao.getUserNotificationsAdmin();
    }

    public List<Notification> getAllNotifications() {
        return dao.getAllNotifications();
    }

    public List<Notification> getUnreadNotifications(int userId) {
        return dao.getUnreadNotifications(userId);
    }

    public List<Notification> getSentNotifications() {
        return dao.getSentNotifications();
    }

    public List<Notification> search(int userId, String query) {
        return dao.search(userId, query);
    }


    public int countAll(int userId) {
        return dao.countAll(userId);
    }

    public int countUnread(int userId) {
        return dao.countUnread(userId);
    }

    public void markAsRead(int notificationId) {
        dao.markAsRead(notificationId);
    }
    public void markAllAsRead(int userId) {
    	dao.markAllAsRead(userId);
    }
    public void addNotification(Notification notification) {
        dao.addNotification(notification);
    }

    public void notifyCommentSignale(String reporterUsername, String commentPreview, int commentId) {
        String title   = "⚠️ Commentaire signalé";
        String message = "L'utilisateur \"" + reporterUsername + "\" a signalé le commentaire #"
                         + commentId + " : \"" + truncate(commentPreview, 50) + "\"";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "REPORT", title, message, "", true, false
        );
        dao.addNotification(n);
    }

    
    public void notifyNewContent(String contentType, String contentTitle) {
        String title   = "🎬 Nouveau " + contentType + " ajouté";
        String message = "\"" + contentTitle + "\" a été ajouté au catalogue et est maintenant visible.";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "CONTENT", title, message, "", false, false
        );
        dao.addNotification(n);
    }

   
    public void notifyNewUser(String newUsername, String email) {
        String title   = "👤 Nouvel utilisateur inscrit";
        String message = newUsername + " (" + email + ") vient de créer un compte.";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "USER", title, message, "", false, false
        );
        dao.addNotification(n);
    }

    public void notifyNewSubscription(String username, String planName) {
        String title   = "💳 Nouvel abonnement";
        String message = username + " a souscrit au plan \"" + planName + "\".";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "SUBSCRIPTION", title, message, "", false, false
        );
        dao.addNotification(n);
    }

  
    public void notifyLoginFailure(String username, int attempts) {
        String title   = "🔒 Tentatives de connexion suspectes";
        String message = attempts + " tentatives échouées pour le compte : " + username;
        Notification n = new Notification(
            0, ADMIN_USER_ID, "SECURITY", title, message, "", true, false
        );
        dao.addNotification(n);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
    public boolean userExists(int userId) {
    	return dao.userExists(userId);
    }

    public int countAllAdmin() {
        return dao.countAllAdmin();
    }
    public List<Notification> getSentByAdmin(int adminId) {
    	return dao.getSentByAdmin(adminId);
    }
    public int countSentByAdmin() {
    	return dao.countSentByAdmin();
    }
    public void deleteNotification(int id) {
    	dao.deleteNotification(id);
    }
}