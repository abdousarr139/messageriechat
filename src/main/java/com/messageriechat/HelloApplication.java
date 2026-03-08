package com.messageriechat;

import javafx.application.Application;
import com.messageriechat.client.ClientApp;

public class HelloApplication extends Application {
    @Override
    public void start(javafx.stage.Stage stage) throws Exception {
        // Déléguer à ClientApp
        new ClientApp().start(stage);
    }
}
