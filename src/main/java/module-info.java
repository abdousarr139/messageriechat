module com.messageriechat {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Hibernate / JPA
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // BCrypt
    requires jbcrypt;

    // SQL
    requires java.sql;

    // ✅ Ajouter le package racine pour HelloApplication et Launcher
    opens com.messageriechat to javafx.graphics, javafx.fxml;
    exports com.messageriechat to javafx.graphics, javafx.fxml;

    // Existants
    opens com.messageriechat.client to javafx.graphics, javafx.fxml;
    opens com.messageriechat.ui to javafx.fxml, javafx.graphics;
    opens com.messageriechat.model to org.hibernate.orm.core, jakarta.persistence;
    opens com.messageriechat.utils to org.hibernate.orm.core;
    opens com.messageriechat.dao to org.hibernate.orm.core;

    exports com.messageriechat.client;
    exports com.messageriechat.ui;
    exports com.messageriechat.model;
    exports com.messageriechat.utils;
    exports com.messageriechat.dao;
}
