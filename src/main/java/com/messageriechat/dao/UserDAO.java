package com.messageriechat.dao;



import com.messageriechat.model.User;
import com.messageriechat.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;

public class UserDAO {

    // ✅ Sauvegarder un nouvel utilisateur (Inscription)
    public void save(User user) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            System.out.println("✅ Utilisateur sauvegardé : " + user.getUsername());
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("❌ Erreur save user : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // 🔍 Chercher un utilisateur par son username (RG1, RG2)
    public User findByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // utilisateur non trouvé
        } finally {
            em.close();
        }
    }

    // 🔍 Chercher un utilisateur par son id
    public User findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    // 📋 Récupérer tous les utilisateurs
    // 📋 Récupérer tous les utilisateurs
    public List<User> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            // ✅ Requête native pour bypasser le cache Hibernate
            List<User> users = em.createNativeQuery(
                            "SELECT * FROM users ORDER BY username ASC", User.class)
                    .getResultList();
            em.getTransaction().commit();
            return users;
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("❌ Erreur findAll : " + e.getMessage());
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }

    // 📋 Récupérer uniquement les utilisateurs en ligne
    public List<User> findOnlineUsers() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.status = :status", User.class)
                    .setParameter("status", User.Status.ONLINE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // 🔄 Mettre à jour le statut (ONLINE / OFFLINE) (RG4)
    public void updateStatus(String username, User.Status status) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            user.setStatus(status);
            em.getTransaction().commit();
            System.out.println("🔄 Statut mis à jour : " + username + " → " + status);
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("❌ Erreur updateStatus : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // ✏️ Mettre à jour un utilisateur (mot de passe, etc.)
    public void update(User user) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.err.println("❌ Erreur update user : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // 🗑️ Vérifier si un username existe déjà (RG1)
    public boolean usernameExists(String username) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}