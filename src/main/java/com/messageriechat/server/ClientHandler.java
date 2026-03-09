package com.messageriechat.server;

import com.messageriechat.dao.MessageDAO;
import com.messageriechat.dao.UserDAO;
import com.messageriechat.model.Message;
import com.messageriechat.model.User;
import com.messageriechat.utils.PasswordUtil;
import com.messageriechat.utils.Protocol;
import java.util.List;


import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private User currentUser;
    private Map<String, ClientHandler> activeClients;

    private UserDAO userDAO = new UserDAO();
    private MessageDAO messageDAO = new MessageDAO();

    public ClientHandler(Socket socket, Map<String, ClientHandler> activeClients) {
        this.socket = socket;
        this.activeClients = activeClients;
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("❌ Erreur initialisation ClientHandler : " + e.getMessage());
        }
    }

    @Override
    public void run() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                handleMessage(line);
            }
        } catch (IOException e) {
            System.err.println("⚠️ Connexion perdue : " +
                    (currentUser != null ? currentUser.getUsername() : "inconnu"));
        } finally {
            disconnect();
        }
    }

    // 🔀 Aiguillage des commandes reçues
    private void handleMessage(String line) {
        String[] parts = line.split("\\" + Protocol.SEP, -1);
        String command = parts[0];

        System.out.println("📨 Commande reçue : " + line); // RG12

        switch (command) {
            case Protocol.REGISTER    -> handleRegister(parts);
            case Protocol.LOGIN       -> handleLogin(parts);
            case Protocol.LOGOUT      -> handleLogout();
            case Protocol.SEND_MSG    -> handleSendMessage(parts);
            case Protocol.GET_USERS   -> handleGetUsers();
            case Protocol.GET_HISTORY -> handleGetHistory(parts);
            default -> send(Protocol.ERROR + Protocol.SEP + "Commande inconnue");
        }
    }

    // 📝 Inscription (RG1, RG9)
    private void handleRegister(String[] parts) {
        if (parts.length < 3) {
            send(Protocol.ERROR + Protocol.SEP + "Paramètres manquants");
            return;
        }
        String username = parts[1];
        String password = parts[2];

        if (userDAO.usernameExists(username)) {
            send(Protocol.ERROR + Protocol.SEP + "Ce username est déjà pris");
            return;
        }

        User user = new User(username, PasswordUtil.hash(password));
        userDAO.save(user);
        send(Protocol.SUCCESS + Protocol.SEP + "Inscription réussie");
        System.out.println("✅ Nouvel utilisateur inscrit : " + username); // RG12
    }

    // 🔑 Connexion (RG2, RG3, RG4)
    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            send(Protocol.ERROR + Protocol.SEP + "Paramètres manquants");
            return;
        }
        String username = parts[1];
        String password = parts[2];

        User user = userDAO.findByUsername(username);

        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            send(Protocol.ERROR + Protocol.SEP + "Username ou mot de passe incorrect");
            return;
        }

        // RG3 : un seul utilisateur connecté à la fois
        if (activeClients.containsKey(username)) {
            send(Protocol.ERROR + Protocol.SEP + "Cet utilisateur est déjà connecté");
            return;
        }

        // RG4 : statut ONLINE
        currentUser = user;
        userDAO.updateStatus(username, User.Status.ONLINE);
        activeClients.put(username, this);

        send(Protocol.SUCCESS + Protocol.SEP + "Connexion réussie");
        System.out.println("🟢 Connexion : " + username); // RG12

        // RG6 : livrer les messages en attente
        deliverPendingMessages();
    }

    // 🚪 Déconnexion (RG4)
    private void handleLogout() {
        disconnect();
        send(Protocol.SUCCESS + Protocol.SEP + "Déconnexion réussie");
    }

    // 💬 Envoi de message (RG5, RG6, RG7)
    private void handleSendMessage(String[] parts) {
        if (currentUser == null) {
            send(Protocol.ERROR + Protocol.SEP + "Vous devez être connecté");
            return;
        }
        if (parts.length < 4) {
            send(Protocol.ERROR + Protocol.SEP + "Paramètres manquants");
            return;
        }

        String receiverUsername = parts[2];
        String contenu = parts[3];

        // RG7 : contenu non vide et max 1000 caractères
        if (contenu == null || contenu.trim().isEmpty()) {
            send(Protocol.ERROR + Protocol.SEP + "Le message ne peut pas être vide");
            return;
        }
        if (contenu.length() > 1000) {
            send(Protocol.ERROR + Protocol.SEP + "Message trop long (max 1000 caractères)");
            return;
        }

        // RG5 : le destinataire doit exister
        User receiver = userDAO.findByUsername(receiverUsername);
        if (receiver == null) {
            send(Protocol.ERROR + Protocol.SEP + "Destinataire introuvable");
            return;
        }

        // Sauvegarder le message
        Message message = new Message(currentUser, receiver, contenu);
        messageDAO.save(message);

        System.out.println("💬 Message : " + currentUser.getUsername()
                + " → " + receiverUsername); // RG12

        // RG6 : si le destinataire est connecté, livrer en temps réel
        ClientHandler receiverHandler = activeClients.get(receiverUsername);
        if (receiverHandler != null) {
            receiverHandler.send(Protocol.NEW_MSG + Protocol.SEP
                    + currentUser.getUsername() + Protocol.SEP
                    + contenu + Protocol.SEP
                    + message.getDateEnvoi());
            messageDAO.updateStatut(message.getId(), Message.Statut.RECU);
        }

        send(Protocol.SUCCESS + Protocol.SEP + "Message envoyé");
    }

    // 👥 Liste des utilisateurs connectés
    private void handleGetUsers() {
        if (currentUser == null) {
            send(Protocol.ERROR + Protocol.SEP + "Vous devez être connecté");
            return;
        }

        // ✅ Récupérer TOUS les utilisateurs de la base
        List<User> allUsers = userDAO.findAll();

        StringBuilder sb = new StringBuilder(Protocol.USER_LIST);
        for (User user : allUsers) {
            if (!user.getUsername().equals(currentUser.getUsername())) {
                // ✅ Format : username:ONLINE ou username:OFFLINE
                sb.append(Protocol.SEP)
                        .append(user.getUsername())
                        .append(":")
                        .append(user.getStatus().name());
            }
        }
        send(sb.toString());
        System.out.println("👥 Liste envoyée : " + sb);
    }

    // 📜 Historique de conversation (RG8)
    private void handleGetHistory(String[] parts) {
        if (currentUser == null) {
            send(Protocol.ERROR + Protocol.SEP + "Vous devez être connecté");
            return;
        }
        if (parts.length < 3) {
            send(Protocol.ERROR + Protocol.SEP + "Paramètres manquants");
            return;
        }

        User other = userDAO.findByUsername(parts[2]);
        if (other == null) {
            send(Protocol.ERROR + Protocol.SEP + "Utilisateur introuvable");
            return;
        }

        List<Message> messages = messageDAO.findConversation(currentUser, other);
        for (Message m : messages) {
            send(Protocol.HISTORY_MSG + Protocol.SEP
                    + m.getSender().getUsername() + Protocol.SEP
                    + m.getReceiver().getUsername() + Protocol.SEP
                    + m.getContenu() + Protocol.SEP
                    + m.getDateEnvoi() + Protocol.SEP
                    + m.getStatut());
        }
        send(Protocol.SUCCESS + Protocol.SEP + "Fin de l'historique");
    }

    // 📬 Livrer les messages en attente à la connexion (RG6)
    private void deliverPendingMessages() {
        List<Message> pending = messageDAO.findPendingMessages(currentUser);
        for (Message m : pending) {
            send(Protocol.NEW_MSG + Protocol.SEP
                    + m.getSender().getUsername() + Protocol.SEP
                    + m.getContenu() + Protocol.SEP
                    + m.getDateEnvoi());
            messageDAO.updateStatut(m.getId(), Message.Statut.RECU);
        }
        if (!pending.isEmpty()) {
            System.out.println("📬 " + pending.size()
                    + " message(s) en attente livré(s) à " + currentUser.getUsername());
        }
    }

    // 🔌 Déconnexion propre (RG4, RG10)
    private void disconnect() {
        if (currentUser != null) {
            activeClients.remove(currentUser.getUsername());
            userDAO.updateStatus(currentUser.getUsername(), User.Status.OFFLINE);
            System.out.println("🔴 Déconnexion : " + currentUser.getUsername()); // RG12
            currentUser = null;
        }
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("❌ Erreur fermeture socket : " + e.getMessage());
        }
    }

    // 📤 Envoyer une réponse au client
    public void send(String message) {
        out.println(message);
    }
}