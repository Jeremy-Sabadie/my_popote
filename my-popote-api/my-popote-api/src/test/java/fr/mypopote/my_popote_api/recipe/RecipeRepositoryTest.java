package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class RecipeRepositoryTest {

    @Container
    @ServiceConnection
    static final MariaDBContainer<?> mariadb =
        new MariaDBContainer<>("mariadb:11.8");

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnOnlyRecipesOwnedByGivenUser() {
        User jeremy = userRepository.save(
            new User(
                "jeremy@example.com",
                "hashed-password",
                "Jérémy"
            )
        );

        User alice = userRepository.save(
            new User(
                "alice@example.com",
                "hashed-password",
                "Alice"
            )
        );

        recipeRepository.save(
            new Recipe(
                jeremy,
                "Pâtes carbonara",
                "MEAT",
                2,
                new BigDecimal("6.50"),
                "Préparer les pâtes."
            )
        );

        recipeRepository.save(
            new Recipe(
                alice,
                "Salade composée",
                "SALAD",
                1,
                new BigDecimal("4.00"),
                "Préparer la salade."
            )
        );

        List<Recipe> recipes =
            recipeRepository.findAllByUserId(jeremy.getId());

        assertThat(recipes)
            .hasSize(1)
            .extracting(Recipe::getName)
            .containsExactly("Pâtes carbonara");
    }
}