package fr.mypopote.my_popote_api.export;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.mypopote.my_popote_api.recipe.RecipeService;
import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientResponse;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.shopping.ShoppingListService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Génère les exports CSV de l'application.
 *
 * Le service utilise les services métier existants afin de conserver
 * les contrôles d'accès et l'isolation des données par utilisateur.
 */
@Service
public class CsvExportService {

    private static final String SEPARATOR = ";";
    private static final String LINE_BREAK = "\r\n";

    private final RecipeService recipeService;
    private final ShoppingListService shoppingListService;

    public CsvExportService(
            RecipeService recipeService,
            ShoppingListService shoppingListService) {

        this.recipeService = recipeService;
        this.shoppingListService = shoppingListService;
    }

    /**
     * Exporte toutes les recettes appartenant à l'utilisateur connecté.
     *
     * Une recette correspond à une ligne du fichier CSV.
     */
    public String exportRecipes(Long userId) {

        List<RecipeResponse> recipes =
            recipeService.findAllByUserId(userId, null, null);

        StringBuilder csv = new StringBuilder();

        csv.append(
            "Nom;Portions;Coût estimé;Instructions;Ingrédients;Saisons"
        );
        csv.append(LINE_BREAK);

        for (RecipeResponse recipe : recipes) {

            // Les ingrédients d'une recette sont regroupés
            // dans une seule colonne pour garder une ligne par recette.
            String ingredients = recipe.ingredients()
                .stream()
                .map(this::formatIngredient)
                .collect(Collectors.joining(", "));

            String seasons = recipe.seasons()
                .stream()
                .sorted()
                .collect(Collectors.joining(", "));

            csv.append(escape(recipe.name()))
                .append(SEPARATOR)
                .append(
                    recipe.servings() == null
                        ? ""
                        : recipe.servings()
                )
                .append(SEPARATOR)
                .append(
                    recipe.estimatedCost() == null
                        ? ""
                        : recipe.estimatedCost().toPlainString()
                )
                .append(SEPARATOR)
                .append(escape(recipe.instructions()))
                .append(SEPARATOR)
                .append(escape(ingredients))
                .append(SEPARATOR)
                .append(escape(seasons))
                .append(LINE_BREAK);
        }

        return csv.toString();
    }

    /**
     * Exporte une liste de courses appartenant à l'utilisateur connecté.
     *
     * Chaque article de courses correspond à une ligne du fichier.
     */
    public String exportShoppingList(
            Long shoppingListId,
            Long userId) {

        // Le service ShoppingListService contrôle déjà que la liste
        // appartient bien à l'utilisateur authentifié.
        ShoppingListResponse shoppingList =
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            );

        StringBuilder csv = new StringBuilder();

        csv.append("Ingrédient;Quantité;Unité;Acheté");
        csv.append(LINE_BREAK);

        for (ShoppingItemResponse item : shoppingList.items()) {

            csv.append(escape(item.ingredientName()))
                .append(SEPARATOR)
                .append(
                    item.quantity() == null
                        ? ""
                        : item.quantity().toPlainString()
                )
                .append(SEPARATOR)
                .append(escape(item.unit()))
                .append(SEPARATOR)
                .append(item.checked() ? "Oui" : "Non")
                .append(LINE_BREAK);
        }

        return csv.toString();
    }

    /**
     * Transforme un ingrédient de recette en texte compact.
     *
     * Exemple : Tomate 2.000 PIECE
     */
    private String formatIngredient(
            RecipeIngredientResponse ingredient) {

        String quantity =
            ingredient.quantity() == null
                ? ""
                : ingredient.quantity().toPlainString();

        return ingredient.ingredientName()
            + " "
            + quantity
            + " "
            + ingredient.unit();
    }

    /**
     * Échappe les valeurs pouvant casser la structure du CSV.
     *
     * Une valeur contenant un point-virgule, un guillemet ou un retour
     * à la ligne est entourée de guillemets. Les guillemets déjà présents
     * dans la valeur sont doublés.
     */
    private String escape(String value) {

        if (value == null) {
            return "";
        }

        boolean mustBeQuoted =
            value.contains(SEPARATOR)
            || value.contains("\"")
            || value.contains("\n")
            || value.contains("\r");

        if (!mustBeQuoted) {
            return value;
        }

        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }
}