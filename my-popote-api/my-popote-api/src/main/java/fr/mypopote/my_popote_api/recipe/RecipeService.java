package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientResponse;
import fr.mypopote.my_popote_api.recipe.dto.RecipeRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service chargé des opérations métier liées aux recettes.
 *
 * L'identifiant utilisateur provient toujours du JWT et toutes les
 * recherches de recettes sont limitées à cet utilisateur.
 */
@Service
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final IngredientService ingredientService;

    public RecipeService(
        RecipeRepository recipeRepository,
        UserRepository userRepository,
        IngredientService ingredientService
    ) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.ingredientService = ingredientService;
    }

    /**
     * Retourne les recettes de l'utilisateur avec filtres facultatifs.
     */
    public List<RecipeResponse> findAllByUserId(
        Long userId,
        String category,
        String season
    ) {
        List<Recipe> recipes;

        if (category != null && season != null) {
            recipes =
                recipeRepository
                    .findDistinctByUserIdAndCategoryIgnoreCaseAndSeasonsSeasonIgnoreCase(
                        userId,
                        category,
                        season
                    );
        } else if (category != null) {
            recipes =
                recipeRepository.findAllByUserIdAndCategoryIgnoreCase(
                    userId,
                    category
                );
        } else if (season != null) {
            recipes =
                recipeRepository.findDistinctByUserIdAndSeasonsSeasonIgnoreCase(
                    userId,
                    season
                );
        } else {
            recipes = recipeRepository.findAllByUserId(userId);
        }

        return recipes.stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Conserve la méthode déjà utilisée par certains tests existants.
     */
    public List<Recipe> findAllByUserId(Long userId) {
        return recipeRepository.findAllByUserId(userId);
    }

    /**
     * Consulte une recette appartenant à l'utilisateur connecté.
     */
    public RecipeResponse findById(Long recipeId, Long userId) {
        return toResponse(findOwnedRecipe(recipeId, userId));
    }

    /**
     * Crée une recette pour l'utilisateur authentifié.
     *
     * Aucun userId venant du frontend n'est accepté.
     */
    @Transactional
    public RecipeResponse create(
        RecipeRequest request,
        Long userId
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );

        Recipe recipe = new Recipe(
            user,
            request.name().trim(),
            request.category().trim().toUpperCase(),
            request.servings(),
            request.estimatedCost(),
            request.instructions()
        );

        applyIngredients(recipe, request.ingredients());
        applySeasons(recipe, request.seasons());

        return toResponse(recipeRepository.save(recipe));
    }

    /**
     * Modifie uniquement une recette appartenant à l'utilisateur connecté.
     */
    @Transactional
    public RecipeResponse update(
        Long recipeId,
        RecipeRequest request,
        Long userId
    ) {
        Recipe recipe = findOwnedRecipe(recipeId, userId);

        recipe.setName(request.name().trim());
        recipe.setCategory(request.category().trim().toUpperCase());
        recipe.setServings(request.servings());
        recipe.setEstimatedCost(request.estimatedCost());
        recipe.setInstructions(request.instructions());

        recipe.clearIngredients();
        recipe.clearSeasons();

        applyIngredients(recipe, request.ingredients());
        applySeasons(recipe, request.seasons());

        return toResponse(recipeRepository.save(recipe));
    }

    /**
     * Supprime uniquement une recette appartenant à l'utilisateur connecté.
     */
    @Transactional
    public void delete(Long recipeId, Long userId) {
        Recipe recipe = findOwnedRecipe(recipeId, userId);
        recipeRepository.delete(recipe);
    }

    /**
     * Recherche une recette en vérifiant son propriétaire.
     */
    private Recipe findOwnedRecipe(Long recipeId, Long userId) {
        return recipeRepository.findByIdAndUserId(recipeId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("Recipe not found")
            );
    }

    /**
     * Construit les associations recette-ingrédient.
     */
    private void applyIngredients(
        Recipe recipe,
        List<RecipeIngredientRequest> requests
    ) {
        for (RecipeIngredientRequest request : requests) {
            Ingredient ingredient =
                ingredientService.findOrCreate(
                    request.ingredientName().trim()
                );

            recipe.addIngredient(
                ingredient,
                request.quantity(),
                request.unit().trim()
            );
        }
    }

    /**
     * Construit les saisons en normalisant leur valeur.
     */
    private void applySeasons(
        Recipe recipe,
        Set<String> seasons
    ) {
        seasons.stream()
            .map(String::trim)
            .map(String::toUpperCase)
            .distinct()
            .forEach(recipe::addSeason);
    }

    /**
     * Transforme l'entité JPA en DTO REST.
     */
    private RecipeResponse toResponse(Recipe recipe) {

        List<RecipeIngredientResponse> ingredients =
            recipe.getIngredients()
                .stream()
                .map(recipeIngredient ->
                    new RecipeIngredientResponse(
                        recipeIngredient.getIngredient().getId(),
                        recipeIngredient.getIngredient().getName(),
                        recipeIngredient.getQuantity(),
                        recipeIngredient.getUnit()
                    )
                )
                .toList();

        Set<String> seasons =
            recipe.getSeasons()
                .stream()
                .map(RecipeSeason::getSeason)
                .collect(Collectors.toSet());

        return new RecipeResponse(
            recipe.getId(),
            recipe.getName(),
            recipe.getCategory(),
            recipe.getServings(),
            recipe.getEstimatedCost(),
            recipe.getInstructions(),
            ingredients,
            seasons
        );
    }
}