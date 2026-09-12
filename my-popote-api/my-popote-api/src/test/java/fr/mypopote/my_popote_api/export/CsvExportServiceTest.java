package fr.mypopote.my_popote_api.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.mypopote.my_popote_api.recipe.RecipeService;
import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientResponse;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.shopping.ShoppingListService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Tests unitaires du service d'export.
 *
 * Une recette est exportée sous forme de texte lisible.
 * Les listes de courses restent exportées au format CSV.
 *
 * Les tests vérifient également que les services métier sont appelés
 * avec l'identifiant de l'utilisateur connecté afin de préserver
 * l'isolation des données.
 */
@ExtendWith(MockitoExtension.class)
class CsvExportServiceTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private ShoppingListService shoppingListService;

    private CsvExportService csvExportService;

    @BeforeEach
    void setUp() {

        csvExportService = new CsvExportService(
            recipeService,
            shoppingListService
        );
    }

    /**
     * Vérifie qu'une recette précise est exportée sous forme
     * de fiche texte lisible pour l'utilisateur.
     */
    @Test
    void shouldExportRecipeAsReadableText() {

        Long userId = 1L;
        Long recipeId = 20L;

        RecipeIngredientResponse ingredient =
            new RecipeIngredientResponse(
                10L,
                "Tomate",
                new BigDecimal("2.000"),
                "PIECE"
            );

        RecipeResponse recipe =
            new RecipeResponse(
                recipeId,
                "Salade tomate",
                "SALAD",
                2,
                new BigDecimal("4.50"),
                "Couper les tomates",
                List.of(ingredient),
                Set.of("SUMMER")
            );

        when(
            recipeService.findById(
                recipeId,
                userId
            )
        ).thenReturn(recipe);

        String text =
            csvExportService.exportRecipe(
                recipeId,
                userId
            );

        assertThat(text)
            .contains("Nom = Salade tomate")
            .contains("Portions = 2")
            .contains("Coût = 4,50 €")
            .contains("Instructions = Couper les tomates")
            .contains("Ingrédients = Tomate : 2 PIECE")
            .contains("Saisons = Été");

        verify(recipeService)
            .findById(
                recipeId,
                userId
            );
    }

    /**
     * Vérifie qu'un point-virgule reste lisible dans l'export texte.
     */
    @Test
    void shouldKeepRecipeTextReadableWithSeparatorCharacters() {

        Long userId = 1L;
        Long recipeId = 20L;

        RecipeResponse recipe =
            new RecipeResponse(
                recipeId,
                "Poulet; curry",
                "MEAT",
                2,
                new BigDecimal("6.50"),
                "Cuire puis servir",
                List.of(),
                Set.of("ALL_YEAR")
            );

        when(
            recipeService.findById(
                recipeId,
                userId
            )
        ).thenReturn(recipe);

        String text =
            csvExportService.exportRecipe(
                recipeId,
                userId
            );

        assertThat(text)
            .contains("Nom = Poulet; curry")
            .contains("Coût = 6,50 €")
            .contains("Saisons = Toute l’année");

        verify(recipeService)
            .findById(
                recipeId,
                userId
            );
    }

    /**
     * Vérifie la génération d'un CSV depuis une liste de courses.
     */
    @Test
    void shouldExportShoppingListAsCsv() {

        Long userId = 1L;
        Long shoppingListId = 10L;

        ShoppingItemResponse item =
            new ShoppingItemResponse(
                20L,
                30L,
                "Tomate",
                new BigDecimal("4.000"),
                "PIECE",
                true
            );

        ShoppingListResponse shoppingList =
            new ShoppingListResponse(
                shoppingListId,
                40L,
                List.of(item)
            );

        when(
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            )
        ).thenReturn(shoppingList);

        String csv =
            csvExportService.exportShoppingList(
                shoppingListId,
                userId
            );

        assertThat(csv)
            .contains("Ingrédient;Quantité;Unité;Acheté")
            .contains("Tomate;4;PIECE;Oui");

        verify(shoppingListService)
            .findByIdAndUserId(
                shoppingListId,
                userId
            );
    }
}