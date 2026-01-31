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
					int mediaId=rs.getInt(2);
					int numeroSaison=rs.getInt(3);
					Saison saison=new Saison(id,mediaId,numeroSaison);
					saisons.add(saison);
				}
				conn.close();
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
				String sql="INSERT INTO saison(mediaId,numeroSaison) VALUES (?,?)";
				pstml =conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				pstml.setInt(1, saison.getMediaId());
				pstml.setInt(2, saison.getNumeroSaison());
				pstml.executeUpdate();
				rs=pstml.getGeneratedKeys();
				if(rs.next()) 
					saisonId=rs.getInt(1);
			}catch(SQLException ex) {
				System.out.println(ex.getMessage());
			}
			return saisonId;
		}

	

}
