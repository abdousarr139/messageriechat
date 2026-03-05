module com.messageriechat.messageriechat {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens com.messageriechat to javafx.fxml;
    exports com.messageriechat;
}