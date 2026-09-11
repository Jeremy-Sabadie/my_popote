package fr.mypopote.my_popote_api.auth.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository chargé de l'accès aux tokens
 * de réinitialisation de mot de passe.
 */
public interface PasswordResetTokenRepository
    extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Permet d'invalider les anciennes demandes encore actives
     * lorsqu'un utilisateur demande un nouveau lien.
     */
    List<PasswordResetToken> findAllByUserIdAndUsedAtIsNull(Long userId);
}