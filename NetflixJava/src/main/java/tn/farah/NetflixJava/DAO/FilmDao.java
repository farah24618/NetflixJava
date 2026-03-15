import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import tn.farah.NetflixJava.Entities.AgeRating;
import tn.farah.NetflixJava.Entities.Film;

public class FilmDao {
    private Connection connection;

    public FilmDao(Connection connection) {
        this.connection = connection;
    }

    // --- CREATE ---
    public void create(Film film) throws SQLException {
        String queryMedia = "INSERT INTO media (titre, synopsis, casting, date_sortie, url_cover, url_banner, url_teaser, age_rating, type_media) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String queryFilm = "INSERT INTO film (id_media, url_video, duree, nbre_vue) VALUES (?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            int generatedId = 0;

            try (PreparedStatement psM = connection.prepareStatement(queryMedia, Statement.RETURN_GENERATED_KEYS)) {
                psM.setString(1, film.getTitre());
                psM.setString(2, film.getSynopsis());
                psM.setString(3, film.getCasting());
                psM.setDate(4, Date.valueOf(film.getDateSortie()));
                psM.setString(5, film.getUrlImageCover());
                psM.setString(6, film.getUrlImageBanner());
                psM.setString(7, film.getUrlTeaser());
                psM.setString(8, film.getAgeRating().name());
                psM.setString(9, "FILM"); 
                psM.executeUpdate();

                ResultSet rs = psM.getGeneratedKeys();
                if (rs.next()) generatedId = rs.getInt(1);
                film.setId(generatedId);
            }

            try (PreparedStatement psF = connection.prepareStatement(queryFilm)) {
                psF.setInt(1, generatedId);
                psF.setString(2, film.getUrlVedio());
                psF.setInt(3, film.getDuree());
                psF.setInt(4, film.getNbreVue());
                psF.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // --- READ ALL ---
    public List<Film> findAll() throws SQLException {
        List<Film> films = new ArrayList<>();
        String query = "SELECT * FROM media m JOIN film f ON m.id = f.id_media";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                films.add(mapResultSetToFilm(rs));
            }
        }
        return films;
    }

    // --- UPDATE ---
    public void update(Film film) throws SQLException {
        String updateMedia = "UPDATE media SET titre=?, synopsis=?, casting=?, date_sortie=?, url_cover=?, url_banner=?, url_teaser=?, age_rating=? WHERE id=?";
        String updateFilm = "UPDATE film SET url_video=?, duree=?, nbre_vue=? WHERE id_media=?";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement psM = connection.prepareStatement(updateMedia)) {
                psM.setString(1, film.getTitre());
                psM.setString(2, film.getSynopsis());
                psM.setString(3, film.getCasting());
                psM.setDate(4, Date.valueOf(film.getDateSortie()));
                psM.setString(5, film.getUrlImageCover());
                psM.setString(6, film.getUrlImageBanner());
                psM.setString(7, film.getUrlTeaser());
                psM.setString(8, film.getAgeRating().name());
                psM.setInt(9, film.getId());
                psM.executeUpdate();
            }
            try (PreparedStatement psF = connection.prepareStatement(updateFilm)) {
                psF.setString(1, film.getUrlVedio());
                psF.setInt(2, film.getDuree());
                psF.setInt(3, film.getNbreVue());
                psF.setInt(4, film.getId());
                psF.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // --- DELETE ---
    public void delete(int id) throws SQLException {
        // Grâce au "ON DELETE CASCADE" en SQL, supprimer le média supprimera le film automatiquement
        String query = "DELETE FROM media WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // --- RECHERCHE PAR ID ---
    public Film findById(int id) throws SQLException {
        String query = "SELECT * FROM media m JOIN film f ON m.id = f.id_media WHERE m.id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToFilm(rs);
            }
        }
        return null;
    }
    public List<Film> findByCategory(int categoryId) throws SQLException {
        List<Film> films = new ArrayList<>();
        String query = "SELECT m.*, f.* FROM media m " +
                       "JOIN film f ON m.id = f.id_media " +
                       "JOIN film_categories fc ON m.id = fc.film_id " +
                       "WHERE fc.category_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    films.add(mapResultSetToFilm(rs));
                }
            }
        }
        return films;
    }
    public List<Film> findByYear(int year) throws SQLException {
        List<Film> films = new ArrayList<>();
        // Utilisation de la fonction YEAR() de SQL
        String query = "SELECT m.*, f.* FROM media m " +
                       "JOIN film f ON m.id = f.id_media " +
                       "WHERE YEAR(m.date_sortie) = ?"+
                       "ORDER BY m.date_sortie DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    films.add(mapResultSetToFilm(rs));
                }
            }
        }
        return films;
    }
    public List<Film> findByTitle(String title) throws SQLException {
        List<Film> films = new ArrayList<>();
        String query = "SELECT m.*, f.* FROM media m " +
                       "JOIN film f ON m.id = f.id_media " +
                       "WHERE m.titre LIKE ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, "%" + title + "%"); // Recherche partielle
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    films.add(mapResultSetToFilm(rs));
                }
            }
        }
        return films;
    }
    public List<Film> findByManyCategories(List<Integer> categoryIds) throws SQLException {
        List<Film> films = new ArrayList<>();
        if (categoryIds == null || categoryIds.isEmpty()) return findAll();

        // Crée une chaîne de "?" selon le nombre d'IDs (ex: "?, ?, ?")
        String placeholders = categoryIds.stream()
                                         .map(id -> "?")
                                         .collect(Collectors.joining(", "));

        String query = "SELECT DISTINCT m.*, f.* FROM media m " +
                       "JOIN film f ON m.id = f.id_media " +
                       "JOIN film_categories fc ON m.id = fc.film_id " +
                       "WHERE fc.category_id IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (int i = 0; i < categoryIds.size(); i++) {
                ps.setInt(i + 1, categoryIds.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    films.add(mapResultSetToFilm(rs));
                }
            }//hedhi
        }
        return films;
    }
    // --- MAPPING ---
    private Film mapResultSetToFilm(ResultSet rs) throws SQLException {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setTitre(rs.getString("titre"));
        f.setSynopsis(rs.getString("synopsis"));
        f.setCasting(rs.getString("casting"));
        f.setDateSortie(rs.getDate("date_sortie").toLocalDate());
        f.setUrlImageCover(rs.getString("url_cover"));
        f.setUrlImageBanner(rs.getString("url_banner"));
        f.setUrlTeaser(rs.getString("url_teaser"));
        f.setRatingMoyen(rs.getDouble("rating_moyen"));
        f.setAgeRating(AgeRating.valueOf(rs.getString("age_rating")));
        f.setUrlVedio(rs.getString("url_video"));
        f.setDuree(rs.getInt("duree"));
        f.setNbreVue(rs.getInt("nbre_vue"));
        return f;
    }
}