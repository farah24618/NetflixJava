package tn.farah.NetflixJava.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import tn.farah.NetflixJava.Entities.Rating;

public class RatingDao {

    private Connection connection;

    public RatingDao(Connection connection) {
        this.connection = connection;
    }

    public boolean addRating(Rating rating) {
        String sql = "INSERT INTO rating (user_id, media_id, note) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, rating.getUserId());
            pstmt.setInt(2, rating.getFilmId());
            pstmt.setInt(3, rating.getScore());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Rating> getRatingsByFilmId(int filmId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM rating WHERE media_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, filmId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ratings.add(mapResultSetToRating(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ratings;
    }

    public double getAverageScoreForFilm(int filmId) {
        String sql = "SELECT AVG(note) as moyenne FROM rating WHERE media_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, filmId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Rating mapResultSetToRating(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setUserId(rs.getInt("user_id"));
        rating.setFilmId(rs.getInt("media_id"));
        rating.setScore(rs.getInt("note"));
        return rating;
    }
    public boolean hasRated(int userId, int mediaId) {
        String sql = "SELECT COUNT(*) FROM rating WHERE user_id = ? AND media_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, mediaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
