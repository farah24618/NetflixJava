package tn.farah.NetflixJava.utils;

public class SessionData {
    private static String forfaitNom = "Standard";
    private static String forfaitPrix = "45,86 DT";

    public static String getForfaitNom() { return forfaitNom; }
    public static String getForfaitPrix() { return forfaitPrix; }

    public static void setForfait(String nom, String prix) {
        forfaitNom = nom;
        forfaitPrix = prix;
    }
}