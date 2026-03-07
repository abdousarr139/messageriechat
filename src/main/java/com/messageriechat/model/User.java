package com.messageriechat.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User{


    // --- Énumération du statut ---
    public enum Status {
        ONLINE, OFFLINE
    }
    // --- Attributs ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // sera hashé (RG9)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OFFLINE;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    public User() {}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.status = Status.OFFLINE;
        this.dateCreation = LocalDateTime.now();
    }
    // --- Getters et Setters ---
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", status=" + status +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
