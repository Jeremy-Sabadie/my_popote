package fr.mypopote.my_popote_api.shopping;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux listes de courses.
 *
 * Les recherches incluent toujours l'utilisateur propriétaire afin
 * d'empêcher l'accès à la liste d'un autre utilisateur.
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