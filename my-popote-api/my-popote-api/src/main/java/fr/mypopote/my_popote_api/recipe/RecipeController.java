package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST dédiée à la gestion des recettes.
 *
 * L'identité utilisateur vient exclusivement du JWT authentifié.
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
     * Liste les recettes avec filtres facultatifs.
     *
     * Exemples :
     * /api/recipes
     * /api/recipes?category=MEAT
     * /api/recipes?season=SUMMER
     * /api/recipes?category=SALAD&season=SUMMER
     */
    @GetMapping
    public List<RecipeResponse> findAll(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String season
    ) {
        Long userId = currentUserService.getUserId(jwt);

        return recipeService.findAllByUserId(
            userId,
            category,
            season
        );
    }

    /**
     * Consulte une recette précise.
     */
    @GetMapping("/{recipeId}")
    public RecipeResponse findById(
        @PathVariable Long recipeId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserService.getUserId(jwt);

        return recipeService.findById(recipeId, userId);
    }

    /**
     * Crée une nouvelle recette.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse create(
        @Valid @RequestBody RecipeRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserService.getUserId(jwt);

        return recipeService.create(request, userId);
    }

    /**
     * Modifie une recette existante.
     */
    @PutMapping("/{recipeId}")
    public RecipeResponse update(
        @PathVariable Long recipeId,
        @Valid @RequestBody RecipeRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserService.getUserId(jwt);

        return recipeService.update(
            recipeId,
            request,
            userId
        );
    }

    /**
     * Supprime une recette appartenant à l'utilisateur connecté.
     */
    @DeleteMapping("/{recipeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable Long recipeId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserService.getUserId(jwt);

        recipeService.delete(recipeId, userId);
    }
}