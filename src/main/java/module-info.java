module com.example.mychat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mychat to javafx.fxml;
    exports com.example.mychat;
}