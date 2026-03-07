package com.ui;

import com.messagerie.client.Client;
import com.messagerie.utils.Protocol;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChatController {

    @FXML private Label currentUserLabel;
    @FXML private Label chatWithLabel;
    @FXML private ListView<String> userListView;
    @FXML private VBox messageBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageField;

    private Client client;
    private String currentUsername;
    private String selectedUser;

    public void init(Client client, String username) {
        this.client = client;
        this.currentUsername = username;

        currentUserLabel.setText("👤 " + username);

        // ✅ Afficher statut avec couleur
        userListView.setCellFactory(list -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String[] p      = item.split(":");
                    String uname    = p[0];
                    String status   = p.length > 1 ? p[1] : "OFFLINE";
                    if (status.equals("ONLINE")) {
                        setText("🟢 " + uname);
                        setStyle("-fx-text-fill: #128C7E; -fx-font-weight: bold;");
                    } else {
                        setText("⚫ " + uname);
                        setStyle("-fx-text-fill: #888888;");
                    }
                }
            }
        });

        // ✅ Clic sur un utilisateur
        userListView.setOnMouseClicked(event -> {
            String selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String uname = selected.split(":")[0];
                if (!uname.equals(currentUsername)) {
                    selectedUser = uname;
                    chatWithLabel.setText("💬 " + selectedUser);
                    messageBox.getChildren().clear();
                    client.getHistory(currentUsername, selectedUser);
                }
            }
        });

        // ✅ Callback puis liste des utilisateurs
        client.setOnMessageReceived(this::handleServerMessage);
        client.getUsers();

        startUserListRefresh();
    }

    private void handleServerMessage(String response) {
        Platform.runLater(() -> {
            String[] parts = response.split("\\" + Protocol.SEP, -1);
            switch (parts[0]) {
                case Protocol.USER_LIST   -> updateUserList(parts);
                case Protocol.NEW_MSG     -> displayNewMessage(parts);
                case Protocol.HISTORY_MSG -> displayHistoryMessage(parts);
                case Protocol.ERROR       -> showError(parts.length > 1 ? parts[1] : "Erreur");
                default -> {}
            }
        });
    }

    // ✅ Mettre à jour la liste avec format "username:STATUS"
    private void updateUserList(String[] parts) {
        userListView.getItems().clear();
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                userListView.getItems().add(parts[i]);
            }
        }
    }

    private void displayNewMessage(String[] parts) {
        if (parts.length < 4) return;
        String sender  = parts[1];
        String contenu = parts[2];
        String date    = parts[3];
        if (sender.equals(selectedUser) || sender.equals(currentUsername)) {
            boolean isMe = sender.equals(currentUsername);
            addMessageBubble(sender, contenu, date, isMe);
        }
    }

    private void displayHistoryMessage(String[] parts) {
        if (parts.length < 6) return;
        String sender  = parts[1];
        String contenu = parts[3];
        String date    = parts[4];
        boolean isMe   = sender.equals(currentUsername);
        addMessageBubble(sender, contenu, date, isMe);
    }

    private void addMessageBubble(String sender, String contenu, String date, boolean isMe) {
        VBox bubble = new VBox(3);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setMaxWidth(400);

        Text messageText = new Text(contenu);
        messageText.setWrappingWidth(350);
        messageText.setFill(isMe ? Color.WHITE : Color.BLACK);

        Label dateLabel = new Label(date.length() > 16 ? date.substring(11, 16) : date);
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: "
                + (isMe ? "#ddd" : "#888") + ";");

        bubble.getChildren().addAll(messageText, dateLabel);

        if (isMe) {
            bubble.setStyle("-fx-background-color: #128C7E; "
                    + "-fx-background-radius: 15 15 0 15;");
        } else {
            bubble.setStyle("-fx-background-color: #ffffff; "
                    + "-fx-background-radius: 15 15 15 0;");
        }

        HBox container = new HBox(bubble);
        container.setPadding(new Insets(2, 10, 2, 10));
        container.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        messageBox.getChildren().add(container);
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

    @FXML
    private void handleSendMessage() {
        String contenu = messageField.getText().trim();
        if (selectedUser == null) {
            showError("Sélectionnez un utilisateur d'abord.");
            return;
        }
        if (contenu.isEmpty()) return;
        if (contenu.length() > 1000) {
            showError("Message trop long (max 1000 caractères).");
            return;
        }
        addMessageBubble(currentUsername, contenu,
                java.time.LocalDateTime.now().toString(), true);
        client.sendMessage(currentUsername, selectedUser, contenu);
        messageField.clear();
    }

    @FXML
    private void handleLogout() {
        client.logout(currentUsername);
        client.disconnect();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messageField.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400));
            stage.setTitle("💬 Messagerie");
        } catch (Exception e) {
            showError("Erreur retour à la connexion.");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void startUserListRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (client.isConnected()) {
                try {
                    Thread.sleep(5000);
                    client.getUsers();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
}