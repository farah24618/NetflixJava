package tn.farah.NetflixJava.utils;

public class PreferencesStore {
	private static final java.util.prefs.Preferences PREFS =
            java.util.prefs.Preferences.userRoot().node("rekchanet/login");
        private static final String KEY_EMAIL = "remembered_email";

        public static void saveEmail(String email) { PREFS.put(KEY_EMAIL, email); }
        public static void clearEmail()            { PREFS.remove(KEY_EMAIL); }
        public static String getSavedEmail()       { return PREFS.get(KEY_EMAIL, ""); }
    }

