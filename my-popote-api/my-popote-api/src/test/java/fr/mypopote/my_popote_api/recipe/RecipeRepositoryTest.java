package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

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

    @Test
    void shouldPersistSeveralTagsForRecipe() {
        User user = userRepository.save(
            new User(
                "jeremy.tags@example.com",
                "hashed-password",
                "Jérémy"
            )
        );

        Tag proteinTag = tagRepository.save(
            new Tag(
                "Très protéiné",
                "NUTRITION"
            )
        );

        Tag quickTag = tagRepository.save(
            new Tag(
                "Très rapide",
                "PRACTICAL"
            )
        );

        Recipe recipe = new Recipe(
            user,
            "Poulet express",
            "MEAT",
            2,
            new BigDecimal("7.50"),
            "Préparer le poulet."
        );

        recipe.addTag(proteinTag);
        recipe.addTag(quickTag);

        Recipe savedRecipe =
            recipeRepository.save(recipe);

        entityManager.flush();
        entityManager.clear();

        Recipe reloadedRecipe =
            recipeRepository
                .findByIdAndUserId(
                    savedRecipe.getId(),
                    user.getId()
                )
                .orElseThrow();

        assertThat(reloadedRecipe.getTags())
            .extracting(Tag::getName)
            .containsExactlyInAnyOrder(
                "Très protéiné",
                "Très rapide"
            );
    }

    @Test
    void shouldReturnOnlyRecipesContainingAllRequestedTags() {
        User user = userRepository.save(
            new User(
                "jeremy.filter@example.com",
                "hashed-password",
                "Jérémy"
            )
        );

        Tag proteinTag = tagRepository.save(
            new Tag(
                "Filtre protéines",
                "NUTRITION"
            )
        );

        Tag dryTag = tagRepository.save(
            new Tag(
                "Filtre sèche",
                "NUTRITION"
            )
        );

        Tag quickTag = tagRepository.save(
            new Tag(
                "Filtre rapide",
                "PRACTICAL"
            )
        );

        Recipe matchingRecipe = new Recipe(
            user,
            "Poulet sèche",
            "MEAT",
            2,
            new BigDecimal("7.00"),
            null
        );

        matchingRecipe.addTag(proteinTag);
        matchingRecipe.addTag(dryTag);
        matchingRecipe.addTag(quickTag);

        Recipe partialRecipe = new Recipe(
            user,
            "Poulet simple",
            "MEAT",
            2,
            new BigDecimal("6.00"),
            null
        );

        partialRecipe.addTag(proteinTag);
        partialRecipe.addTag(quickTag);

        recipeRepository.save(matchingRecipe);
        recipeRepository.save(partialRecipe);

        entityManager.flush();
        entityManager.clear();

        Set<Long> requestedTagIds =
            Set.of(
                proteinTag.getId(),
                dryTag.getId()
            );

        List<Recipe> result =
            recipeRepository.findAllByUserIdAndAllTagIds(
                user.getId(),
                requestedTagIds,
                requestedTagIds.size()
            );

        assertThat(result)
            .extracting(Recipe::getName)
            .containsExactly("Poulet sèche");
    }
}