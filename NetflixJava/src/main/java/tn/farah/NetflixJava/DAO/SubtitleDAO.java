package tn.farah.NetflixJava.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Subtitle;
import tn.farah.NetflixJava.utils.ConxDB;

public class SubtitleDAO {
	 private static Connection conn = ConxDB.getInstance();

	    // ─────────────────────────────────
	    //  FIND ALL
	    // ─────────────────────────────────
	    public static List<Subtitle> findAll() {
	        Statement stml = null;
	        ResultSet rs = null;
	        List<Subtitle> subtitles = new ArrayList<Subtitle>();
	        String SQL = "SELECT * FROM subtitle";
	        try {
	            stml = conn.createStatement();
	            rs = stml.executeQuery(SQL);
	            while (rs.next()) {
	                int id = rs.getInt(1);
	                String langage = rs.getString(2);
	                int idMedia = rs.getInt(3);
	                String url = rs.getString(4);
	                Subtitle subtitle = new Subtitle(id, langage, idMedia, url);
	                subtitles.add(subtitle);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return subtitles;
	    }

	    // ─────────────────────────────────
	    //  SAVE (INSERT)
	    // ─────────────────────────────────
	    public static int save(Subtitle subtitle) {
	        int subtitleId = 0;
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        try {
	            String sql = "INSERT INTO subtitle(langage, idMedia, url) VALUES (?,?,?)";
	            pstml = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	            pstml.setString(1, subtitle.getLangage());
	            pstml.setInt(2, subtitle.getIdMedia());
	            pstml.setString(3, subtitle.getUrl());
	            pstml.executeUpdate();
	            rs = pstml.getGeneratedKeys();
	            if (rs.next())
	                subtitleId = rs.getInt(1);
	        } catch (SQLException ex) {
	            System.out.println(ex.getMessage());
	        }
	        return subtitleId;
	    }

	    // ─────────────────────────────────
	    //  FIND BY ID
	    // ─────────────────────────────────
	    public static Subtitle findById(int id) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        String sql = "SELECT * FROM subtitle WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, id);
	            rs = pstml.executeQuery();
	            if (rs.next()) {
	                String langage = rs.getString(2);
	                int idMedia = rs.getInt(3);
	                String url = rs.getString(4);
	                return new Subtitle(id, langage, idMedia, url);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // ─────────────────────────────────
	    //  FIND BY MEDIA
	    //  tous les sous-titres d'un media
	    // ─────────────────────────────────
	    public static List<Subtitle> findByMedia(int idMedia) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        List<Subtitle> subtitles = new ArrayList<Subtitle>();
	        String sql = "SELECT * FROM subtitle WHERE idMedia = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, idMedia);
	            rs = pstml.executeQuery();
	            while (rs.next()) {
	                int id = rs.getInt(1);
	                String langage = rs.getString(2);
	                String url = rs.getString(4);
	                subtitles.add(new Subtitle(id, langage, idMedia, url));
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return subtitles;
	    }

	    // ─────────────────────────────────
	    //  FIND BY MEDIA + LANGAGE
	    //  sous-titre précis pour le player
	    // ─────────────────────────────────
	    public static Subtitle findByMediaAndLangage(int idMedia, String langage) {
	        PreparedStatement pstml = null;
	        ResultSet rs = null;
	        String sql = "SELECT * FROM subtitle WHERE idMedia = ? AND langage = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, idMedia);
	            pstml.setString(2, langage);
	            rs = pstml.executeQuery();
	            if (rs.next()) {
	                int id = rs.getInt(1);
	                String url = rs.getString(4);
	                return new Subtitle(id, langage, idMedia, url);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    // ─────────────────────────────────
	    //  UPDATE
	    // ─────────────────────────────────
	    public static void update(Subtitle subtitle) {
	        PreparedStatement pstml = null;
	        String sql = "UPDATE subtitle SET langage = ?, idMedia = ?, url = ? WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setString(1, subtitle.getLangage());
	            pstml.setInt(2, subtitle.getIdMedia());
	            pstml.setString(3, subtitle.getUrl());
	            pstml.setInt(4, subtitle.getId());
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
	        String sql = "DELETE FROM subtitle WHERE id = ?";
	        try {
	            pstml = conn.prepareStatement(sql);
	            pstml.setInt(1, id);
	            pstml.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
}
