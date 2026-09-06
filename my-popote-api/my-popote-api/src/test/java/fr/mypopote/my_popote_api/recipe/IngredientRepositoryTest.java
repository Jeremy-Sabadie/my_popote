package fr.mypopote.my_popote_api.recipe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class IngredientRepositoryTest {

    @Container
    @ServiceConnection
    static final MariaDBContainer<?> mariadb =
        new MariaDBContainer<>("mariadb:11.8");

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    void shouldFindIngredientByNameIgnoringCase() {
        ingredientRepository.save(
            new Ingredient("Tomate")
        );

        Optional<Ingredient> ingredient =
            ingredientRepository.findByNameIgnoreCase("tomate");

        assertThat(ingredient)
            .isPresent()
            .get()
            .extracting(Ingredient::getName)
            .isEqualTo("Tomate");
    }
}