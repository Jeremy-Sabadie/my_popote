package fr.mypopote.my_popote_api.auth.passwordreset;

import fr.mypopote.my_popote_api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Token temporaire utilisé lors d'une demande
 * de réinitialisation du mot de passe.
 *
 * Le token envoyé à l'utilisateur n'est jamais enregistré en clair.
 * Seul son hash SHA-256 est conservé en base.
 */
@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Utilisateur auquel appartient la demande de réinitialisation.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Hash SHA-256 du token réel.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * Après cette date, le token n'est plus accepté.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Renseigné lorsque le token a déjà permis
     * de changer le mot de passe.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Date créée directement par MariaDB.
     */
    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    protected PasswordResetToken() {
    }

    public PasswordResetToken(
        User user,
        String tokenHash,
        LocalDateTime expiresAt
    ) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Un token est utilisable uniquement s'il n'a jamais servi
     * et si sa date d'expiration n'est pas dépassée.
     */
    public boolean isUsableAt(LocalDateTime now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public void markAsUsed(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}