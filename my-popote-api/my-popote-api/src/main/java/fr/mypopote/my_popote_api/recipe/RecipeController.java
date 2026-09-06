package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * API REST dédiée aux recettes.
 *
 * Les recherches sont limitées à l'utilisateur concerné
 * afin de préserver l'isolation des données.
 */
@RestController
@RequestMapping("/api/users/{userId}/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Retourne les recettes appartenant à l'utilisateur demandé.
     */
    @GetMapping
    public List<RecipeResponse> findAllByUserId(
        @PathVariable Long userId
    ) {
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