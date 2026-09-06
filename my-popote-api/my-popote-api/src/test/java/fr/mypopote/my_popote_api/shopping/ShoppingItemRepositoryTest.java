package fr.mypopote.my_popote_api.shopping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import fr.mypopote.my_popote_api.config.TestDatabaseConfig;

/**
 * Vérifie que le repository des éléments de liste de courses
 * est correctement chargé par Spring Data JPA.
 */
@DataJpaTest
@Import(TestDatabaseConfig.class)
class ShoppingItemRepositoryTest {

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Test
    void shouldStartWithEmptyRepository() {

        // La base Testcontainers est neuve au démarrage du test.
        assertThat(shoppingItemRepository.count()).isZero();
    }
}