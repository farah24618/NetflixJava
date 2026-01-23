module tn.farah.NetflixJava {
    requires javafx.controls;
    requires javafx.fxml;

    opens tn.farah.NetflixJava to javafx.fxml;
    exports tn.farah.NetflixJava;
}
