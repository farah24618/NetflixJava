package tn.farah.NetflixJava.utils;

import tn.farah.NetflixJava.Entities.User;

public class SessionData {

    // Variable statique pour garder l'utilisateur en mémoire
    private static User currentUser;
    
    private static String forfaitNom = "Standard";
    private static String forfaitPrix = "45,86 DT";

    public static String getForfaitNom() { return forfaitNom; }
    public static String getForfaitPrix() { return forfaitPrix; }

    public static void setForfait(String nom, String prix) {
        forfaitNom = nom;
        forfaitPrix = prix;
    }
    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}