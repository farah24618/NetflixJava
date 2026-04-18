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

   public boolean registerUser(User user) {

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return false;
        if (!user.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) return false;
        if (user.getPasswordHash() == null || user.getPasswordHash().length() < 6) return false;
        if (user.getBirthDate() == null || user.getBirthDate().isAfter(java.time.LocalDate.now())) return false;
        if (user.getPhone() != null && !user.getPhone().matches("^\\+?[0-9]{8,15}$")) return false;
        if (userDao.findByEmail(user.getEmail()) != null) return false;
        
        return userDao.addUser(user);
    }
  
    public boolean registerUser2(User user) {

       
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return false;
        if (!user.getEmail().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) return false;

        if (user.getPasswordHash() == null || user.getPasswordHash().length() < 6) return false;

        if (user.getBirthDate() == null || user.getBirthDate().isAfter(java.time.LocalDate.now())) return false;

        if (user.getPhone() != null && !user.getPhone().matches("^\\+?[0-9]{8,15}$")) return false;

      
        if (user.getPseudo() == null || user.getPseudo().trim().isEmpty()) return false;

        if (userDao.findByEmail(user.getEmail()) != null) return false;

        if (userDao.findByPseudo(user.getPseudo()) != null) return false;

        user.setEstPaye(false);

        return userDao.addUser(user);
    }
   
    public User loginUser(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) return null;
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) return null;

        return userDao.login(email, password);
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public boolean deleteUser(int id) {
        if (id <= 0) return false;
        return userDao.deleteUser(id);
    }

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

    public boolean updatePassword(String email, String hashedPass) {
     
        if (email == null || email.trim().isEmpty() || hashedPass == null || hashedPass.isEmpty()) {
            return false;
        }
        
        return userDao.updatePassword(email, hashedPass);
    }
 
    public boolean updateUser(User user) {
        if (user == null || user.getId() <= 0) return false;
        
        return userDao.updateUser(user);

    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }


	public void addAuditLog(int i, String string) {
		userDao.addAuditLog(i, string);
		
	}

	public List<String> getAdminLogs(int id) {
		
		return userDao.getAdminLogs(id);
	}
	public boolean isBlocked(String email) {
		return userDao.findByEmail(email).isActive();
	}

}