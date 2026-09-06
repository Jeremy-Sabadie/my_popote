package fr.mypopote.my_popote_api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MariaDBContainer;

/**
 * Configuration de base de données réservée aux tests.
 *
 * Une instance MariaDB temporaire est créée avec Testcontainers.
 * Les tests ne doivent ainsi jamais utiliser la base MariaDB Cloud.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestDatabaseConfig {

    @Bean
    @ServiceConnection
    MariaDBContainer<?> mariaDBContainer() {
        return new MariaDBContainer<>(
            "mariadb:11.8"
        );
    }
}