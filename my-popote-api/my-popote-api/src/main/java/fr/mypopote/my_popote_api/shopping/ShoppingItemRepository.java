package fr.mypopote.my_popote_api.shopping;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux éléments d'une liste de courses.
 *
 * Les recherches utilisées par l'API tiennent compte du propriétaire
 * de la liste afin de garantir l'isolation des données entre utilisateurs.
 */
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {

    List<ShoppingItem> findAllByShoppingListIdOrderByIngredientNameAsc(
        Long shoppingListId
    );

    Optional<ShoppingItem> findByIdAndShoppingListMealPlanUserId(
        Long shoppingItemId,
        Long userId
    );

    void deleteAllByShoppingListId(Long shoppingListId);
}