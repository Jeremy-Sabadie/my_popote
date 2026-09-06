package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST dédiée aux listes de courses.
 *
 * La génération et l'agrégation des ingrédients resteront
 * dans la couche Service.
 */
@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    public ShoppingListController(
        ShoppingListService shoppingListService
    ) {
        this.shoppingListService = shoppingListService;
    }

    /**
     * Retourne une liste de courses à partir de son identifiant.
     *
     * Les items seront ajoutés lorsque leur logique métier
     * sera implémentée.
     */
    @GetMapping("/{shoppingListId}")
    public ShoppingListResponse findById(
        @PathVariable Long shoppingListId
    ) {
        ShoppingList shoppingList =
            shoppingListService.findById(shoppingListId);

        Long mealPlanId = shoppingList.getMealPlan() == null
            ? null
            : shoppingList.getMealPlan().getId();

        return new ShoppingListResponse(
            shoppingList.getId(),
            mealPlanId,
            List.of()
        );
    }
}