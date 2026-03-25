package tn.farah.NetflixJava.Service;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import tn.farah.NetflixJava.DAO.UserDao;
import tn.farah.NetflixJava.Entities.User;

public class UserService {

    private UserDao userDao;

    public UserService(Connection connexion) {
        this.userDao = new UserDao(connexion);
    }

    // 1️⃣ INSCRIPTION
    public boolean registerUser(User user) {
<<<<<<< HEAD
=======
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
>>>>>>> branch 'master' of https://github.com/farah24618/NetflixJava.git

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return false;
        if (!user.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) return false;
        if (user.getPasswordHash() == null || user.getPasswordHash().length() < 6) return false;
        if (user.getBirthDate() == null || user.getBirthDate().isAfter(java.time.LocalDate.now())) return false;
        if (user.getPhone() != null && !user.getPhone().matches("^\\+?[0-9]{8,15}$")) return false;
        if (userDao.findByEmail(user.getEmail()) != null) return false;

        return userDao.addUser(user);
    }

    // 2️⃣ LOGIN
    public User loginUser(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) return null;
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) return null;

        return userDao.login(email, password);
    }

    // 3️⃣ GET ALL USERS
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    // 4️⃣ DELETE USER
    public boolean deleteUser(int id) {
        if (id <= 0) return false;
        return userDao.deleteUser(id);
    }
}