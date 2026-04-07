package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.util.List;
import tn.farah.NetflixJava.DAO.NotificationDAO;
import tn.farah.NetflixJava.Entities.Notification;

public class NotificationService {

    private NotificationDAO dao;

    // ID de l'admin : toutes les notifications système lui sont envoyées
    private static final int ADMIN_USER_ID = 1;

    public NotificationService(Connection c) {
        dao = new NotificationDAO(c);
    }

    // ═══════════════════════════════════════════════════
    //  RÉCUPÉRATION
    // ═══════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════
    //  COMPTEURS
    // ═══════════════════════════════════════════════════

    public int countAll(int userId) {
        return dao.countAll(userId);
    }

    public int countUnread(int userId) {
        return dao.countUnread(userId);
    }

    // ═══════════════════════════════════════════════════
    //  ACTIONS
    // ═══════════════════════════════════════════════════

    public void markAsRead(int notificationId) {
        dao.markAsRead(notificationId);
    }
    public void markAllAsRead(int userId) {
    	dao.markAllAsRead(userId);
    }
    public void addNotification(Notification notification) {
        dao.addNotification(notification);
    }

    // ═══════════════════════════════════════════════════
    //  NOTIFICATIONS AUTOMATIQUES — À appeler depuis
    //  les autres controllers/services
    // ═══════════════════════════════════════════════════

    /**
     * Appelé depuis CommentaireController quand un user signale un commentaire.
     *
     * @param reporterUsername  pseudo de l'utilisateur qui signale
     * @param commentPreview    extrait du commentaire signalé (50 chars max)
     * @param commentId         ID du commentaire en base
     */
    public void notifyCommentSignale(String reporterUsername, String commentPreview, int commentId) {
        String title   = "⚠️ Commentaire signalé";
        String message = "L'utilisateur \"" + reporterUsername + "\" a signalé le commentaire #"
                         + commentId + " : \"" + truncate(commentPreview, 50) + "\"";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "REPORT", title, message, "", true, false
        );
        dao.addNotification(n);
    }

    /**
     * Appelé depuis FilmService / SerieService quand un nouveau contenu est ajouté.
     *
     * @param contentType  "film" ou "série"
     * @param contentTitle titre du contenu
     */
    public void notifyNewContent(String contentType, String contentTitle) {
        String title   = "🎬 Nouveau " + contentType + " ajouté";
        String message = "\"" + contentTitle + "\" a été ajouté au catalogue et est maintenant visible.";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "CONTENT", title, message, "", false, false
        );
        dao.addNotification(n);
    }

    /**
     * Appelé depuis UserService quand un nouvel utilisateur s'inscrit.
     *
     * @param newUsername pseudo du nouvel utilisateur
     * @param email       email du nouvel utilisateur
     */
    public void notifyNewUser(String newUsername, String email) {
        String title   = "👤 Nouvel utilisateur inscrit";
        String message = newUsername + " (" + email + ") vient de créer un compte.";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "USER", title, message, "", false, false
        );
        dao.addNotification(n);
    }

    /**
     * Appelé depuis AbonnementService quand un user souscrit ou renouvelle.
     *
     * @param username    pseudo de l'abonné
     * @param planName    nom du plan (ex: "Premium", "Standard")
     */
    public void notifyNewSubscription(String username, String planName) {
        String title   = "💳 Nouvel abonnement";
        String message = username + " a souscrit au plan \"" + planName + "\".";
        Notification n = new Notification(
            0, ADMIN_USER_ID, "SUBSCRIPTION", title, message, "", false, false
        );
        dao.addNotification(n);
    }

    /**
     * Appelé quand un utilisateur tente de se connecter plusieurs fois sans succès.
     *
     * @param username  pseudo ou email concerné
     * @param attempts  nombre de tentatives
     */
    public void notifyLoginFailure(String username, int attempts) {
        String title   = "🔒 Tentatives de connexion suspectes";
        String message = attempts + " tentatives échouées pour le compte : " + username;
        Notification n = new Notification(
            0, ADMIN_USER_ID, "SECURITY", title, message, "", true, false
        );
        dao.addNotification(n);
    }

    // ─────────────────────────────────────────────
    //  Utilitaire
    // ─────────────────────────────────────────────
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
    public boolean userExists(int userId) {
    	return dao.userExists(userId);
    }
 // ✅ Compter TOUTES les notifications (admin voit tout)
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