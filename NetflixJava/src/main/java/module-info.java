module tn.farah.NetflixJava {
	requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // Indispensable pour JDBC / ta DB
    
    // Autorise le moteur FXML à accéder à tes contrôleurs par réflexion
    opens tn.farah.NetflixJava.Controller to javafx.fxml;

    // Si tu as besoin d'exporter d'autres packages
    exports tn.farah.NetflixJava;
}
