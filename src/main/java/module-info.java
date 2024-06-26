module org.example.promynarzece {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.promynarzece to javafx.fxml;
    exports org.example.promynarzece;
}