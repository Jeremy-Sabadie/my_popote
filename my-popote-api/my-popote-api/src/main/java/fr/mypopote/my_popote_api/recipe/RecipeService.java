package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientResponse;
import fr.mypopote.my_popote_api.recipe.dto.RecipeRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.recipe.dto.TagResponse;
import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
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
    private final TagRepository tagRepository;

    public RecipeService(
        RecipeRepository recipeRepository,
        UserRepository userRepository,
        IngredientService ingredientService,
        TagRepository tagRepository
    ) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.ingredientService = ingredientService;
        this.tagRepository = tagRepository;
    }

    /**
     * Retourne les recettes de l'utilisateur avec filtres facultatifs.
     *
     * Plusieurs tags utilisent une logique AND :
     * la recette doit posséder tous les tags demandés.
     */
    public List<RecipeResponse> findAllByUserId(
        Long userId,
        String category,
        String season,
        Set<Long> tagIds
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

        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Long> normalizedTagIds =
                new LinkedHashSet<>(tagIds);

            Set<Long> matchingRecipeIds =
                recipeRepository
                    .findAllByUserIdAndAllTagIds(
                        userId,
                        normalizedTagIds,
                        normalizedTagIds.size()
                    )
                    .stream()
                    .map(Recipe::getId)
                    .collect(Collectors.toSet());

            recipes = recipes.stream()
                .filter(recipe ->
                    matchingRecipeIds.contains(recipe.getId())
                )
                .toList();
        }

        return recipes.stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Compatibilité avec les appels qui utilisent uniquement
     * les anciens filtres catégorie et saison.
     */
    public List<RecipeResponse> findAllByUserId(
        Long userId,
        String category,
        String season
    ) {
        return findAllByUserId(
            userId,
            category,
            season,
            Set.of()
        );
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
        applyTags(recipe, request.tagIds());

        return toResponse(recipeRepository.save(recipe));
    }

    /**
     * Modifie uniquement une recette appartenant à l'utilisateur connecté.
     *
     * Les anciennes associations sont supprimées et synchronisées avec
     * la base avant de créer les nouvelles. Cela évite qu'une association
     * identique soit temporairement présente deux fois lors du flush
     * Hibernate et déclenche la contrainte unique de recipe_ingredient.
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

        /*
         * On supprime d'abord les anciennes associations.
         */
        recipe.clearIngredients();
        recipe.clearSeasons();
        recipe.clearTags();

        /*
         * Le flush force Hibernate à exécuter les DELETE maintenant.
         *
         * Sans ce flush, Hibernate peut tenter d'insérer les nouvelles
         * associations avant d'avoir supprimé les anciennes, ce qui
         * provoque notamment uk_recipe_ingredient sur un ingrédient
         * conservé pendant une modification.
         */
        recipeRepository.flush();

        applyIngredients(recipe, request.ingredients());
        applySeasons(recipe, request.seasons());
        applyTags(recipe, request.tagIds());

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
     * Associe les tags sélectionnés à la recette.
     *
     * Les tags doivent obligatoirement exister dans le référentiel.
     */
    private void applyTags(
        Recipe recipe,
        Set<Long> tagIds
    ) {
        if (tagIds.isEmpty()) {
            return;
        }

        Set<Long> uniqueTagIds =
            new LinkedHashSet<>(tagIds);

        List<Tag> tags =
            tagRepository.findAllById(uniqueTagIds);

        if (tags.size() != uniqueTagIds.size()) {
            throw new IllegalArgumentException("Tag not found");
        }

        tags.forEach(recipe::addTag);
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

        Set<TagResponse> tags =
            recipe.getTags()
                .stream()
                .map(tag ->
                    new TagResponse(
                        tag.getId(),
                        tag.getName(),
                        tag.getGroupName()
                    )
                )
                .collect(
                    Collectors.toCollection(
                        LinkedHashSet::new
                    )
                );

        return new RecipeResponse(
            recipe.getId(),
            recipe.getName(),
            recipe.getCategory(),
            recipe.getServings(),
            recipe.getEstimatedCost(),
            recipe.getInstructions(),
            ingredients,
            seasons,
            tags
        );
    }
}