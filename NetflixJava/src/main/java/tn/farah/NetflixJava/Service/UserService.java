package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.UserDao;
import tn.farah.NetflixJava.Entities.User;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

public class UserService {

    private UserDao userDao;

    public UserService(Connection connexion) {
        this.userDao = new UserDao(connexion);
    }

    // 1️⃣ INSCRIPTION
    public boolean registerUser(User user) {

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