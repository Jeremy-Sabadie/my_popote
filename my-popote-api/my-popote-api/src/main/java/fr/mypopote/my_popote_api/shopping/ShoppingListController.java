package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.shopping.dto.ManualShoppingItemRequest;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemAlreadyOwnedRequest;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemUpdateRequest;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            CurrentUserService currentUserService) {

        this.shoppingListService = shoppingListService;
        this.currentUserService = currentUserService;
    }

    /**
     * Génère la liste de courses d'un planning.
     */
    @PostMapping("/meal-plans/{mealPlanId}")
    public ShoppingListResponse generate(
            @PathVariable Long mealPlanId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId =
            currentUserService.getUserId(jwt);

        return shoppingListService.generate(
            mealPlanId,
            userId
        );
    }

    /**
     * Retourne une liste appartenant à l'utilisateur connecté.
     */
    @GetMapping("/{shoppingListId}")
    public ShoppingListResponse getShoppingList(
            @PathVariable Long shoppingListId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId =
            currentUserService.getUserId(jwt);

        return shoppingListService.findByIdAndUserId(
            shoppingListId,
            userId
        );
    }

    /**
     * Alias conservé pour les appels existants utilisant findById.
     *
     * Il n'est pas exposé comme une seconde route HTTP.
     */
    public ShoppingListResponse findById(
            Jwt jwt,
            Long shoppingListId) {

        return getShoppingList(
            shoppingListId,
            jwt
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

        Long userId =
            currentUserService.getUserId(jwt);

        return shoppingListService.updateChecked(
            shoppingItemId,
            userId,
            request.checked()
        );
    }

    /**
     * Correspond côté frontend à :
     * "C'est bon, j'en ai déjà".
     */
    @PatchMapping("/items/{shoppingItemId}/already-owned")
    public ShoppingItemResponse updateAlreadyOwned(
            @PathVariable Long shoppingItemId,
            @RequestBody ShoppingItemAlreadyOwnedRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId =
            currentUserService.getUserId(jwt);

        return shoppingListService.updateAlreadyOwned(
            shoppingItemId,
            userId,
            request.alreadyOwned()
        );
    }

    /**
     * Ajoute une "petite envie" à une liste existante.
     */
    @PostMapping("/{shoppingListId}/items")
    public ShoppingItemResponse addManualItem(
            @PathVariable Long shoppingListId,
            @Valid @RequestBody ManualShoppingItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId =
            currentUserService.getUserId(jwt);

        return shoppingListService.addManualItem(
            shoppingListId,
            userId,
            request
        );
    }
}