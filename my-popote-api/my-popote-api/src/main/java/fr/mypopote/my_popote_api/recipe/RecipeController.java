package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * API REST dédiée aux recettes.
 *
 * L'utilisateur n'est jamais identifié à partir d'un identifiant
 * fourni par le frontend. Son identité provient du JWT authentifié.
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final CurrentUserService currentUserService;

    public RecipeController(
        RecipeService recipeService,
        CurrentUserService currentUserService
    ) {
        this.recipeService = recipeService;
        this.currentUserService = currentUserService;
    }

    /**
     * Retourne uniquement les recettes de l'utilisateur connecté.
     */
    @GetMapping
    public List<RecipeResponse> findAll(
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserService.getUserId(jwt);

        return recipeService.findAllByUserId(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Transforme l'entité JPA en DTO destiné au frontend.
     *
     * Les ingrédients et saisons seront ajoutés lorsque
     * leur chargement sera pris en charge par le service.
     */
    private RecipeResponse toResponse(Recipe recipe) {
        return new RecipeResponse(
            recipe.getId(),
            recipe.getName(),
            recipe.getCategory(),
            recipe.getServings(),
            recipe.getEstimatedCost(),
            recipe.getInstructions(),
            List.of(),
            Set.of()
        );
    }
}