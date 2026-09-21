
package fr.mypopote.my_popote_api.shopping;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import fr.mypopote.my_popote_api.config.TestDatabaseConfig;
import fr.mypopote.my_popote_api.planning.MealPlan;
import fr.mypopote.my_popote_api.planning.MealPlanRepository;
import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;

/**
 * Vérifie le filtrage et le tri de l'historique des listes de courses
 * sur une véritable base MariaDB de test.
 */
@DataJpaTest
@Import(TestDatabaseConfig.class)
class ShoppingListRepositoryTest {

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private MealPlanRepository mealPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnOnlyAuthenticatedUsersListsOrderedByNewestWeek() {

        User user = userRepository.save(
            new User(
                "jeremy@example.com",
                "hashed-password",
                "Jérémy"
            )
        );

        User otherUser = userRepository.save(
            new User(
                "other@example.com",
                "hashed-password",
                "Autre"
            )
        );

        MealPlan olderMealPlan = mealPlanRepository.save(
            new MealPlan(
                user,
                LocalDate.of(2026, 9, 7),
                false,
                null,
                null
            )
        );

        MealPlan recentMealPlan = mealPlanRepository.save(
            new MealPlan(
                user,
                LocalDate.of(2026, 9, 14),
                false,
                null,
                null
            )
        );

        MealPlan otherUsersMealPlan = mealPlanRepository.save(
            new MealPlan(
                otherUser,
                LocalDate.of(2026, 9, 21),
                false,
                null,
                null
            )
        );

        ShoppingList olderList = shoppingListRepository.save(
            new ShoppingList(olderMealPlan)
        );

        ShoppingList recentList = shoppingListRepository.save(
            new ShoppingList(recentMealPlan)
        );

        shoppingListRepository.save(
            new ShoppingList(otherUsersMealPlan)
        );

        /*
         * Force l'exécution des INSERT avant la lecture :
         * la requête est ainsi vérifiée sur les données en base.
         */
        shoppingListRepository.flush();

        List<ShoppingList> history =
            shoppingListRepository
                .findAllByMealPlanUserIdOrderByMealPlanWeekStartDateDescIdDesc(
                    user.getId()
                );

        assertThat(history)
            .extracting(ShoppingList::getId)
            .containsExactly(
                recentList.getId(),
                olderList.getId()
            );

        assertThat(history)
            .extracting(list -> list.getMealPlan().getWeekStartDate())
            .containsExactly(
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 7)
            );
    }
}