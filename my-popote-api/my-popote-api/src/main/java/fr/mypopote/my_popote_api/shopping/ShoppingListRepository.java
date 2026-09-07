package fr.mypopote.my_popote_api.shopping;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository d'accès aux listes de courses.
 *
 * Les recherches exposées à l'API tiennent compte
 * du propriétaire du planning.
 */
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
}