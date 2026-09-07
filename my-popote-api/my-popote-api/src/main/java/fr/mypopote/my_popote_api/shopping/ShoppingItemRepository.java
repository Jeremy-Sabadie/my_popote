package fr.mypopote.my_popote_api.shopping;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository des articles des listes de courses.
 */
public interface ShoppingItemRepository
        extends JpaRepository<ShoppingItem, Long> {

    /**
     * Retourne tous les articles d'une liste dans leur ordre de création.
     *
     * Cette méthode fonctionne également avec les ajouts manuels
     * qui ne possèdent pas forcément d'Ingredient associé.
     */
    List<ShoppingItem> findAllByShoppingListIdOrderByIdAsc(
        Long shoppingListId
    );

    /**
     * Méthode historique conservée pour compatibilité.
     *
     * Elle reste notamment utilisée dans les tests existants.
     */
    List<ShoppingItem> findAllByShoppingListIdOrderByIngredientNameAsc(
        Long shoppingListId
    );

    /**
     * Retourne uniquement les articles provenant
     * automatiquement des recettes.
     */
    List<ShoppingItem> findAllByShoppingListIdAndManualFalse(
        Long shoppingListId
    );

    /**
     * Supprime uniquement les articles générés depuis les recettes.
     *
     * Les "petites envies" ajoutées manuellement sont ainsi
     * conservées lors d'une régénération.
     */
    void deleteAllByShoppingListIdAndManualFalse(
        Long shoppingListId
    );

    /**
     * Recherche sécurisée d'un article appartenant
     * à l'utilisateur connecté.
     */
    Optional<ShoppingItem> findByIdAndShoppingListMealPlanUserId(
        Long shoppingItemId,
        Long userId
    );
}