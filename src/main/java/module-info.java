module com.messageriechat.messageriechat {
    requires javafx.controls;
    requires javafx.fxml;

    requires jakarta.persistence;
    requires jbcrypt;

    opens com.messageriechat to javafx.fxml;
    exports com.messageriechat;
}