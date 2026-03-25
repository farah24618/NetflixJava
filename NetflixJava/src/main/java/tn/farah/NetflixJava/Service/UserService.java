package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.util.List;

import tn.farah.NetflixJava.DAO.UserDao;
import tn.farah.NetflixJava.Entities.User;

public class UserService {

    // Le Service a besoin du DAO pour parler à la base de données
    private UserDao userDao;
    private Connection connexion;

    // Constructeur
    public UserService(Connection connexion) {
        this.userDao = new UserDao(connexion);
    }

    // 1. INSCRIPTION (Logique métier pour ajouter un utilisateur)
    public boolean registerUser(User user) {
        // VÉRIFICATION 1 : L'email ou le mot de passe sont-ils vides ?
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            System.out.println("Erreur : L'email est obligatoire.");
            return false;
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().length() < 4) {
            System.out.println("Erreur : Le mot de passe doit faire au moins 4 caractères.");
            return false;
        }

        // VÉRIFICATION 2 : L'email a-t-il un bon format ? (contient un @)
        if (!user.getEmail().contains("@")) {
            System.out.println("Erreur : Format d'email invalide.");
            return false;
        }

        // Si toutes les règles sont respectées, on autorise le DAO à l'ajouter en base !
        return userDao.addUser(user);
    }

    // 2. CONNEXION (Logique métier pour le Login)
    public User loginUser(String email, String password) {
        if (email == null || password == null) {
            System.out.println("Erreur : Veuillez remplir tous les champs.");
            return null;
        }

        // On demande au DAO de vérifier dans la base de données
        User user = userDao.login(email, password);

        if (user == null) {
            System.out.println("Erreur : Email ou mot de passe incorrect.");
        } else if (!user.isActive()) {
            System.out.println("Erreur : Ce compte a été désactivé !");
            return null; // On bloque la connexion si le compte n'est pas actif
        } else {
            System.out.println("Connexion réussie ! Bienvenue " + user.getFirstName());
        }

        return user;
    }

    // 3. RÉCUPÉRER TOUS LES UTILISATEURS
    public List<User> getAllUsers() {
        // Ici, pas de règle stricte, on demande juste au DAO de faire son travail
        return userDao.getAllUsers();
    }

    // 4. SUPPRIMER UN UTILISATEUR
    public boolean deleteUser(int id) {
        if (id <= 0) {
            System.out.println("Erreur : ID utilisateur invalide.");
            return false;
        }
        return userDao.deleteUser(id);
    }
}