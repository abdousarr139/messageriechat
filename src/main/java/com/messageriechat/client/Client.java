package com.messageriechat.client;

import com.messageriechat.utils.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived; // callback pour l'UI

    // 🔌 Connexion au serveur
    public boolean connect() {
        try {
            socket = new Socket(HOST, PORT);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Thread d'écoute des messages entrants
            new Thread(this::listenFromServer).start();

            System.out.println("✅ Connecté au serveur");
            return true;
        } catch (IOException e) {
            System.err.println("❌ Impossible de se connecter au serveur : " + e.getMessage());
            return false;
        }
    }

    // 👂 Écoute en continu les messages du serveur (RG10)
    private void listenFromServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (onMessageReceived != null) {
                    onMessageReceived.accept(line);
                }
            }
        } catch (IOException e) {
            // RG10 : perte de connexion
            if (onMessageReceived != null) {
                onMessageReceived.accept(Protocol.ERROR + Protocol.SEP
                        + "Connexion au serveur perdue");
            }
            System.err.println("⚠️ Connexion au serveur perdue");
        }
    }

    // 📤 Envoyer une commande au serveur
    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // 📝 Inscription
    public void register(String username, String password) {
        send(Protocol.REGISTER + Protocol.SEP + username + Protocol.SEP + password);
    }

    // 🔑 Connexion
    public void login(String username, String password) {
        send(Protocol.LOGIN + Protocol.SEP + username + Protocol.SEP + password);
    }

    // 🚪 Déconnexion
    public void logout(String username) {
        send(Protocol.LOGOUT + Protocol.SEP + username);
    }

    // 💬 Envoyer un message
    public void sendMessage(String sender, String receiver, String contenu) {
        send(Protocol.SEND_MSG + Protocol.SEP + sender
                + Protocol.SEP + receiver
                + Protocol.SEP + contenu);
    }

    // 👥 Demander la liste des utilisateurs connectés
    public void getUsers() {
        send(Protocol.GET_USERS);
    }

    // 📜 Demander l'historique d'une conversation
    public void getHistory(String myUsername, String otherUsername) {
        send(Protocol.GET_HISTORY + Protocol.SEP + myUsername
                + Protocol.SEP + otherUsername);
    }

    // 🎯 Définir le callback pour recevoir les messages
    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    // 🔒 Fermer la connexion
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur déconnexion : " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}