package tn.farah.NetflixJava.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.utils.ConxDB;


public class SaisonDAO {
		private static Connection conn=ConxDB.getInstance();
		public static List<Saison> findAll(){
			Statement stml=null;
			ResultSet rs=null;
			List<Saison> saisons=new ArrayList<>();
			String SQL="SELECT * FROM saison";
			try {
				stml=conn.createStatement();
				rs=stml.executeQuery(SQL);
				while(rs.next()) {
					int id=rs.getInt(1);
					int idSerie=rs.getInt(2);
					int numeroSaison=rs.getInt(3);
					Saison saison=new Saison(id,idSerie,numeroSaison);
					saisons.add(saison);
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
			return saisons;
		}
		public static int save(Saison saison) {
			int saisonId =0;
			PreparedStatement pstml=null;
			ResultSet rs=null;
			try {
				String sql="INSERT INTO saison(idSerie,numeroSaison) VALUES (?,?)";
				pstml =conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				pstml.setInt(1, saison.getIdSerie());
				pstml.setInt(2, saison.getNumeroSaison());
				pstml.executeUpdate();
				rs=pstml.getGeneratedKeys();
				if(rs.next()) {
					saisonId=rs.getInt(1);
				}
			}catch(SQLException ex) {
				System.out.println(ex.getMessage());
			}
			return saisonId;
		}
	    // ─────────────────────────────────
	    //  FIND BY ID
	    // ─────────────────────────────────
	    public static Saison findById(int id) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        String sql = "SELECT * FROM saison WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, id);
	            rs = pstml.executeQuery();
	            if (rs.next()) {
	                int idSerie = rs.getInt(2);
	                int numeroSaison = rs.getInt(3);
	                return new Saison(id, idSerie, numeroSaison);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // ─────────────────────────────────
	    //  FIND BY SERIE
	    //  toutes les saisons d'une série
	    // ─────────────────────────────────
	    public static List<Saison> findBySerie(int idSerie) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        List<Saison> saisons = new ArrayList<>();
	        String sql = "SELECT * FROM saison WHERE idSerie = ? ORDER BY numeroSaison ASC";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, idSerie);
	            rs = pstml.executeQuery();
	            while (rs.next()) {
	                int id = rs.getInt(1);
	                int numeroSaison = rs.getInt(3);
	                saisons.add(new Saison(id, idSerie, numeroSaison));
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return saisons;
	    }

	    // ─────────────────────────────────
	    //  UPDATE
	    // ─────────────────────────────────
	    public static void update(Saison saison) {
	        PreparedStatement pstml = null;
	        String sql = "UPDATE saison SET idSerie = ?, numeroSaison = ? WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, saison.getIdSerie());
	            pstml.setInt(2, saison.getNumeroSaison());
	            pstml.setInt(3, saison.getId());
	            pstml.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    // ─────────────────────────────────
	    //  DELETE
	    // ─────────────────────────────────
	    public static void delete(int id) {
	        PreparedStatement pstml = null;
	        String sql = "DELETE FROM saison WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, id);
	            pstml.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    // ─────────────────────────────────
	    //  COUNT BY SERIE
	    //  nombre de saisons d'une série
	    // ─────────────────────────────────
	    public static int countBySerie(int idSerie) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        String sql = "SELECT COUNT(*) FROM saison WHERE idSerie = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, idSerie);
	            rs = pstml.executeQuery();
	            if (rs.next()) {
					return rs.getInt(1);
				}
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return 0;
	    }


}
