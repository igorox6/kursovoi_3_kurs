module org.example.kursovoi {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    opens org.example.kursovoi.classes to com.fasterxml.jackson.databind;

    opens org.example.kursovoi to com.google.gson, javafx.fxml;

    opens org.example.kursovoi.db_classes to com.google.gson, javafx.fxml;

    opens org.example.kursovoi.controllers to com.google.gson, javafx.fxml;

    exports org.example.kursovoi.classes;
    exports org.example.kursovoi;
    exports org.example.kursovoi.interfases;
    exports org.example.kursovoi.db_classes;
    exports org.example.kursovoi.controllers;
    exports org.example.kursovoi.pojo to com.fasterxml.jackson.databind;
}
