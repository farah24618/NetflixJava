package tn.farah.NetflixJava.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tn.farah.NetflixJava.Entities.AgeRating;
import tn.farah.NetflixJava.Entities.Category;
import tn.farah.NetflixJava.Entities.Warning;
import tn.farah.NetflixJava.Entities.Film;

public class FilmDao {

    private Connection connection;
    public void incrementLike(int commentaireId) {
        String sql = "UPDATE comment SET likes = likes + 1 WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, commentaireId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ FIX 1 : On ajoute m.producteur dans le SELECT de base
    private static final String BASE_SELECT =
            "SELECT m.id AS media_id, m.titre, m.synopsis, m.casting, m.date_sortie, " +
            "m.url_image_cover, m.url_image_banner, m.url_teaser, m.producteur, " +
            "f.url_video, f.duree_minutes, f.nbre_vues, m.rating_moyen, " +
            "ac.label AS age_category_name, " +
            "c.id AS category_id, c.nom AS category_nom, " +
            "w.id AS warning_id, w.label AS warning_desc " +
            "FROM media m " +
            "JOIN film f ON m.id = f.id " +
            "LEFT JOIN age_rating ac ON m.age_rating_id = ac.id " +
            "LEFT JOIN media_category fc ON m.id = fc.media_id " +
            "LEFT JOIN category c ON fc.category_id = c.id " +
            "LEFT JOIN media_warning mw ON m.id = mw.media_id " +
            "LEFT JOIN content_warning w ON mw.warning_id = w.id ";

    public FilmDao(Connection connection) {
        this.connection = connection;
    }

    public void create(Film film) throws SQLException {
        String queryMedia = "INSERT INTO media (titre, synopsis, casting, date_sortie, url_image_cover, url_image_banner, url_teaser, age_rating_id, producteur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String queryFilm  = "INSERT INTO film (id, url_video, duree_minutes, nbre_vues) VALUES (?, ?, ?, ?)";

        try {
            
            int generatedId = 0;

            try (PreparedStatement psM = connection.prepareStatement(queryMedia, Statement.RETURN_GENERATED_KEYS)) {
                psM.setString(1, film.getTitre());
                psM.setString(2, film.getSynopsis());
                psM.setString(3, film.getCasting());
                psM.setDate(4, Date.valueOf(film.getDateSortie()));
                psM.setString(5, film.getUrlImageCover());
                psM.setString(6, film.getUrlImageBanner());
                psM.setString(7, film.getUrlTeaser());
                psM.setInt(8, film.getAgeRating() != null ? film.getAgeRating().getId() : 1);
                psM.setString(9, film.getProducteur());
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

          

            // ✅ FIX 2 : Lier les catégories dans media_category
           
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<Film> findAll() throws SQLException {
        String query = BASE_SELECT + "ORDER BY m.id";
        return executeAndGroup(query, ps -> {});
    }

    public void update(Film film) throws SQLException {
        String updateMedia = "UPDATE media SET titre=?, synopsis=?, casting=?, date_sortie=?, url_image_cover=?, url_image_banner=?, url_teaser=?, age_rating_id=?, producteur=? WHERE id=?";
        String updateFilm  = "UPDATE film SET url_video=?, duree_minutes=?, nbre_vues=? WHERE id=?";

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
                psM.setInt(8, film.getAgeRating().getId());
                psM.setString(9, film.getProducteur()); // ✅ producteur dans update aussi
                psM.setInt(10, film.getId());
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

            // ✅ Mettre à jour les catégories : supprimer les anciennes, réinsérer les nouvelles
            try (PreparedStatement psDel = connection.prepareStatement("DELETE FROM media_category WHERE media_id = ?")) {
                psDel.setInt(1, film.getId());
                psDel.executeUpdate();
            }
            if (film.getGenres() != null) {
                CategoryDAO categoryDAO = new CategoryDAO(connection);
                for (Category cat : film.getGenres()) {
                    categoryDAO.lierMedia(film.getId(), cat.getId());
                }
            }

            // ✅ Mettre à jour les warnings : supprimer les anciens, réinsérer les nouveaux
            try (PreparedStatement psDel = connection.prepareStatement("DELETE FROM media_warning WHERE media_id = ?")) {
                psDel.setInt(1, film.getId());
                psDel.executeUpdate();
            }
            if (film.getWarnings() != null) {
                WarningDao warningDao = new WarningDao(connection);
                for (Warning w : film.getWarnings()) {
                    warningDao.lierMedia(film.getId(), w.getId());
                }
            }

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM media WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Film findById(int id) throws SQLException {
        String query = BASE_SELECT + "WHERE m.id = ?";
        List<Film> films = executeAndGroup(query, ps -> ps.setInt(1, id));
        return films.isEmpty() ? null : films.get(0);
    }

    public List<Film> findByTitle(String title) throws SQLException {
        String query = BASE_SELECT + "WHERE m.titre LIKE ? ORDER BY m.date_sortie DESC";
        return executeAndGroup(query, ps -> ps.setString(1, "%" + title + "%"));
    }

    private List<Film> executeAndGroup(String query, ParamSetter setter) throws SQLException {
        Map<Integer, Film> filmMap = new LinkedHashMap<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mediaId = rs.getInt("media_id");

                    Film f = filmMap.computeIfAbsent(mediaId, k -> {
                        try {
                            Film newFilm = new Film();
                            newFilm.setId(mediaId);
                            newFilm.setTitre(rs.getString("titre"));
                            newFilm.setSynopsis(rs.getString("synopsis"));
                            newFilm.setCasting(rs.getString("casting"));
                            newFilm.setDateSortie(rs.getDate("date_sortie").toLocalDate());
                            newFilm.setUrlImageCover(rs.getString("url_image_cover"));
                            newFilm.setUrlImageBanner(rs.getString("url_image_banner"));
                            newFilm.setUrlTeaser(rs.getString("url_teaser"));
                            newFilm.setRatingMoyen(rs.getDouble("rating_moyen"));
                            newFilm.setUrlVedio(rs.getString("url_video"));
                            newFilm.setDuree(rs.getInt("duree_minutes"));
                            newFilm.setNbreVue(rs.getInt("nbre_vues"));

                            // ✅ FIX 4 : producteur maintenant lu correctement depuis la DB
                            newFilm.setProducteur(rs.getString("producteur"));

                            String ageRatingStr = rs.getString("age_category_name");
                            if (ageRatingStr != null) {
                                try {
                                    newFilm.setAgeRating(AgeRating.valueOf(ageRatingStr));
                                } catch (IllegalArgumentException e) {
                                    newFilm.setAgeRating(AgeRating.ALL);
                                }
                            }
                            newFilm.setGenres(new LinkedHashSet<>());
                            newFilm.setWarnings(new LinkedHashSet<>());
                            return newFilm;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    int catId = rs.getInt("category_id");
                    if (!rs.wasNull()) {
                        boolean catExists = f.getGenres().stream().anyMatch(c -> c.getId() == catId);
                        if (!catExists) {
                            Category cat = new Category();
                            cat.setId(catId);
                            cat.setName(rs.getString("category_nom"));
                            f.getGenres().add(cat);
                        }
                    }

                    int warningId = rs.getInt("warning_id");
                    if (!rs.wasNull()) {
                        boolean warnExists = f.getWarnings().stream().anyMatch(w -> w.getId() == warningId);
                        if (!warnExists) {
                            Warning w = new Warning();
                            w.setId(warningId);
                            w.setNom(rs.getString("warning_desc"));
                            f.getWarnings().add(w);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(filmMap.values());
    }

    public Map<String, List<Film>> findAllGroupedByCategory() throws SQLException {
        List<Film> allFilms = findAll();
        return allFilms.stream()
            .flatMap(film -> film.getGenres().stream()
                .map(category -> new java.util.AbstractMap.SimpleEntry<>(category.getName(), film)))
            .collect(Collectors.groupingBy(
                java.util.Map.Entry::getKey,
                Collectors.mapping(java.util.Map.Entry::getValue, Collectors.toList())
            ));
    }

    public List<Film> findByManyCategories(List<Integer> categoryIds) throws SQLException {
        if (categoryIds == null || categoryIds.isEmpty()) return findAll();

        String placeholders = categoryIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String query = BASE_SELECT +
                       "WHERE c.id IN (" + placeholders + ") " +
                       "ORDER BY m.date_sortie DESC";

        return executeAndGroup(query, ps -> {
            for (int i = 0; i < categoryIds.size(); i++) {
                ps.setInt(i + 1, categoryIds.get(i));
            }
        });
    }

    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }
   
}