package fr.mypopote.my_popote_api.shopping;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository d'accès aux listes de courses.
 *
 * Les recherches utilisées par l'API sont limitées
 * à l'utilisateur propriétaire du planning associé.
 */
public interface ShoppingListRepository
    extends JpaRepository<ShoppingList, Long> {

    /**
     * Recherche une liste uniquement si son planning
     * appartient à l'utilisateur authentifié.
     */
    Optional<ShoppingList> findByIdAndMealPlanUserId(
        Long shoppingListId,
        Long userId
    );
}