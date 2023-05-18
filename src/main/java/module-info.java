module com.example.juhju2_projekt_bat {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens Graphics to javafx.fxml;
    exports Graphics;

    opens Server to com.fasterxml.jackson.databind;
    exports Server;
    exports Client;
}