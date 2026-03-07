package com.messageriechat.dao;

import com.messageriechat.model.Message;
import com.messageriechat.model.User;
import com.messageriechat.utils.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class MessageDAO {

    // ✅ Sauvegarder un message
    public void save(Message message) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(message);
            em.getTransaction().commit();
            System.out.println("✅ Message sauvegardé de "
                    + message.getSender().getUsername()
                    + " → " + message.getReceiver().getUsername());
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("❌ Erreur save message : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // 📋 Récupérer la conversation entre deux utilisateurs (RG8 : ordre chronologique)
    public List<Message> findConversation(User user1, User user2) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
                                    "   OR (m.sender = :user2 AND m.receiver = :user1) " +
                                    "ORDER BY m.dateEnvoi ASC", Message.class)
                    .setParameter("user1", user1)
                    .setParameter("user2", user2)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // 📬 Récupérer les messages non livrés pour un utilisateur (RG6)
    public List<Message> findPendingMessages(User receiver) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "WHERE m.receiver = :receiver " +
                                    "AND m.statut = :statut " +
                                    "ORDER BY m.dateEnvoi ASC", Message.class)
                    .setParameter("receiver", receiver)
                    .setParameter("statut", Message.Statut.ENVOYE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // 🔄 Mettre à jour le statut d'un message (ENVOYE → RECU → LU)
    public void updateStatut(Long messageId, Message.Statut statut) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Message message = em.find(Message.class, messageId);
            if (message != null) {
                message.setStatut(statut);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("❌ Erreur updateStatut : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // 📋 Récupérer tous les messages reçus par un utilisateur
    public List<Message> findByReceiver(User receiver) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Message m WHERE m.receiver = :receiver " +
                                    "ORDER BY m.dateEnvoi ASC", Message.class)
                    .setParameter("receiver", receiver)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}