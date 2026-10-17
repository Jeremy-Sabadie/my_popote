
package fr.mypopote.my_popote_api;

import fr.mypopote.my_popote_api.config.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/**
 * Vérifie le démarrage complet de My Popote.
 *
 * Utilise MariaDB Testcontainers, des paramètres SMTP fictifs
 * et une paire de clés RSA temporaire dédiée aux tests.
 */
@SpringBootTest(
    properties = {
        "app.frontend.url=http://localhost:4200",
        "app.mail.from=test@my-popote.local",
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "spring.mail.username=test",
        "spring.mail.password=test"
    }
)
@Import(TestDatabaseConfig.class)
class MyPopoteApiApplicationTests {

    private static final Path PRIVATE_KEY_PATH;
    private static final Path PUBLIC_KEY_PATH;

    static {
        try {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);

            KeyPair keyPair = generator.generateKeyPair();

            PRIVATE_KEY_PATH = writeTemporaryPem(
                    "my-popote-test-private-",
                    "PRIVATE KEY",
                    keyPair.getPrivate().getEncoded()
            );

            PUBLIC_KEY_PATH = writeTemporaryPem(
                    "my-popote-test-public-",
                    "PUBLIC KEY",
                    keyPair.getPublic().getEncoded()
            );
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    /**
     * Fournit la configuration JWT propre au contexte de test.
     */
    @DynamicPropertySource
    static void configureJwt(DynamicPropertyRegistry registry) {
        registry.add(
                "app.jwt.private-key-path",
                () -> PRIVATE_KEY_PATH.toString()
        );
        registry.add(
                "app.jwt.public-key-path",
                () -> PUBLIC_KEY_PATH.toString()
        );
        registry.add(
                "app.jwt.issuer",
                () -> "my-popote-test"
        );
        registry.add(
                "app.jwt.audience",
                () -> "mypopote"
        );
    }

    /**
     * Crée un fichier PEM temporaire sans utiliser les clés
     * de développement ou de production.
     */
    private static Path writeTemporaryPem(
            String prefix,
            String keyType,
            byte[] encodedKey
    ) throws Exception {
        Path path = Files.createTempFile(prefix, ".pem");

        String base64 = Base64.getMimeEncoder(
                64,
                new byte[]{'\n'}
        ).encodeToString(encodedKey);

        String pem = "-----BEGIN " + keyType + "-----\n"
                + base64
                + "\n-----END " + keyType + "-----\n";

        Files.writeString(path, pem);
        path.toFile().deleteOnExit();

        return path;
    }

    @Test
    void contextLoads() {
        // Le démarrage du contexte constitue le test.
    }
}
