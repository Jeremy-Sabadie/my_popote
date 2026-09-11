package fr.mypopote.my_popote_api;

import fr.mypopote.my_popote_api.config.TestDatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Vérifie que le contexte complet de l'application démarre correctement.
 *
 * La base utilisée ici est une MariaDB Testcontainers temporaire.
 * Ce test ne doit jamais se connecter à MariaDB Cloud.
 *
 * Les paramètres SMTP sont également fictifs :
 * aucun e-mail n'est envoyé pendant ce test.
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

    @Test
    void contextLoads() {
        // Le démarrage réussi du contexte constitue le test.
    }
}