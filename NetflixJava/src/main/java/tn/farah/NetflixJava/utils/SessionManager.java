package tn.farah.NetflixJava.utils;

import tn.farah.NetflixJava.Entities.User;

/**
 * Singleton global — conserve l'utilisateur connecté pendant toute la session.
 * Usage :  SessionManager.getInstance().getCurrentUser()
 */
public class SessionManager {

    // ── Singleton ──────────────────────────────────────────────────────────
    private static SessionManager instance;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    private User currentUser;

    /** Appelé juste après l'authentification réussie */
    public void login(User user) {
        this.currentUser = user;
    }

    /** Appelé lors de la déconnexion */
    public void logout() {
        this.currentUser = null;
    }

    /** Retourne l'utilisateur connecté, ou null si personne n'est connecté */
    public User getCurrentUser() {
        return currentUser;
    }

    /** Raccourci : ID de l'utilisateur connecté (-1 si non connecté) */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    /** Vrai si quelqu'un est connecté */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Vrai si l'utilisateur connecté est admin */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}