
package fr.mypopote.my_popote_api.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * Émet et vérifie les JWT RS256 de My Popote.
 *
 * Les jetons générés peuvent être utilisés par My Popote
 * et ProductPulse, avec une authentification commune.
 */
@Service
public class JwtService {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);
    private static final String PRODUCTPULSE_AUDIENCE = "productpulse";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String issuer;
    private final String audience;

    /**
     * Charge les clés RSA depuis des fichiers locaux protégés.
     *
     * Constructeur utilisé par Spring pour l'injection
     * des paramètres de configuration.
     */
    @Autowired
    public JwtService(
            @Value("${app.jwt.private-key-path}") String privateKeyPath,
            @Value("${app.jwt.public-key-path}") String publicKeyPath,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience
    ) throws Exception {
        this(
                readPrivateKey(privateKeyPath),
                readPublicKey(publicKeyPath),
                issuer,
                audience
        );
    }

    /**
     * Constructeur permettant d'injecter directement
     * une paire RSA, notamment pour les tests unitaires.
     */
    public JwtService(
            RSAPrivateKey privateKey,
            RSAPublicKey publicKey,
            String issuer,
            String audience
    ) {
        this.issuer = issuer;
        this.audience = audience;

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        this.jwtEncoder = new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(rsaKey))
        );

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error(
                                        "invalid_token",
                                        "Invalid audience",
                                        null
                                )
                        );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        List.of(
                                JwtValidators.createDefaultWithIssuer(issuer),
                                audienceValidator
                        )
                )
        );

        this.jwtDecoder = decoder;
    }

    /**
     * Génère un JWT RS256 valable une heure.
     *
     * Le jeton contient :
     * - l'identifiant de l'utilisateur ;
     * - son adresse e-mail ;
     * - l'audience My Popote ;
     * - l'audience ProductPulse.
     *
     * @param userId identifiant de l'utilisateur
     * @param email adresse e-mail de l'utilisateur
     * @return JWT signé avec la clé privée RSA
     */
    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience, PRODUCTPULSE_AUDIENCE))
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_LIFETIME))
                .subject(userId.toString())
                .claim("email", email)
                .build();

        JwsHeader header = JwsHeader
                .with(SignatureAlgorithm.RS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    /**
     * Vérifie la signature, l'émetteur, l'expiration
     * et l'audience My Popote du JWT.
     *
     * @param token JWT à vérifier
     * @return JWT décodé et validé
     */
    public Jwt decodeToken(String token) {
        return jwtDecoder.decode(token);
    }

    /**
     * Charge une clé privée RSA au format PEM PKCS#8.
     *
     * @param path chemin du fichier de clé privée
     * @return clé privée RSA
     */
    private static RSAPrivateKey readPrivateKey(String path)
            throws Exception {
        String pem = Files.readString(Path.of(path));

        String encoded = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] bytes = Base64.getDecoder().decode(encoded);

        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    /**
     * Charge une clé publique RSA au format PEM X.509.
     *
     * @param path chemin du fichier de clé publique
     * @return clé publique RSA
     */
    private static RSAPublicKey readPublicKey(String path)
            throws Exception {
        String pem = Files.readString(Path.of(path));

        String encoded = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] bytes = Base64.getDecoder().decode(encoded);

        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(bytes));
    }
}
