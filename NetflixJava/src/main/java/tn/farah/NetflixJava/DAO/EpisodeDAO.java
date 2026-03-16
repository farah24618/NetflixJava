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
				int durreeIntro=rs.getInt(9);
				Episode episode=new Episode(id,saisonId,titre,numeroEpisode,videoUrl,duree,resume,miniatureUrl,durreeIntro);
				episodes.add(episode);
			}
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
			String sql="INSERT INTO episode(saisonId,titre,numeroEpisode,videoUrl,duree,resume,miniatureUrl,durreeIntro) VALUES (?,?,?,?,?,?,?,?)";
			pstml =conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstml.setInt(1, episode.getSaisonId());
			pstml.setString(2, episode.getTitre());
			pstml.setInt(3, episode.getNumeroEpisode());
			pstml.setString(4, episode.getVideoUrl());
			pstml.setInt(5, episode.getDuree());
			pstml.setString(6, episode.getResume());
			pstml.setString(7, episode.getMiniatureUrl());
			pstml.setInt(8, episode.getDurreeIntro());
			pstml.executeUpdate();
			rs=pstml.getGeneratedKeys();
			if(rs.next()) 
				episodeId=rs.getInt(1);
		}catch(SQLException ex) {
			System.out.println(ex.getMessage());
		}
		return episodeId;
	}
	
	
	 // ─────────────────────────────────
    //  FIND BY ID
    // ─────────────────────────────────
    public static Episode findById(int id) {
        PreparedStatement pstml = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM episode WHERE id = ?";
        try {
            pstml = conn.prepareStatement(sql);
            pstml.setInt(1, id);
            rs = pstml.executeQuery();
            if (rs.next()) {
                int saisonId = rs.getInt(2);
                String titre = rs.getString(3);
                int numeroEpisode = rs.getInt(4);
                String videoUrl = rs.getString(5);
                int duree = rs.getInt(6);
                String resume = rs.getString(7);
                String miniatureUrl = rs.getString(8);
                int dureeIntro = rs.getInt(9);
                return new Episode(id, saisonId, titre, numeroEpisode,
                                   videoUrl, duree, resume, miniatureUrl, dureeIntro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────
    //  FIND BY SAISON
    //  tous les épisodes d'une saison
    // ─────────────────────────────────
    public static List<Episode> findBySaison(int saisonId) {
        PreparedStatement pstml = null;
        ResultSet rs = null;
        List<Episode> episodes = new ArrayList<Episode>();
        String sql = "SELECT * FROM episode WHERE saisonId = ? ORDER BY numeroEpisode ASC";
        try {
            pstml = conn.prepareStatement(sql);
            pstml.setInt(1, saisonId);
            rs = pstml.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String titre = rs.getString(3);
                int numeroEpisode = rs.getInt(4);
                String videoUrl = rs.getString(5);
                int duree = rs.getInt(6);
                String resume = rs.getString(7);
                String miniatureUrl = rs.getString(8);
                int dureeIntro = rs.getInt(9);
                episodes.add(new Episode(id, saisonId, titre, numeroEpisode,
                                         videoUrl, duree, resume, miniatureUrl, dureeIntro));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return episodes;
    }

    // ─────────────────────────────────
    //  FIND NEXT EPISODE
    //  prochain épisode (binge-watching)
    // ─────────────────────────────────
    public static Episode findNextEpisode(int saisonId, int numeroEpisode) {
        PreparedStatement pstml = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM episode WHERE saisonId = ? " +
                     "AND numeroEpisode = ?";
        try {
            pstml = conn.prepareStatement(sql);
            pstml.setInt(1, saisonId);
            pstml.setInt(2, numeroEpisode + 1);  // épisode suivant
            rs = pstml.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                String titre = rs.getString(3);
                String videoUrl = rs.getString(5);
                int duree = rs.getInt(6);
                String resume = rs.getString(7);
                String miniatureUrl = rs.getString(8);
                int dureeIntro = rs.getInt(9);
                return new Episode(id, saisonId, titre, numeroEpisode + 1,
                                   videoUrl, duree, resume, miniatureUrl, dureeIntro);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // pas d'épisode suivant = fin de saison
    }

    // ─────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────
    public static void update(Episode episode) {
        PreparedStatement pstml = null;
        String sql = "UPDATE episode SET saisonId = ?, titre = ?, numeroEpisode = ?, " +
                     "videoUrl = ?, duree = ?, resume = ?, miniatureUrl = ?, " +
                     "dureeIntro = ? WHERE id = ?";
        try {
            pstml = conn.prepareStatement(sql);
            pstml.setInt(1, episode.getSaisonId());
            pstml.setString(2, episode.getTitre());
            pstml.setInt(3, episode.getNumeroEpisode());
            pstml.setString(4, episode.getVideoUrl());
            pstml.setInt(5, episode.getDuree());
            pstml.setString(6, episode.getResume());
            pstml.setString(7, episode.getMiniatureUrl());
            pstml.setInt(8, episode.getDurreeIntro());
            pstml.setInt(9, episode.getId());
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
        String sql = "DELETE FROM episode WHERE id = ?";
        try {
            pstml = conn.prepareStatement(sql);
            pstml.setInt(1, id);
            pstml.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────
    //  COUNT BY SAISON
    //  nombre d'épisodes d'une saison
    // ─────────────────────────────────
    public static int countBySaison(int saisonId) {
        PreparedStatement pstml = null;
        ResultSet rs = null;
        String sql = "SELECT COUNT(*) FROM episode WHERE saisonId = ?";
        try {
            pstml = conn.prepareStatement(sql);
            pstml.setInt(1, saisonId);
            rs = pstml.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
