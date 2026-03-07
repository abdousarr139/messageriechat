package com.ui;

import com.messagerie.client.Client;
import com.messagerie.utils.Protocol;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // ✅ Client créé mais pas connecté
    private Client client = new Client();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm  = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setText("❌ Les mots de passe ne correspondent pas.");
            return;
        }
        if (password.length() < 4) {
            messageLabel.setText("❌ Mot de passe trop court (min 4 caractères).");
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
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("✅ Inscription réussie ! Connectez-vous.");
                } else {
                    messageLabel.setText("❌ " + (parts.length > 1 ? parts[1] : "Erreur"));
                }
                client.disconnect();
            });
        });

        client.register(username, password);
    }

    @FXML
    private void handleGoToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400));
        } catch (Exception e) {
            messageLabel.setText("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}