package tn.farah.NetflixJava.utils;

import tn.farah.NetflixJava.Entities.User;

public class SessionData {
	
	private static String carteNumero;
	private static String carteExpiration;
	private static String carteCvv;
	private static String carteNom;

	public static String getCarteNumero() { return carteNumero; }
	public static void setCarteNumero(String v) { carteNumero = v; }

	public static String getCarteExpiration() { return carteExpiration; }
	public static void setCarteExpiration(String v) { carteExpiration = v; }

	public static String getCarteCvv() { return carteCvv; }
	public static void setCarteCvv(String v) { carteCvv = v; }

	public static String getCarteNom() { return carteNom; }
	public static void setCarteNom(String v) { carteNom = v; }
   
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