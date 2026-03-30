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
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Warning;
import tn.farah.NetflixJava.utils.ConxDB;

public class SerieDAO {
    private Connection connection;

    public SerieDAO(Connection connection) {
        this.connection = connection;
    }

    // =========================================================
    // BASE QUERY — reused everywhere
    // =========================================================
    private static final String BASE_SELECT =
        "SELECT m.id AS media_id, m.titre, m.synopsis, m.casting, m.date_sortie, " +
        "m.url_image_cover, m.url_image_banner, m.url_teaser, " +
        "s.est_complet, m.rating_moyen, " +
        "ac.label AS age_category_name, " +
        "c.id AS category_id, c.nom AS category_nom, " +
        "w.id AS warning_id, w.label AS warning_desc, cs.nom AS serie_category " +
        "FROM `media` m " +
        "JOIN serie s ON m.id = s.id " +
        "LEFT JOIN age_rating ac ON m.age_rating_id = ac.id " +
        "LEFT JOIN media_category fc ON m.id = fc.media_id " +
        "LEFT JOIN category c ON fc.category_id = c.id " +
        "LEFT JOIN media_warning mw ON m.id = mw.media_id " +
        "LEFT JOIN content_warning w ON mw.warning_id = w.id " +
        "LEFT JOIN liaison_serie_category sc ON m.id = sc.id_serie " +
        "LEFT JOIN category_serie cs ON cs.id = sc.id_category ";  // trailing space

    // =========================================================
    // CREATE
    // =========================================================
    public void create(Serie Serie) throws SQLException {
        String queryMedia = "INSERT INTO `media` (titre, synopsis, casting, date_sortie, url_image_cover, url_image_banner, url_teaser, age_rating_id, type_media) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String querySerie = "INSERT INTO serie (id, est_complet) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);
            int generatedId = 0;

            try (PreparedStatement psM = connection.prepareStatement(queryMedia, Statement.RETURN_GENERATED_KEYS)) {
                psM.setString(1, Serie.getTitre());
                psM.setString(2, Serie.getSynopsis());
                psM.setString(3, Serie.getCasting());
                psM.setDate(4, Date.valueOf(Serie.getDateSortie()));
                psM.setString(5, Serie.getUrlImageCover());
                psM.setString(6, Serie.getUrlImageBanner());
                psM.setString(7, Serie.getUrlTeaser());
                psM.setInt(8, Serie.getAgeRating().getId());
                psM.setString(9, "SERIE");
                psM.executeUpdate();

                ResultSet rs = psM.getGeneratedKeys();
                if (rs.next()) generatedId = rs.getInt(1);
                Serie.setId(generatedId);
            }

            try (PreparedStatement psF = connection.prepareStatement(querySerie)) {
                psF.setInt(1, generatedId);
                psF.setBoolean(2, Serie.isTerminee());
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

    // =========================================================
    // READ ALL
    // =========================================================
    public List<Serie> findAll() throws SQLException {
        String query = BASE_SELECT + "ORDER BY m.id";
        return executeAndGroup(query, ps -> {});
    }

    // =========================================================
    // UPDATE
    // =========================================================
    public void update(Serie serie) throws SQLException {
        String updateMedia = "UPDATE `media` SET titre=?, synopsis=?, casting=?, date_sortie=?, url_image_cover=?, url_image_banner=?, url_teaser=?, age_rating_id=? WHERE id=?";
        String updateSerie = "UPDATE serie SET est_complet=? WHERE id=?";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement psM = connection.prepareStatement(updateMedia)) {
                psM.setString(1, serie.getTitre());
                psM.setString(2, serie.getSynopsis());
                psM.setString(3, serie.getCasting());
                psM.setDate(4, Date.valueOf(serie.getDateSortie()));
                psM.setString(5, serie.getUrlImageCover());
                psM.setString(6, serie.getUrlImageBanner());
                psM.setString(7, serie.getUrlTeaser());
                psM.setInt(8, serie.getAgeRating().getId());
                psM.setInt(9, serie.getId());
                psM.executeUpdate();
            }
            try (PreparedStatement psF = connection.prepareStatement(updateSerie)) {
                psF.setBoolean(1, serie.isTerminee());
                psF.setInt(2, serie.getId());
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

    // =========================================================
    // DELETE
    // =========================================================
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM `media` WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // =========================================================
    // FIND BY ID
    // =========================================================
    public Serie findById(int id) throws SQLException {
        String query = BASE_SELECT + "WHERE m.id = ?";
        List<Serie> series = executeAndGroup(query, ps -> ps.setInt(1, id));
        return series.isEmpty() ? null : series.get(0);
    }

    // =========================================================
    // FIND BY YEAR
    // =========================================================
    public List<Serie> findByYear(int year) throws SQLException {
        String query = BASE_SELECT + "WHERE YEAR(m.date_sortie) = ? ORDER BY m.date_sortie DESC";
        return executeAndGroup(query, ps -> ps.setInt(1, year));
    }

    // =========================================================
    // FIND BY TITLE
    // =========================================================
    public List<Serie> findByTitle(String title) throws SQLException {
        String query = BASE_SELECT + "WHERE m.titre LIKE ? ORDER BY m.date_sortie DESC";
        return executeAndGroup(query, ps -> ps.setString(1, "%" + title + "%"));
    }

    // =========================================================
    // FIND BY MANY CATEGORIES
    // =========================================================
    public List<Serie> findByManyCategories(List<Integer> categoryIds) throws SQLException {
        if (categoryIds == null || categoryIds.isEmpty()) return findAll();

        String placeholders = categoryIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        String query = BASE_SELECT +
                       "WHERE m.id IN (" +
                       "  SELECT fc2.media_id FROM media_category fc2 WHERE fc2.category_id IN (" + placeholders + ")" +
                       ") ORDER BY m.date_sortie DESC";

        return executeAndGroup(query, ps -> {
            for (int i = 0; i < categoryIds.size(); i++) ps.setInt(i + 1, categoryIds.get(i));
        });
    }

    // =========================================================
    // FIND ALL GROUPED BY CATEGORY
    // =========================================================
    public Map<String, List<Serie>> findAllGroupedByCategory() throws SQLException {
        Map<String, List<Serie>> result = new LinkedHashMap<>();
        String query = BASE_SELECT + "ORDER BY c.nom, m.titre";

        List<Serie> allSeries = executeAndGroup(query, ps -> {});

        for (Serie serie : allSeries) {
            for (Category cat : serie.getGenres()) {
                result.computeIfAbsent(cat.getName(), k -> new ArrayList<>()).add(serie);
            }
        }
        return result;
    }

    // =========================================================
    // CORE HELPER — executes query, groups rows into Serie objects
    // =========================================================
    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<Serie> executeAndGroup(String query, ParamSetter setter) throws SQLException {
        Map<Integer, Serie> filmMap = new LinkedHashMap<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mediaId = rs.getInt("media_id");

                    Serie f = filmMap.computeIfAbsent(mediaId, k -> {
                        try {
                            Serie newFilm = new Serie();
                            newFilm.setId(mediaId);
                            newFilm.setTitre(rs.getString("titre"));
                            newFilm.setSynopsis(rs.getString("synopsis"));
                            newFilm.setCasting(rs.getString("casting"));
                            newFilm.setDateSortie(rs.getDate("date_sortie").toLocalDate());
                            newFilm.setUrlImageCover(rs.getString("url_image_cover"));
                            newFilm.setUrlImageBanner(rs.getString("url_image_banner"));
                            newFilm.setUrlTeaser(rs.getString("url_teaser"));
                            newFilm.setRatingMoyen(rs.getDouble("rating_moyen"));
                            newFilm.setTerminee(rs.getBoolean("est_complet"));

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

                    // Accumulate Category
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

                    // Accumulate Warning
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

    public Serie findByEpisodeId(int episodeId) throws SQLException {
        String query = BASE_SELECT + "JOIN episode e ON e.serie_id = m.id WHERE e.id = ? LIMIT 1";
        List<Serie> series = executeAndGroup(query, ps -> ps.setInt(1, episodeId));
        return series.isEmpty() ? null : series.get(0);
    }
 // Dans SerieDAO — ajouter ces méthodes statiques

    

    
 
    public static String[] getInfosMedia(int serieId) {
        String query = "SELECT m.titre, m.synopsis, YEAR(m.date_sortie) AS annee " +
                       "FROM media m WHERE m.id = ?";
        try (Connection con = ConxDB.getInstance();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, serieId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("titre"),
                    rs.getString("synopsis"),
                    String.valueOf(rs.getInt("annee"))
                };
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new String[]{"", "", ""};
    }

    public static String getGenreMedia(int serieId) {
        String query = "SELECT c.nom FROM category c " +
                       "JOIN media_category mc ON c.id = mc.category_id " +
                       "WHERE mc.media_id = ?";
        // Récupérer TOUS les genres séparés par " / "
        StringBuilder genres = new StringBuilder();
        try (Connection con = ConxDB.getInstance();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, serieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (genres.length() > 0) genres.append(" / ");
                genres.append(rs.getString("nom"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return genres.toString();
    }

    public static String getCastingBySerieId(int serieId) {
        String query = "SELECT m.casting FROM media m WHERE m.id = ?";
        try (Connection con = ConxDB.getInstance();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, serieId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("casting");
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }





}