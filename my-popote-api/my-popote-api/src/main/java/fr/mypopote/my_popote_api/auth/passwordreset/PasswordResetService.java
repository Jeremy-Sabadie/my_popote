package fr.mypopote.my_popote_api.auth.passwordreset;

import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * Gère le cycle de vie d'une réinitialisation de mot de passe.
 *
 * Le token réel n'est jamais stocké en base :
 * seul son hash SHA-256 est enregistré.
 */
@Service
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final int TOKEN_SIZE_BYTES = 32;
    private static final int TOKEN_LIFETIME_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetNotifier passwordResetNotifier;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
        UserRepository userRepository,
        PasswordResetTokenRepository tokenRepository,
        PasswordEncoder passwordEncoder,
        PasswordResetNotifier passwordResetNotifier
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetNotifier = passwordResetNotifier;
    }

    /**
     * Lance une demande de réinitialisation si l'adresse existe.
     *
     * Une adresse inconnue ne provoque volontairement aucune erreur
     * afin de ne pas permettre de découvrir les comptes existants.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository
            .findByEmailIgnoreCase(email)
            .ifPresent(this::createAndSendToken);
    }

    /**
     * Vérifie le token puis remplace le mot de passe.
     */
    @Transactional
    public void resetPassword(
        String rawToken,
        String newPassword
    ) {
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = tokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Invalid or expired password reset token"
                )
            );

        LocalDateTime now = LocalDateTime.now();

        if (!resetToken.isUsableAt(now)) {
            throw new IllegalArgumentException(
                "Invalid or expired password reset token"
            );
        }

        User user = resetToken.getUser();

        /*
         * Le mot de passe brut n'est jamais sauvegardé.
         */
        user.setPasswordHash(
            passwordEncoder.encode(newPassword)
        );

        /*
         * Le token est invalidé immédiatement après utilisation.
         */
        resetToken.markAsUsed(now);

        userRepository.save(user);
        tokenRepository.save(resetToken);
    }

    /**
     * Invalide les anciennes demandes puis génère
     * un nouveau token valable pendant 30 minutes.
     */
    private void createAndSendToken(User user) {
        LocalDateTime now = LocalDateTime.now();

        List<PasswordResetToken> previousTokens =
            tokenRepository.findAllByUserIdAndUsedAtIsNull(
                user.getId()
            );

        for (PasswordResetToken previousToken : previousTokens) {
            previousToken.markAsUsed(now);
        }

        tokenRepository.saveAll(previousTokens);

        String rawToken = generateSecureToken();

        PasswordResetToken resetToken =
            new PasswordResetToken(
                user,
                hashToken(rawToken),
                now.plusMinutes(TOKEN_LIFETIME_MINUTES)
            );

        tokenRepository.save(resetToken);

        /*
         * Le token brut quitte le service uniquement vers
         * le canal chargé de le transmettre à l'utilisateur.
         */
        passwordResetNotifier.sendPasswordReset(
            user,
            rawToken
        );
    }

    /**
     * Génère 256 bits aléatoires avec SecureRandom.
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_SIZE_BYTES];

        secureRandom.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Produit le hash SHA-256 conservé en base.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            /*
             * SHA-256 est obligatoire dans une JVM standard.
             */
            throw new IllegalStateException(
                "SHA-256 is not available",
                exception
            );
        }
    }
}