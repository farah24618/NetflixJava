package tn.farah.NetflixJava.DAO;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.utils.ConxDB;
public class EpisodeDAO {
	private static Connection conn=ConxDB.getInstance();
	public static List<Episode> findAll(){
		Statement stml=null;
		ResultSet rs=null;
		List<Episode> episodes=new ArrayList<Episode>();
		String SQL="SELECT * FROM episode";
		try {
			stml=conn.createStatement();
			rs=stml.executeQuery(SQL);
			while(rs.next()) {
				int id=rs.getInt(1);
				int saisonId=rs.getInt(2);
				String titre=rs.getString(3);
				int numeroEpisode=rs.getInt(4);
				String videoUrl=rs.getString(5);
				int duree=rs.getInt(6);
				String resume=rs.getString(7);
				String miniatureUrl=rs.getString(8);
				Episode episode=new Episode(id,saisonId,titre,numeroEpisode,videoUrl,duree,resume,miniatureUrl);
				episodes.add(episode);
			}
			conn.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return episodes;
	}
	public static int save(Episode episode) {
		int episodeId =0;
		PreparedStatement pstml=null;
		ResultSet rs=null;
		try {
			String sql="INSERT INTO episode(saisonId,titre,numeroEpisode,videoUrl,duree,resume,miniatureUrl) VALUES (?,?,?,?,?,?,?)";
			pstml =conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstml.setInt(1, episode.getSaisonId());
			pstml.setString(2, episode.getTitre());
			pstml.setInt(3, episode.getNumeroEpisode());
			pstml.setString(4, episode.getVideoUrl());
			pstml.setInt(5, episode.getDuree());
			pstml.setString(6, episode.getResume());
			pstml.setString(7, episode.getMiniatureUrl());
			pstml.executeUpdate();
			rs=pstml.getGeneratedKeys();
			if(rs.next()) 
				episodeId=rs.getInt(1);
		}catch(SQLException ex) {
			System.out.println(ex.getMessage());
		}
		return episodeId;
	}

}
