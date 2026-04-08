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
import tn.farah.NetflixJava.Entities.Episode;
import tn.farah.NetflixJava.Entities.Saison;
import tn.farah.NetflixJava.Entities.Serie;
import tn.farah.NetflixJava.Entities.Warning;

public class SerieDAO {
	
	
	
	
	
	
	
	

    private final Connection connection;

    public SerieDAO(Connection connection) {
        this.connection = connection;
    }

    // ✅ CORRIGÉ : "m.producteur" supprimé car absent de la DB
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
        "LEFT JOIN category_serie cs ON cs.id = sc.id_category ";

    

    public void create(Serie serie) throws SQLException {
        String queryMedia = "INSERT INTO media (titre, synopsis, casting, date_sortie, url_image_cover, url_image_banner, url_teaser, age_rating_id, producteur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    	String querySerie = "INSERT INTO serie (id, est_complet) VALUES (?, ?)";


        try {
           
            int generatedId = 0;

            try (PreparedStatement psM = connection.prepareStatement(queryMedia, Statement.RETURN_GENERATED_KEYS)) {
                psM.setString(1, serie.getTitre());
                psM.setString(2, serie.getSynopsis());
                psM.setString(3, serie.getCasting());
                psM.setDate(4, Date.valueOf(serie.getDateSortie()));
                psM.setString(5, serie.getUrlImageCover());
                psM.setString(6, serie.getUrlImageBanner());
                psM.setString(7, serie.getUrlTeaser());

              

                psM.setInt(8, serie.getAgeRating() != null ? serie.getAgeRating().getId() : 1);
                psM.setString(9, serie.getProducteur());

                psM.executeUpdate();

                ResultSet rs = psM.getGeneratedKeys();
                if (rs.next()) generatedId = rs.getInt(1);
                serie.setId(generatedId);
            }

            try (PreparedStatement psF = connection.prepareStatement(querySerie)) {
                psF.setInt(1, generatedId);
                psF.setBoolean(2, serie.isTerminee());
                psF.executeUpdate();
            }
          
        } catch (SQLException e) {
           connection.rollback();  
           e.printStackTrace(); 
           // Log the error or throw a custom RuntimeException
           throw new RuntimeException("Database error while saving Serie", e);
       
    }}

    public List<Serie> findAll() {
        String query = BASE_SELECT + "ORDER BY m.id";
        return executeAndGroup(query, ps -> {});
    }

    public void update(Serie serie) {
        // ✅ CORRIGÉ : Retrait de producteur
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
            try { connection.rollback(); } catch (SQLException ex) { throw new RuntimeException("Erreur rollback", ex); }
            throw new RuntimeException("Erreur update série", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { throw new RuntimeException("Erreur autoCommit", e); }
        }
    }

    public void delete(int id) {
        String query = "DELETE FROM `media` WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur delete série id=" + id, e);
        }
    }

    public Serie findById(int id) {
        String query = BASE_SELECT + "WHERE m.id = ?";
        List<Serie> series = executeAndGroup(query, ps -> ps.setInt(1, id));
        return series.isEmpty() ? null : series.get(0);
    }

    public List<Serie> findByYear(int year) {
        String query = BASE_SELECT + "WHERE YEAR(m.date_sortie) = ? ORDER BY m.date_sortie DESC";
        return executeAndGroup(query, ps -> ps.setInt(1, year));
    }

    public List<Serie> findByTitle(String title) {
        String query = BASE_SELECT + "WHERE m.titre LIKE ? ORDER BY m.date_sortie DESC";
        return executeAndGroup(query, ps -> ps.setString(1, "%" + title + "%"));
    }

    public List<Serie> findByManyCategories(List<Integer> categoryIds) {
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

    public Map<String, List<Serie>> findAllGroupedByCategory() {
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

    public Serie findByEpisodeId(int episodeId) {
        String query = BASE_SELECT +
            "JOIN season se ON se.serie_id = m.id " +
            "JOIN episode e ON e.season_id = se.id " +
            "WHERE e.id = ? LIMIT 1";
        List<Serie> series = executeAndGroup(query, ps -> ps.setInt(1, episodeId));
        return series.isEmpty() ? null : series.get(0);
    }

    public int countEpisodesBySaison(int saisonId) {
        String query = "SELECT COUNT(*) FROM episode WHERE season_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, saisonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur countEpisodesBySaison id=" + saisonId, e);
        }
        return 0;
    }

    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<Serie> executeAndGroup(String query, ParamSetter setter) {
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
                            // ✅ CORRIGÉ : Retrait de rs.getString("producteur")
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
        } catch (SQLException e) {
            throw new RuntimeException("Erreur executeAndGroup", e);
        }
        return new ArrayList<>(filmMap.values());
    }

    public List<Episode> findEpisodeBySaison(int saisonId) {
        List<Episode> episodes = new ArrayList<>();
        String query = "SELECT id, season_id, titre, numero, duree_minutes, resume, url_video, url_image, duree_intro_sec " +
                       "FROM episode WHERE season_id = ? ORDER BY numero ASC";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, saisonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Episode ep = new Episode();
                    ep.setId(rs.getInt("id"));
                    ep.setTitre(rs.getString("titre"));
                    ep.setNumeroEpisode(rs.getInt("numero"));
                    ep.setDuree(rs.getInt("duree_minutes"));
                    ep.setResume(rs.getString("resume"));
                    ep.setVideoUrl(rs.getString("url_video"));
                    ep.setMiniatureUrl(rs.getString("url_image"));
                    ep.setDurreeIntro(rs.getInt("duree_intro_sec"));
                    
                    episodes.add(ep);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findEpisodeBySaison pour saison_id=" + saisonId, e);
        }
        
        return episodes;
    }
}