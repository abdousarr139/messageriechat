package com.messageriechat.model;



import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class
Message {

    // --- Énumération du statut du message ---
    public enum Statut {
        ENVOYE, RECU, LU
    }

    // --- Attributs ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 1000)
    private String contenu; // max 1000 caractères (RG7)

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.ENVOYE;

    // --- Constructeurs ---
    public Message() {}

    public Message(User sender, User receiver, String contenu) {
        this.sender = sender;
        this.receiver = receiver;
        this.contenu = contenu;
        this.dateEnvoi = LocalDateTime.now();
        this.statut = Statut.ENVOYE;
    }

    // --- Getters et Setters ---
    public Long getId() { return id; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Message{id=" + id + ", de=" + sender.getUsername() +
                ", à=" + receiver.getUsername() + ", statut=" + statut + "}";
    }
}