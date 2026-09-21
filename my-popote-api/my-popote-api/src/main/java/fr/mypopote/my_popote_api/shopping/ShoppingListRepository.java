package fr.mypopote.my_popote_api.shopping;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingListRepository
        extends JpaRepository<ShoppingList, Long> {

    Optional<ShoppingList> findByIdAndMealPlanUserId(
        Long shoppingListId,
        Long userId
    );

    Optional<ShoppingList> findByMealPlanIdAndMealPlanUserId(
        Long mealPlanId,
        Long userId
    );

    /**
     * Historique de l'utilisateur connecté, trié par semaine décroissante.
     */
    List<ShoppingList> findAllByMealPlanUserIdOrderByMealPlanWeekStartDateDescIdDesc(
        Long userId
    );
}