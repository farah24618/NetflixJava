package tn.farah.NetflixJava.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class MaListeMobileController implements Initializable {

    @FXML
    private VBox listContainer;
    @FXML
    private StackPane sortMenuOverlay;

    
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ajouter des éléments factices à la liste lors du chargement
        listContainer.getChildren().add(createListItem("The Office (US)", "S1 : E1"));
        listContainer.getChildren().add(createListItem("Spider-Man : New Generation", "Film"));
        listContainer.getChildren().add(createListItem("Working : Passer sa vie à la gagner", "S1 : E1"));
        listContainer.getChildren().add(createListItem("Nouvelle École", "S2 : E1"));
        listContainer.getChildren().add(createListItem("Donnie Brasco", "Film"));
    }

    /**
     * Méthode pour afficher le menu contextuel "Trier par"
     */
    @FXML
    public void openSortMenu() {
        sortMenuOverlay.setVisible(true);
    }

    /**
     * Méthode pour cacher le menu contextuel "Trier par"
     */
    @FXML
    public void closeSortMenu() {
        sortMenuOverlay.setVisible(false);
    }

    /**
     * Crée une ligne (HBox) pour un film/série dans la liste
     */
    private HBox createListItem(String title, String subtitle) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(15);
        row.setCursor(Cursor.HAND);

        // 1. Image (Rectangle fictif, remplacez par ImageView)
        Region thumbnail = new Region();
        thumbnail.setPrefSize(140, 80);
        thumbnail.setMinSize(140, 80);
        thumbnail.setStyle("-fx-background-color: #333333; -fx-background-radius: 4;");

        // 2. Textes (Titre + Sous-titre)
        VBox textContainer = new VBox();
        textContainer.setAlignment(Pos.CENTER_LEFT);
        textContainer.setSpacing(5);
        
        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setTextFill(Color.web("#808080"));
        subtitleLabel.setStyle("-fx-font-size: 12px;");

        textContainer.getChildren().addAll(titleLabel, subtitleLabel);

        // 3. Espace pour pousser le bouton Play à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Bouton Play (Cercle avec icône)
        StackPane playBtn = new StackPane();
        Circle circle = new Circle(16);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(1.5);
        
        Label playIcon = new Label("▷");
        playIcon.setTextFill(Color.WHITE);
        playIcon.setStyle("-fx-font-size: 14px; -fx-translate-x: 1;"); // translate-x pour centrer visuellement le triangle
        
        playBtn.getChildren().addAll(circle, playIcon);

        // Ajout de tous les éléments à la ligne
        row.getChildren().addAll(thumbnail, textContainer, spacer, playBtn);

        // Effet visuel au survol
        row.setOnMouseEntered(e -> thumbnail.setOpacity(0.8));
        row.setOnMouseExited(e -> thumbnail.setOpacity(1.0));

        return row;
    }
}