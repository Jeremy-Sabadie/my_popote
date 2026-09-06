package fr.mypopote.my_popote_api.shopping;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé de la gestion des listes de courses.
 *
 * Toute récupération d'une liste destinée à l'utilisateur
 * est limitée au propriétaire du planning associé.
 */
@Service
@Transactional(readOnly = true)
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;

    public ShoppingListService(
        ShoppingListRepository shoppingListRepository
    ) {
        this.shoppingListRepository = shoppingListRepository;
    }

    /**
     * Recherche une liste appartenant à l'utilisateur authentifié.
     */
    public ShoppingList findByIdAndUserId(
        Long shoppingListId,
        Long userId
    ) {
        return shoppingListRepository
            .findByIdAndMealPlanUserId(shoppingListId, userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Shopping list not found"
            ));
    }
}