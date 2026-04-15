package tn.farah.NetflixJava.Service;

import tn.farah.NetflixJava.DAO.UserDao;


import tn.farah.NetflixJava.Entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class UserService {

    private UserDao userDao;

    public UserService(Connection c) {
        this.userDao = new UserDao(c);
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

    
    
     // 1️⃣ INSCRIPTION
    public boolean registerUser2(User user) {

        // 🔹 Email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return false;
        if (!user.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) return false;

        // 🔹 Password
        if (user.getPasswordHash() == null || user.getPasswordHash().length() < 6) return false;

        // 🔹 Date de naissance
        if (user.getBirthDate() == null || user.getBirthDate().isAfter(java.time.LocalDate.now())) return false;

        // 🔹 Téléphone
        if (user.getPhone() != null && !user.getPhone().matches("^\\+?[0-9]{8,15}$")) return false;

        // 🔹 NOUVEAU : pseudo obligatoire
        if (user.getPseudo() == null || user.getPseudo().trim().isEmpty()) return false;

        // 🔹 Vérifier unicité email
        if (userDao.findByEmail(user.getEmail()) != null) return false;

        // 🔹 (Optionnel mais conseillé) pseudo unique
        if (userDao.findByPseudo(user.getPseudo()) != null) return false;

        // 🔹 estPaye → pas besoin de validation (boolean)
        // mais tu peux forcer false à l'inscription si tu veux :
        user.setEstPaye(false);

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
 // Ajouter cette méthode
    public boolean updatePaymentStatus(int userId, boolean status) {
        if (userId <= 0) return false;
        return userDao.updatePaymentStatus(userId, status); 
    }

    public User findUserById(int userId) {
    	return userDao.getUserById(userId);}
    public boolean updatePseudo(final int userId, final String newPseudo) {
        return userDao.updatePseudo(userId, newPseudo);
    }
    public boolean isPseudoTaken(final String pseudo, final int userId) {
      
        return userDao.isPseudoTaken(pseudo, userId);}

 // 5️⃣ UPDATE PASSWORD
    public boolean updatePassword(String email, String hashedPass) {
        // Validation de base
        if (email == null || email.trim().isEmpty() || hashedPass == null || hashedPass.isEmpty()) {
            return false;
        }
        
        // Appel au DAO
        return userDao.updatePassword(email, hashedPass);
    }
 // Dans UserService.java
    public boolean updateUser(User user) {
        if (user == null || user.getId() <= 0) return false;
        // Tu peux ajouter ici une validation pour vérifier si le pseudo est déjà pris
        return userDao.updateUser(user);

    }

	public void addAuditLog(int i, String string) {
		userDao.addAuditLog(i, string);
		
	}

	public List<String> getAdminLogs(int id) {
		
		return userDao.getAdminLogs(id);
	}
}