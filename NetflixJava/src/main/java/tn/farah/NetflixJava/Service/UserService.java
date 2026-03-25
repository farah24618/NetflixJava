package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.UserDao;
import tn.farah.NetflixJava.Entities.User;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

public class UserService {

    private UserDao userDao;

    // 🔹 Constructeur
    public UserService(Connection connexion) {
        this.userDao = new UserDao(connexion);
    }

    // 🔹 1. INSCRIPTION
    public boolean registerUser(User user) {

        // ✅ Email obligatoire
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            System.out.println("Erreur : L'email est obligatoire.");
            return false;
        }

        // ✅ Format email
        if (!user.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("Erreur : Format d'email invalide.");
            return false;
        }

        // ✅ Mot de passe
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            System.out.println("Erreur : Mot de passe trop court (min 6 caractères).");
            return false;
        }

        // ✅ Téléphone (optionnel mais recommandé)
        if (user.getPhone() != null && !user.getPhone().matches("^\\+?[0-9]{8,15}$")) {
            System.out.println("Erreur : Numéro de téléphone invalide.");
            return false;
        }

        // ✅ Date de naissance obligatoire
        if (user.getBirthDate() == null) {
            System.out.println("Erreur : Date de naissance obligatoire.");
            return false;
        }

        // ✅ Vérifier date future
        if (user.getBirthDate().after(new Date())) {
            System.out.println("Erreur : Date de naissance invalide.");
            return false;
        }

        // ✅ Vérifier âge minimum (13 ans)
        long age = (System.currentTimeMillis() - user.getBirthDate().getTime()) 
                    / (1000L * 60 * 60 * 24 * 365);

        if (age < 13) {
            System.out.println("Erreur : âge minimum requis = 13 ans.");
            return false;
        }

        // ✅ Vérifier si email existe déjà (optionnel si DAO le gère)
        if (userDao.findByEmail(user.getEmail()) != null) {
            System.out.println("Erreur : Email déjà utilisé.");
            return false;
        }

        // ✅ Insertion en base
        return userDao.addUser(user);
    }

    // 🔹 2. LOGIN
    public User loginUser(String email, String password) {

        // ✅ Champs vides
        if (email == null || password == null
                || email.trim().isEmpty() || password.isEmpty()) {

            System.out.println("Erreur : Veuillez remplir tous les champs.");
            return null;
        }

        // ✅ Format email
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            System.out.println("Erreur : Email invalide.");
            return null;
        }

        // 🔹 Vérification BD
        User user = userDao.login(email, password);

        if (user == null) {
            System.out.println("Erreur : Email ou mot de passe incorrect.");
            return null;
        }

        if (!user.isActive()) {
            System.out.println("Erreur : Compte désactivé.");
            return null;
        }

        System.out.println("Connexion réussie ! Bienvenue " + user.getFirstName());
        return user;
    }

    // 🔹 3. GET ALL USERS
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    // 🔹 4. DELETE USER
    public boolean deleteUser(int id) {

        if (id <= 0) {
            System.out.println("Erreur : ID utilisateur invalide.");
            return false;
        }

        return userDao.deleteUser(id);
    }
}