module com.example.mychat {
    requires javafx.controls;
    requires javafx.fxml;


    exports com.example.mychat.Client;
    opens com.example.mychat.Client to javafx.fxml;
}