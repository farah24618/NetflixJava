package tn.farah.NetflixJava.DAO;

import tn.farah.NetflixJava.Entities.AdminStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardDAO {

    private final Connection connection;

    public AdminDashboardDAO(Connection connection) {
        this.connection = connection;
    }

    public AdminStats getDashboardStats() {
        AdminStats stats = new AdminStats();

        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM film) AS nb_films,
                    (SELECT COUNT(*) FROM serie) AS nb_series,
                    (SELECT COUNT(*) FROM episode) AS nb_episodes,
                    (SELECT COUNT(*) FROM user) AS nb_users,
                    (SELECT COUNT(*) FROM comment) AS nb_comments,
                    (SELECT COUNT(*) FROM favorite) AS nb_favorites
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.setNbFilms(rs.getInt("nb_films"));
                stats.setNbSeries(rs.getInt("nb_series"));
                stats.setNbEpisodes(rs.getInt("nb_episodes"));
                stats.setNbUsers(rs.getInt("nb_users"));
                stats.setNbComments(rs.getInt("nb_comments"));
                stats.setNbFavorites(rs.getInt("nb_favorites"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }
}