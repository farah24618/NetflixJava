package tn.farah.NetflixJava.Service;

import java.util.List;
import tn.farah.NetflixJava.DAO.SubtitleDAO;
import tn.farah.NetflixJava.Entities.Subtitle;

public class SubtitleService {

    private final SubtitleDAO subtitleDAO;

    // Injection du DAO via le constructeur
    public SubtitleService(SubtitleDAO subtitleDAO) {
        this.subtitleDAO = subtitleDAO;
    }

    // ─────────────────────────────────
    //  AJOUTER UN SOUS-TITRE
    // ─────────────────────────────────
    public int save(Subtitle subtitle) {

        // Règle métier n°1 : la langue ne doit pas être vide
        if (subtitle.getLangage() == null || subtitle.getLangage().trim().isEmpty()) {
            System.err.println("Erreur : la langue est obligatoire");
            return 0;
        }

        // Règle métier n°2 : l'URL ne doit pas être vide
        if (subtitle.getUrl() == null || subtitle.getUrl().trim().isEmpty()) {
            System.err.println("Erreur : l'URL du fichier est obligatoire");
            return 0;
        }

        // Règle métier n°3 : un sous-titre pour cette langue existe déjà ?
        Subtitle existing = subtitleDAO.findByMediaAndLangage(
                subtitle.getIdMedia(), subtitle.getLangage());
        
        if (existing != null) {
            System.err.println("Erreur : sous-titre en "
                    + subtitle.getLangage() + " existe déjà pour ce média");
            return 0;
        }

        return subtitleDAO.save(subtitle);
    }

    // ─────────────────────────────────
    //  MODIFIER
    // ─────────────────────────────────
    public void update(Subtitle subtitle) {
        Subtitle existing = subtitleDAO.findById(subtitle.getId());
        if (existing == null) {
            System.err.println("Erreur : sous-titre introuvable");
            return;
        }
        subtitleDAO.update(subtitle);
    }

    // ─────────────────────────────────
    //  SUPPRIMER
    // ─────────────────────────────────
    public void delete(int id) {
        Subtitle existing = subtitleDAO.findById(id);
        if (existing == null) {
            System.err.println("Erreur : sous-titre introuvable");
            return;
        }
        subtitleDAO.delete(id);
    }

    // ─────────────────────────────────
    //  FIND ALL
    // ─────────────────────────────────
    public List<Subtitle> findAll() {
        return subtitleDAO.findAll();
    }

    // ─────────────────────────────────
    //  FIND BY ID
    // ─────────────────────────────────
    public Subtitle findById(int id) {
        return subtitleDAO.findById(id);
    }

    // ─────────────────────────────────
    //  FIND BY MEDIA
    // ─────────────────────────────────
    public List<Subtitle> findByMedia(int idMedia) {
        List<Subtitle> subtitles = subtitleDAO.findByMedia(idMedia);
        if (subtitles.isEmpty()) {
            System.out.println("Aucun sous-titre disponible pour ce média");
        }
        return subtitles;
    }

    // ──────────────────────────────────────────────
    //  CHARGER LE SOUS-TITRE DANS LE PLAYER
    // ──────────────────────────────────────────────
    public String getSubtitleUrl(int idMedia, String langage) {
        Subtitle subtitle = subtitleDAO.findByMediaAndLangage(idMedia, langage);

        // Règle métier : si langue demandée non disponible → chercher FR par défaut
        if (subtitle == null) {
            System.out.println(langage + " non disponible, tentative en FR...");
            subtitle = subtitleDAO.findByMediaAndLangage(idMedia, "FR");
        }

        // Aucun sous-titre disponible
        if (subtitle == null) {
            System.out.println("Aucun sous-titre disponible");
            return null;
        }

        return subtitle.getUrl();
    }

    // ─────────────────────────────────
    //  VÉRIFIER SI SOUS-TITRE EXISTE
    // ─────────────────────────────────
    public boolean exists(int idMedia, String langage) {
        return subtitleDAO.findByMediaAndLangage(idMedia, langage) != null;
    }
}