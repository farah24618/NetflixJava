package tn.farah.NetflixJava.DAO;
 
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
 
import tn.farah.NetflixJava.Entities.AgeRating;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.utils.ConxDB;
 
public class FilmDao {
 
    private final Connection connexion = ConxDB.getInstance();
 
 
    // =========================================================================
    //  SAVE  —  INSERT dans media + INSERT dans film
    // =========================================================================
    public Film save(Film film) {
 
        // CORRECTION 1 : url_image_cover + url_image_banner (2 colonnes image)
        String sqlMedia = "INSERT INTO media " +
                          "(titre, synopsis, casting, date_sortie, url_image_cover, url_image_banner, " +
                          " rating_moyen, url_teaser, age_rating, type) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'FILM')";
 
        String sqlFilm  = "INSERT INTO film (media_id, url_video, duree, nbre_vues) " +
                          "VALUES (?, ?, ?, 0)";
 
        try {
            connexion.setAutoCommit(false);
 
            try (PreparedStatement psMedia = connexion.prepareStatement(
                    sqlMedia, Statement.RETURN_GENERATED_KEYS)) {
 
                psMedia.setString(1, film.getTitre());
                psMedia.setString(2, film.getSynopsis());
                psMedia.setString(3, film.getCasting());
                psMedia.setDate  (4, Date.valueOf(film.getDateSortie()));
                psMedia.setString(5, film.getUrlImageCover());    // ← cover
                psMedia.setString(6, film.getUrlImageBanner());   // ← banner
                psMedia.setDouble(7, film.getRatingMoyen());
                psMedia.setString(8, film.getUrlTeaser());
                psMedia.setString(9, film.getAgeRating().name());
                psMedia.executeUpdate();
 
                ResultSet rsKeys = psMedia.getGeneratedKeys();
                if (rsKeys.next()) {
                    film.setId(rsKeys.getInt(1));
                }
            }
 
            try (PreparedStatement psFilm = connexion.prepareStatement(sqlFilm)) {
                psFilm.setInt   (1, film.getId());
                psFilm.setString(2, film.getUrlVedio());
                psFilm.setInt   (3, film.getDuree());
                psFilm.executeUpdate();
            }
 
            connexion.commit();
 
        } catch (SQLException e) {
            try { connexion.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Erreur lors de l'enregistrement du film", e);
        } finally {
            try { connexion.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
 
        return film;
    }
 
 
    // =========================================================================
    //  FIND BY ID
    // =========================================================================
    public Optional<Film> findbyId(int id) {
 
        // CORRECTION 2 : SELECT avec les 2 colonnes image
        String sql = "SELECT m.*, f.url_video, f.duree, f.nbre_vues " +
                     "FROM media m JOIN film f ON f.media_id = m.id " +
                     "WHERE m.id = ?";
 
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(convertirEnFilm(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du film id=" + id, e);
        }
 
        return Optional.empty();
    }
 
 
    // =========================================================================
    //  FIND ALL
    // =========================================================================
    public List<Film> findall() {
        String sql = "SELECT m.*, f.url_video, f.duree, f.nbre_vues " +
                     "FROM media m JOIN film f ON f.media_id = m.id " +
                     "ORDER BY m.titre";
        List<Film> liste = new ArrayList<>();
 
        try (PreparedStatement ps = connexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                liste.add(convertirEnFilm(rs));
            }
 
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des films", e);
        }
 
        return liste;
    }
 
 
    // =========================================================================
    //  UPDATE  —  UPDATE sur media + UPDATE sur film
    // =========================================================================
    public Film update(Film film) {
 
        // CORRECTION 1 : url_image_cover + url_image_banner dans le UPDATE aussi
        String sqlMedia = "UPDATE media " +
                          "SET titre=?, synopsis=?, casting=?, date_sortie=?, " +
                          "    url_image_cover=?, url_image_banner=?, " +
                          "    rating_moyen=?, url_teaser=?, age_rating=? " +
                          "WHERE id=?";
 
        String sqlFilm  = "UPDATE film SET url_video=?, duree=? WHERE media_id=?";
 
        try {
            connexion.setAutoCommit(false);
 
            try (PreparedStatement psMedia = connexion.prepareStatement(sqlMedia)) {
                psMedia.setString(1, film.getTitre());
                psMedia.setString(2, film.getSynopsis());
                psMedia.setString(3, film.getCasting());
                psMedia.setDate  (4, Date.valueOf(film.getDateSortie()));
                psMedia.setString(5, film.getUrlImageCover());    // ← cover
                psMedia.setString(6, film.getUrlImageBanner());   // ← banner
                psMedia.setDouble(7, film.getRatingMoyen());
                psMedia.setString(8, film.getUrlTeaser());
                psMedia.setString(9, film.getAgeRating().name());
                psMedia.setInt   (10, film.getId());              // WHERE id=?
                psMedia.executeUpdate();
            }
 
            try (PreparedStatement psFilm = connexion.prepareStatement(sqlFilm)) {
                psFilm.setString(1, film.getUrlVedio());
                psFilm.setInt   (2, film.getDuree());
                psFilm.setInt   (3, film.getId());
                psFilm.executeUpdate();
            }
 
            connexion.commit();
 
        } catch (SQLException e) {
            try { connexion.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw new RuntimeException("Erreur lors de la modification du film", e);
        } finally {
            try { connexion.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
 
        return film;
    }
 
 
    // =========================================================================
    //  DELETE
    // =========================================================================
    public void delet(int id) {
        String sql = "DELETE FROM media WHERE id = ?";
 
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            int lignes = ps.executeUpdate();
            if (lignes == 0) {
                throw new RuntimeException("Aucun film trouvé avec l'id=" + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du film id=" + id, e);
        }
    }
 
 
    // =========================================================================
    //  RECHERCHER
    // =========================================================================
    public List<Film> find(String motCle) {
        String sql = "SELECT m.*, f.url_video, f.duree, f.nbre_vues " +
                     "FROM media m JOIN film f ON f.media_id = m.id " +
                     "WHERE m.titre LIKE ? OR m.synopsis LIKE ? OR m.casting LIKE ?";
        String motif = "%" + motCle + "%";
        List<Film> liste = new ArrayList<>();
 
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setString(1, motif);
            ps.setString(2, motif);
            ps.setString(3, motif);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(convertirEnFilm(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de films", e);
        }
 
        return liste;
    }
 
 
    // =========================================================================
    //  INCRÉMENTER LES VUES
    // =========================================================================
    public void incrementerNbreVues(int filmId) {
        String sql = "UPDATE film SET nbre_vues = nbre_vues + 1 WHERE media_id = ?";
 
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, filmId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'incrémentation des vues", e);
        }
    }
 
 
    // =========================================================================
    //  FILMS LES PLUS VISIONNÉS
    // =========================================================================
    public List<Film> trouverLesPlusVisionnes(int limite) {
        String sql = "SELECT m.*, f.url_video, f.duree, f.nbre_vues " +
                     "FROM media m JOIN film f ON f.media_id = m.id " +
                     "ORDER BY f.nbre_vues DESC LIMIT ?";
        List<Film> liste = new ArrayList<>();
 
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, limite);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(convertirEnFilm(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des films populaires", e);
        }
 
        return liste;
    }
 
 
    // =========================================================================
    //  TROUVER PAR CATÉGORIE
    // =========================================================================
    public List<Film> trouverParCategorie(int categorieId) {
 
        // CORRECTION 3 : noms de colonnes SQL corrects (url_video, nbre_vues)
        // pas les noms Java (urlVedio, nbreVue)
        String sql = "SELECT m.*, f.url_video, f.duree, f.nbre_vues " +
                     "FROM media m " +
                     "JOIN film f ON f.media_id = m.id " +
                     "JOIN media_category mc ON mc.media_id = m.id " +
                     "WHERE mc.category_id = ? " +
                     "ORDER BY m.titre";
        List<Film> liste = new ArrayList<>();
 
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, categorieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(convertirEnFilm(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du filtrage par catégorie", e);
        }
 
        return liste;
    }
 
 
    // =========================================================================
    //  TROUVER PAR ANNÉE
    // =========================================================================
    public List<Film> trouverParAnnee(int annee) {
 
        // CORRECTION 3 : noms de colonnes SQL corrects
        String sql = "SELECT m.*, f.url_video, f.duree, f.nbre_vues " +
                     "FROM media m " +
                     "JOIN film f ON f.media_id = m.id " +
                     "WHERE YEAR(m.date_sortie) = ? " +
                     "ORDER BY m.date_sortie";
        List<Film> liste = new ArrayList<>();
 
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            ps.setInt(1, annee);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(convertirEnFilm(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du filtrage par année", e);
        }
 
        return liste;
    }
 
 
    // =========================================================================
    //  CONVERSION ResultSet → Film
    // =========================================================================
    private Film convertirEnFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
 
        // Champs hérités de Media
        film.setId             (rs.getInt   ("id"));
        film.setTitre          (rs.getString("titre"));
        film.setSynopsis       (rs.getString("synopsis"));
        film.setCasting        (rs.getString("casting"));
        film.setDateSortie     (rs.getDate  ("date_sortie").toLocalDate());
        film.setUrlImageCover  (rs.getString("url_image_cover"));   
        film.setUrlImageBanner (rs.getString("url_image_banner"));  
        film.setRatingMoyen    (rs.getDouble("rating_moyen"));
        film.setUrlTeaser      (rs.getString("url_teaser"));
        film.setAgeRating      (AgeRating.valueOf(rs.getString("age_rating")));
 
        // Champs propres à Film
        film.setUrlVedio       (rs.getString("url_video"));   
        film.setDuree          (rs.getInt   ("duree"));
        film.setNbreVue        (rs.getInt   ("nbre_vues"));  
        return film;
    }
}