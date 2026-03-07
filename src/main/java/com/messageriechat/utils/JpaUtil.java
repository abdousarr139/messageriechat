package com.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

    // Une seule instance partagée dans toute l'application
    private static EntityManagerFactory entityManagerFactory;

    // Initialisation au démarrage
    static {
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory("messageriePU");
            System.out.println("✅ EntityManagerFactory initialisé avec succès !");

        } catch (Exception e) {
            System.err.println("❌ Erreur JPA : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Méthode pour obtenir l'EntityManagerFactory
    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    // Méthode pour fermer proprement la connexion
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            System.out.println("🔒 EntityManagerFactory fermé.");
        }
    }
}