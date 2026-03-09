package com.messageriechat.server;

import com.messageriechat.utils.JpaUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 5000;
    private static Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        // ✅ Initialiser JPA dès le démarrage du serveur
        try {
            JpaUtil.getEntityManagerFactory();
            System.out.println("✅ Base de données connectée !");
        } catch (Exception e) {
            System.err.println("❌ Impossible de se connecter à la base : " + e.getMessage());
            System.err.println("⛔ Vérifiez persistence.xml et PostgreSQL");
            return; // Arrêter le serveur si la BDD n'est pas accessible
        }

        System.out.println("🚀 Serveur démarré sur le port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Nouveau client connecté : "
                        + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, activeClients);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur serveur : " + e.getMessage());
        } finally {
            JpaUtil.shutdown();
        }
    }
}