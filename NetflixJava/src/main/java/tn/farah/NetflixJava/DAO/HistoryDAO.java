package tn.farah.NetflixJava.DAO;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.History;
import tn.farah.NetflixJava.utils.ConxDB;
public class HistoryDAO {
		private static Connection conn=ConxDB.getInstance();
		public HistoryDAO(Connection connection) {
	        this.conn= connection;
	    }
		public static List<History> findAll(){
			Statement stml=null;
			ResultSet rs=null;
			List<History> history=new ArrayList<History>();
			String SQL="SELECT * FROM history";
			try {
				stml=conn.createStatement();
				rs=stml.executeQuery(SQL);
				while(rs.next()) {
					int idUser=rs.getInt(1);
					int idMedia=rs.getInt(2);
					LocalDateTime dateVisionnage = rs.getTimestamp(3).toLocalDateTime();
					int tempsArret=rs.getInt(4);
					boolean estTermine=rs.getBoolean(5);
					History historyy=new History(idUser,idMedia,dateVisionnage,tempsArret,estTermine);
					history.add(historyy);
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
			return history;
		}
		public static int save(History history) {
			int historyId =0;
			PreparedStatement pstml=null;
			ResultSet rs=null;
			try {
				String sql="INSERT INTO history(idMedia,dateVisionnage,tempsArret,estTermine) VALUES (?,?,?,?)";
				pstml =conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				pstml.setInt(1,history.getIdUser());
				pstml.setInt(2, history.getIdMedia());
				pstml.setTimestamp(3, Timestamp.valueOf(history.getDateVisionnage()));
				pstml.setInt(4, history.getTempsArret());
				pstml.setBoolean(5, history.getEstTermine());
				pstml.executeUpdate();
				rs=pstml.getGeneratedKeys();
				if(rs.next()) 
					historyId=rs.getInt(1);
			}catch(SQLException ex) {
				System.out.println(ex.getMessage());
			}
			return historyId;
		}

		public static List<History> findByUser(int userId) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        List<History> list = new ArrayList<History>();
	        String sql = "SELECT * FROM history WHERE idUser = ? ORDER BY dateVisionnage DESC";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, userId);
	            rs = pstml.executeQuery();
	            while (rs.next()) {
	                int idUser = rs.getInt(1);
	                int idMedia = rs.getInt(2);
	                LocalDateTime dateVisionnage = rs.getTimestamp(3).toLocalDateTime();
	                int tempsArret = rs.getInt(4);
	                boolean estTermine = rs.getBoolean(5);
	                History historyy = new History(idUser, idMedia, dateVisionnage, tempsArret, estTermine);
	                list.add(historyy);
	            }
	            conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return list;
	    }

	    public static History findById(int id) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        String sql = "SELECT * FROM history WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, id);
	            rs = pstml.executeQuery();
	            if (rs.next()) {
	                int idUser = rs.getInt(1);
	                int idMedia = rs.getInt(2);
	                LocalDateTime dateVisionnage = rs.getTimestamp(3).toLocalDateTime();
	                int tempsArret = rs.getInt(4);
	                boolean estTermine = rs.getBoolean(5);
	                return new History(idUser, idMedia, dateVisionnage, tempsArret, estTermine);
	            }
	            conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    public static History findByUserAndMedia(int userId, int mediaId) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        String sql = "SELECT * FROM history WHERE idUser = ? AND idMedia = ? LIMIT 1";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, userId);
	            pstml.setInt(2, mediaId);
	            rs = pstml.executeQuery();
	            if (rs.next()) {
	                int idUser = rs.getInt(1);
	                int idMedia = rs.getInt(2);
	                LocalDateTime dateVisionnage = rs.getTimestamp(3).toLocalDateTime();
	                int tempsArret = rs.getInt(4);
	                boolean estTermine = rs.getBoolean(5);
	                return new History(idUser, idMedia, dateVisionnage, tempsArret, estTermine);
	            }
	            conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    public static List<Object[]> findTop5MostWatched() {
	        Statement stml = null;
	        ResultSet rs = null;
	        List<Object[]> result = new ArrayList<>();
	        String sql = "SELECT idMedia, COUNT(id) AS nb_vues FROM history " +
	                     "WHERE estTermine = TRUE GROUP BY idMedia " +
	                     "ORDER BY nb_vues DESC LIMIT 5";
	        try {
	            stml = conn.createStatement();
	            rs = stml.executeQuery(sql);
	            while (rs.next()) {
	                result.add(new Object[]{rs.getInt("idMedia"), rs.getInt("nb_vues")});
	            }
	            conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return result;
	    }

	    public static void delete(int id) {
	        PreparedStatement pstml = null;
	        String sql = "DELETE FROM history WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, id);
	            pstml.executeUpdate();
	            conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	


}
