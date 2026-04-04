package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.util.List;
import tn.farah.NetflixJava.DAO.NotificationDAO;
import tn.farah.NetflixJava.Entities.Notification; // <-- C'est LUI le bon import !

public class NotificationService {

    private NotificationDAO dao;
    public NotificationService(Connection c) {
    	dao=new NotificationDAO(c);
    }

    // 🔄 récupérer toutes les notifications d’un utilisateur
    public List<Notification> getUserNotifications(int userId) {
        return dao.getUserNotifications(userId);
    }

    // 🔴 compter les non lues
    public int countUnread(int userId) {
        return dao.countUnread(userId);
    }

    // ✔️ marquer comme lu
    public void markAsRead(int notificationId) {
        dao.markAsRead(notificationId);
    }

    // ➕ ajouter une notification (like, commentaire...)
    public void addNotification(Notification notification) {
        dao.addNotification(notification);
    }
    public int countAll(int userId) {
        return dao.countAll(userId);
    }
}