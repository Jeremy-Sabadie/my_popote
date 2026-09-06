package fr.mypopote.my_popote_api.shopping;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemUpdateRequest;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Endpoints liés aux listes de courses.
 *
 * L'identifiant utilisateur vient toujours du JWT et jamais du client.
 */
@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final CurrentUserService currentUserService;

    public ShoppingListController(
            ShoppingListService shoppingListService,
            CurrentUserService currentUserService) {

        this.shoppingListService = shoppingListService;
        this.currentUserService = currentUserService;
    }

    /**
     * Génère ou régénère la liste de courses d'un planning.
     */
    @PostMapping("/generate/{mealPlanId}")
    public ShoppingListResponse generate(
            @PathVariable Long mealPlanId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = currentUserService.getUserId(jwt);

        return shoppingListService.generate(
            mealPlanId,
            userId
        );
    }

    /**
     * Retourne une liste de courses appartenant à l'utilisateur.
     */
    @GetMapping("/{shoppingListId}")
    public ShoppingListResponse getShoppingList(
            @PathVariable Long shoppingListId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = currentUserService.getUserId(jwt);

        return shoppingListService.findByIdAndUserId(
            shoppingListId,
            userId
        );
    }

    /**
     * Coche ou décoche un article pendant les courses.
     */
    @PatchMapping("/items/{shoppingItemId}")
    public ShoppingItemResponse updateChecked(
            @PathVariable Long shoppingItemId,
            @RequestBody ShoppingItemUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = currentUserService.getUserId(jwt);

        return shoppingListService.updateChecked(
            shoppingItemId,
            userId,
            request.checked()
        );
    }
}