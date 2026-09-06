package fr.mypopote.my_popote_api.shopping;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé de la gestion des listes de courses.
 *
 * La logique d'agrégation des ingrédients sera ajoutée plus tard
 * avec ses propres tests métier.
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
     * Recherche une liste de courses par son identifiant.
     *
     * Une exception métier dédiée remplacera IllegalArgumentException
     * lorsque nous construirons la gestion centralisée des erreurs.
     */
    public ShoppingList findById(Long shoppingListId) {
        return shoppingListRepository.findById(shoppingListId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Shopping list not found with id: " + shoppingListId
            ));
    }
}