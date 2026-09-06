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
 */
@SpringBootTest
@Import(TestDatabaseConfig.class)
class MyPopoteApiApplicationTests {

    @Test
    void contextLoads() {
        // Le démarrage réussi du contexte constitue le test.
    }
}