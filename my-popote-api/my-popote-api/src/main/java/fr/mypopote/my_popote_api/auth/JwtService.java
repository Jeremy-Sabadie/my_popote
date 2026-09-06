package fr.mypopote.my_popote_api.auth;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;

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
public class JwtService {

    private static final Duration TOKEN_LIFETIME =
        Duration.ofMinutes(30);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private JwtService(
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Construit le service à partir du secret partagé.
     *
     * Cette méthode facilite également les tests avec
     * une clé dédiée qui n'a aucun lien avec la production.
     */
    public static JwtService forSecret(String secret) {
        SecretKey secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );

        JwtEncoder encoder =
            NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();

        JwtDecoder decoder =
            NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        return new JwtService(
            encoder,
            decoder
        );
    }

    /**
     * Génère un access token pour l'utilisateur connecté.
     *
     * L'identifiant utilisateur est placé dans "sub".
     * L'email est ajouté comme information complémentaire.
     */
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

    /**
     * Décode et valide un token.
     *
     * Cette méthode est principalement utile pour les tests.
     * Les requêtes HTTP seront ensuite validées directement
     * par Spring Security grâce au JwtDecoder.
     */
    public Jwt decodeToken(String token) {
        return jwtDecoder.decode(token);
    }
}