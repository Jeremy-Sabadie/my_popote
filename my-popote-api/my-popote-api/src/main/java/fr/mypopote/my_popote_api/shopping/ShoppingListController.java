package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST dédiée aux listes de courses.
 *
 * L'identité utilisateur provient exclusivement du JWT.
 */
@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final CurrentUserService currentUserService;

    public ShoppingListController(
        ShoppingListService shoppingListService,
        CurrentUserService currentUserService
    ) {
        this.shoppingListService = shoppingListService;
        this.currentUserService = currentUserService;
    }

    /**
     * Retourne une liste uniquement si elle appartient
     * à l'utilisateur connecté.
     */
    @GetMapping("/{shoppingListId}")
    public ShoppingListResponse findById(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long shoppingListId
    ) {
        Long userId = currentUserService.getUserId(jwt);

        ShoppingList shoppingList =
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            );

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