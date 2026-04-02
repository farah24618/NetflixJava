package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.farah.NetflixJava.DAO.CommentaireDAO;
import tn.farah.NetflixJava.Entities.Commentaire;
import tn.farah.NetflixJava.Entities.Film;
import tn.farah.NetflixJava.utils.DatabaseConnection;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CommentListController implements Initializable {

    // ── FXML ─────────────────────────────
    @FXML private VBox commentsContainer;
    @FXML private Label filmTitleLabel;
    @FXML private Label commentCountLabel;
    @FXML private Label spoilerCountLabel;
    @FXML private Label deletedCountLabel;
    @FXML private TextField searchField;
    @FXML private ToggleButton btnAll;
    @FXML private ToggleButton btnSpoiler;
    @FXML private ToggleButton btnRecent;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button bulkDeleteBtn;
    @FXML private CheckBox selectAllCb;
    @FXML private Label statusLabel;

    // ── DATA ─────────────────────────────
    private Film currentFilm;
    private CommentaireDAO dao;
    private List<Commentaire> allComments = new ArrayList<>();
    private final Set<Integer> selectedIds = new HashSet<>();
    private int deletedCount = 0;
    private String activeFilter = "ALL";

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var conn = DatabaseConnection.getConnection();
        if (conn != null) dao = new CommentaireDAO(conn);

        sortCombo.getItems().addAll("Newest first", "Oldest first");
        sortCombo.getSelectionModel().selectFirst();
        sortCombo.setOnAction(e -> applyFiltersAndSort());
    }

    public void setFilm(Film film) {
        this.currentFilm = film;
        filmTitleLabel.setText(film.getTitre());
        loadComments();
    }

    // ── LOAD DATA ────────────────────────
    private void loadComments() {
        try {
            if (dao != null && currentFilm != null)
                allComments = dao.findByFilmId(currentFilm.getId());
        } catch (Exception e) {
            allComments = new ArrayList<>();
        }

        applyFiltersAndSort();
    }

    // ── FILTER + SORT ────────────────────
    private void applyFiltersAndSort() {

        String q = searchField.getText() == null ? "" :
                searchField.getText().toLowerCase();

        List<Commentaire> filtered = allComments.stream()
                .filter(c -> {
                    if (!q.isEmpty()) {
                        String body = c.getContenu() == null ? "" : c.getContenu().toLowerCase();
                        String user = c.getUsername() == null ? "" : c.getUsername().toLowerCase();
                        if (!body.contains(q) && !user.contains(q)) return false;
                    }

                    return switch (activeFilter) {
                        case "SPOILER" -> c.isSpoiler();
                        case "RECENT" -> c.getDateCommentaire() != null &&
                                c.getDateCommentaire().isAfter(LocalDateTime.now().minusDays(7));
                        default -> true;
                    };
                })
                .sorted(buildComparator())
                .collect(Collectors.toList());

        renderRows(filtered);
        refreshStats();
    }

    private Comparator<Commentaire> buildComparator() {
        Comparator<Commentaire> cmp =
                Comparator.comparing(c ->
                        c.getDateCommentaire() == null ? LocalDateTime.MIN : c.getDateCommentaire());

        return sortCombo.getValue().equals("Newest first") ? cmp.reversed() : cmp;
    }

    // ── RENDER ───────────────────────────
    private void renderRows(List<Commentaire> list) {
        commentsContainer.getChildren().clear();
        selectedIds.clear();

        for (Commentaire c : list) {
            commentsContainer.getChildren().add(buildRow(c));
        }
    }

    private HBox buildRow(Commentaire c) {

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        // 🔥 IMPORTANT FIX
        row.setUserData(c);

        CheckBox cb = new CheckBox();
        cb.setOnAction(e -> {
            if (cb.isSelected()) selectedIds.add(c.getId());
            else selectedIds.remove(c.getId());
            updateBulkBtn();
        });

        Label user = new Label(c.getUsername());
        Label content = new Label(c.getContenu());
        Label date = new Label(
                c.getDateCommentaire() != null ?
                        c.getDateCommentaire().format(FMT) : "-"
        );

        Label spoiler = new Label(c.isSpoiler() ? "⚠ Spoiler" : "OK");

        Button flag = new Button("🚩");
        flag.setOnAction(e -> {
            c.setSpoiler(!c.isSpoiler());
            if (dao != null) dao.save(c);
            applyFiltersAndSort();
            status("Comment by \"" + c.getUsername() + "\" updated");
        });

        Button delete = new Button("🗑");
        delete.setOnAction(e -> {
            if (dao != null) dao.delete(c.getId());
            allComments.remove(c);
            deletedCount++;
            applyFiltersAndSort();
            status("Comment deleted");
        });

        row.getChildren().addAll(cb, user, content, date, spoiler, flag, delete);
        return row;
    }

    // ── SELECT ALL FIXED ─────────────────
    @FXML
    private void handleSelectAll() {

        boolean selected = selectAllCb.isSelected();
        selectedIds.clear();

        commentsContainer.getChildren().forEach(node -> {
            if (node instanceof HBox row) {

                Commentaire c = (Commentaire) row.getUserData();

                row.getChildren().stream()
                        .filter(n -> n instanceof CheckBox)
                        .map(n -> (CheckBox) n)
                        .findFirst()
                        .ifPresent(cb -> {
                            cb.setSelected(selected);
                            if (selected) selectedIds.add(c.getId());
                        });
            }
        });

        updateBulkBtn();
    }

    @FXML
    private void handleDeleteSelected() {
        if (selectedIds.isEmpty()) return;

        selectedIds.forEach(id -> {
            if (dao != null) dao.delete(id);
        });

        allComments.removeIf(c -> selectedIds.contains(c.getId()));
        deletedCount += selectedIds.size();

        applyFiltersAndSort();
        status("Deleted " + selectedIds.size() + " comments");
    }

    private void updateBulkBtn() {
        bulkDeleteBtn.setDisable(selectedIds.isEmpty());
    }

    // ── FILTER HANDLERS ──────────────────
    @FXML private void filterAll()     { activeFilter = "ALL"; applyFiltersAndSort(); }
    @FXML private void filterSpoiler() { activeFilter = "SPOILER"; applyFiltersAndSort(); }
    @FXML private void filterRecent()  { activeFilter = "RECENT"; applyFiltersAndSort(); }
    @FXML private void handleSearch()  { applyFiltersAndSort(); }

    // ── STATS ───────────────────────────
    private void refreshStats() {
        commentCountLabel.setText(allComments.size() + " total");
        spoilerCountLabel.setText(
                allComments.stream().filter(Commentaire::isSpoiler).count() + " spoilers");
        deletedCountLabel.setText(deletedCount + " deleted");
    }

    // ── WINDOW ──────────────────────────
    @FXML
    private void handleClose() {
        ((Stage) commentsContainer.getScene().getWindow()).close();
    }

    // ── STATUS ──────────────────────────
    private void status(String msg) {
        statusLabel.setText(msg);
    }
}