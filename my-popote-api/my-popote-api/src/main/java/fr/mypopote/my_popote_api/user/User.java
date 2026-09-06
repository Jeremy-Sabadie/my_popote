package fr.mypopote.my_popote_api.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Représente un utilisateur de l'application My Popote.
 *
 * Chaque utilisateur possède ses propres recettes et planifications.
 * Les relations avec ces entités seront ajoutées progressivement.
 */
@Entity
@Table(name = "app_user")
public class User {

    /**
     * Identifiant technique généré automatiquement par MariaDB.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Adresse email utilisée notamment pour l'authentification.
     * Elle doit être unique pour éviter plusieurs comptes avec le même email.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Le mot de passe n'est jamais stocké en clair.
     * Ce champ contiendra uniquement son hash généré lors de l'inscription.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Prénom affiché dans l'interface utilisateur.
     * Ce champ reste facultatif.
     */
    @Column(name = "first_name", length = 100)
    private String firstName;

    /**
     * Date de création du compte.
     * La valeur est directement générée par MariaDB.
     */
    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification du compte.
     * MariaDB met automatiquement cette valeur à jour.
     */
    @Column(
        name = "updated_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime updatedAt;

    /**
     * Constructeur sans argument requis par JPA.
     */
    protected User() {
    }

    /**
     * Constructeur utilisé pour créer un nouvel utilisateur.
     * L'identifiant et les dates sont générés automatiquement.
     */
    public User(String email, String passwordHash, String firstName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}