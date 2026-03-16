package tn.farah.NetflixJava.Service;

import java.util.List;
import tn.farah.NetflixJava.DAO.SubtitleDAO;
import tn.farah.NetflixJava.Entities.Subtitle;

public class SubtitleService {

    // ─────────────────────────────────
    //  AJOUTER UN SOUS-TITRE
    // ─────────────────────────────────
    public static int save(Subtitle subtitle) {

        // Règle métier n°1 : la langue ne doit pas être vide
        if (subtitle.getLangage() == null || subtitle.getLangage().trim().isEmpty()) {
            System.out.println("Erreur : la langue est obligatoire");
            return 0;
        }

        // Règle métier n°2 : l'URL ne doit pas être vide
        if (subtitle.getUrl() == null || subtitle.getUrl().trim().isEmpty()) {
            System.out.println("Erreur : l'URL du fichier est obligatoire");
            return 0;
        }

        // Règle métier n°3 : un sous-titre pour cette langue existe déjà ?
        Subtitle existing = SubtitleDAO.findByMediaAndLangage(
                subtitle.getIdMedia(), subtitle.getLangage());
        if (existing != null) {
            System.out.println("Erreur : sous-titre en "
                    + subtitle.getLangage() + " existe déjà pour ce média");
            return 0;
        }

        return SubtitleDAO.save(subtitle);
    }

    // ─────────────────────────────────
    //  MODIFIER
    // ─────────────────────────────────
    public static void update(Subtitle subtitle) {
        Subtitle existing = SubtitleDAO.findById(subtitle.getId());
        if (existing == null) {
            System.out.println("Erreur : sous-titre introuvable");
            return;
        }
        SubtitleDAO.update(subtitle);
    }

    // ─────────────────────────────────
    //  SUPPRIMER
    // ─────────────────────────────────
    public static void delete(int id) {
        Subtitle existing = SubtitleDAO.findById(id);
        if (existing == null) {
            System.out.println("Erreur : sous-titre introuvable");
            return;
        }
        SubtitleDAO.delete(id);
    }

    // ─────────────────────────────────
    //  FIND ALL
    // ─────────────────────────────────
    public static List<Subtitle> findAll() {
        return SubtitleDAO.findAll();
    }

    // ─────────────────────────────────
    //  FIND BY ID
    // ─────────────────────────────────
    public static Subtitle findById(int id) {
        return SubtitleDAO.findById(id);
    }

    // ─────────────────────────────────
    //  FIND BY MEDIA
    //  langues disponibles pour un media
    // ─────────────────────────────────
    public static List<Subtitle> findByMedia(int idMedia) {
        List<Subtitle> subtitles = SubtitleDAO.findByMedia(idMedia);
        if (subtitles.isEmpty()) {
            System.out.println("Aucun sous-titre disponible pour ce média");
        }
        return subtitles;
    }

    // ──────────────────────────────────────────────
    //  CHARGER LE SOUS-TITRE DANS LE PLAYER
    //  Retourne l'URL du fichier .srt selon la langue
    // ──────────────────────────────────────────────
    public static String getSubtitleUrl(int idMedia, String langage) {
        Subtitle subtitle = SubtitleDAO.findByMediaAndLangage(idMedia, langage);

        // Règle métier : si langue demandée non disponible → chercher FR par défaut
        if (subtitle == null) {
            System.out.println(langage + " non disponible, tentative en FR...");
            subtitle = SubtitleDAO.findByMediaAndLangage(idMedia, "FR");
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
    public static boolean exists(int idMedia, String langage) {
        return SubtitleDAO.findByMediaAndLangage(idMedia, langage) != null;
    }
}
