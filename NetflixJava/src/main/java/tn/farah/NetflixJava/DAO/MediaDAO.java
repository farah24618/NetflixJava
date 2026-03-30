package tn.farah.NetflixJava.DAO;

public class MediaDAO {

   /* // ============================================================
    // 1. SAUVEGARDE (INSERT)
    // Gère automatiquement le 'type_media' selon l'objet passé
    // ============================================================
    public int save(Media media) {
        String sql = "INSERT INTO media (titre, synopsis, casting, date_sortie, url_image_cover, rating_moyen, type_media) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, media.getTitre());
            pstmt.setString(2, media.getSynopsis());
            pstmt.setString(3, media.getCasting());
            pstmt.setDate(4, new java.sql.Date(media.getDateSortie().getTime()));
            pstmt.setString(5, media.getUrlImageCover());
            pstmt.setDouble(6, media.getRatingMoyen());

            // --- C'EST ICI QUE LA MAGIE OPÈRE ---
            if (media instanceof Film) {
                pstmt.setString(7, "FILM");
            } else if (media instanceof Serie) {
                pstmt.setString(7, "SERIE");
            } else {
                pstmt.setString(7, "UNKNOWN");
            }

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    media.setId(generatedId);
                }
            }

        } catch (SQLException ex) {
            System.err.println("Erreur save Media : " + ex.getMessage());
        }
        return generatedId;
    }

    // ============================================================
    // 2. LECTURE GLOBALE (FIND ALL)
    // Instancie Film ou Serie selon la colonne type_media
    // ============================================================
    public List<Media> findAll() {
        List<Media> catalogue = new ArrayList<>();
        String sql = "SELECT * FROM media ORDER BY date_ajout DESC";

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Media m = mapRowToMedia(rs); // Appel de la méthode Helper
                if (m != null) {
                    catalogue.add(m);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return catalogue;
    }

    // ============================================================
    // 3. RECHERCHE PAR TITRE
    // ============================================================
    public List<Media> findByTitre(String motCle) {
        List<Media> resultats = new ArrayList<>();
        String sql = "SELECT * FROM media WHERE titre LIKE ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + motCle + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Media m = mapRowToMedia(rs);
                    if (m != null) resultats.add(m);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultats;
    }

    // ============================================================
    // 4. RECHERCHE PAR CATEGORIE (JOIN)
    // ============================================================
    public List<Media> findByCategory(String nomCategorie) {
        List<Media> resultats = new ArrayList<>();
        String sql = "SELECT m.* FROM media m " +
                     "JOIN media_category mc ON m.id = mc.media_id " +
                     "JOIN category c ON mc.category_id = c.id " +
                     "WHERE c.nom = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nomCategorie);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Media m = mapRowToMedia(rs);
                    if (m != null) resultats.add(m);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultats;
    }

    // ============================================================
    // 5. FIND BY ID
    // ============================================================
    public Media findById(int id) {
        Media media = null;
        String sql = "SELECT * FROM media WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    media = mapRowToMedia(rs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return media;
    }

    // ============================================================
    // 🛠 MÉTHODE PRIVÉE (HELPER)
    // C'est elle qui gère le problème "abstract class"
    // ============================================================
    private Media mapRowToMedia(ResultSet rs) throws SQLException {
        String type = rs.getString("type_media");
        Media m = null;

        // 1. Choix de l'instance
        if ("FILM".equalsIgnoreCase(type)) {
            m = new Film();
        } else if ("SERIE".equalsIgnoreCase(type)) {
            m = new Serie();
        } else {
            // Si le type est NULL ou inconnu, on ne peut pas instancier Media (car abstract)
            // On retourne null, et la boucle while ignorera cette ligne.
            System.err.println("Type inconnu pour ID " + rs.getInt("id"));
            return null;
        }

        // 2. Remplissage des données communes
        m.setId(rs.getInt("id"));
        m.setTitre(rs.getString("titre"));
        m.setSynopsis(rs.getString("synopsis"));
        m.setCasting(rs.getString("casting"));
        m.setDateSortie(rs.getDate("date_sortie"));
        m.setUrlImageCover(rs.getString("url_image_cover"));
        m.setRatingMoyen(rs.getDouble("rating_moyen"));

        // Optionnel : Gestion de la date d'ajout
        Timestamp ts = rs.getTimestamp("date_ajout");
        if(ts != null) m.setDateAjout(ts.toLocalDateTime());

        return m;
    }*/
	
}