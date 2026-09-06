package fr.mypopote.my_popote_api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

/**
 * Service chargé de créer et de lire les JWT de My Popote.
 *
 * Le token permet d'identifier l'utilisateur authentifié
 * sans stocker de session côté serveur.
 */
@Service
public class JwtService {

    private static final Duration TOKEN_LIFETIME =
        Duration.ofMinutes(30);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    /**
     * Constructeur utilisé par Spring.
     *
     * Le secret JWT est fourni par la configuration
     * et ne doit jamais être écrit directement dans le code.
     */
    public JwtService(
        @Value("${app.jwt.secret}")
        String secret
    ) {
        SecretKey secretKey = buildSecretKey(secret);

        this.jwtEncoder =
            NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();

        this.jwtDecoder =
            NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Fabrique pratique pour les tests unitaires
     * avec une clé totalement indépendante de la production.
     */
    public static JwtService forSecret(String secret) {
        return new JwtService(secret);
    }

    public String generateToken(
        Long userId,
        String email
    ) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plus(TOKEN_LIFETIME))
            .subject(userId.toString())
            .claim("email", email)
            .build();

        JwsHeader header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build();

        return jwtEncoder.encode(
            JwtEncoderParameters.from(
                header,
                claims
            )
        ).getTokenValue();
    }

    public Jwt decodeToken(String token) {
        return jwtDecoder.decode(token);
    }

    /**
     * Construit la clé cryptographique utilisée
     * pour signer et vérifier les JWT HS256.
     */
    private static SecretKey buildSecretKey(String secret) {
        return new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
    }
}