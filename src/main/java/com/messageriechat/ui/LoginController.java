package com.messageriechat.ui;

import com.messageriechat.client.Client;
import com.messageriechat.utils.Protocol;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    // ✅ Le client n'est PAS connecté ici, seulement créé
    private Client client = new Client();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        // ✅ Connexion seulement quand on clique sur le bouton
        if (!client.connect()) {
            messageLabel.setText("❌ Impossible de se connecter au serveur.");
            return;
        }

        client.setOnMessageReceived(response -> {
            Platform.runLater(() -> {
                String[] parts = response.split("\\" + Protocol.SEP, -1);
                if (parts[0].equals(Protocol.SUCCESS)) {
                    openChatScreen(username);
                } else {
                    messageLabel.setText("❌ " + (parts.length > 1 ? parts[1] : "Erreur"));
                    client.disconnect();
                }
            });
        });

        client.login(username, password);
    }

    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
        } catch (Exception e) {
            messageLabel.setText("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openChatScreen(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/chat.fxml"));
            Parent root = loader.load();

            ChatController chatController = loader.getController();
            chatController.init(client, username);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("💬 Messagerie - " + username);
        } catch (Exception e) {
            messageLabel.setText("❌ Erreur ouverture chat : " + e.getMessage());
            e.printStackTrace();
        }
    }
}