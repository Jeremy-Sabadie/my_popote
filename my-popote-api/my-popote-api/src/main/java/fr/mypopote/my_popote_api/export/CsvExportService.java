package fr.mypopote.my_popote_api.export;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.mypopote.my_popote_api.recipe.RecipeService;
import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientResponse;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.shopping.ShoppingListService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Génère les exports de l'application.
 *
 * Une recette est exportée sous forme de fiche texte lisible.
 * La liste de courses reste au format CSV tabulaire.
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
     * Exporte une recette précise appartenant à l'utilisateur connecté.
     *
     * Le service RecipeService vérifie que la recette appartient bien
     * à l'utilisateur authentifié avant de renvoyer ses données.
     */
    public String exportRecipe(
            Long recipeId,
            Long userId) {

        RecipeResponse recipe =
            recipeService.findById(
                recipeId,
                userId
            );

        return formatRecipe(recipe);
    }

    /**
     * Construit la fiche texte lisible d'une recette.
     */
    private String formatRecipe(RecipeResponse recipe) {

        String ingredients = recipe.ingredients()
            .stream()
            .map(this::formatIngredient)
            .collect(Collectors.joining(", "));

        String seasons = recipe.seasons()
            .stream()
            .map(this::formatSeason)
            .sorted()
            .collect(Collectors.joining(", "));

        StringBuilder export = new StringBuilder();

        export.append("Nom = ")
            .append(safeText(recipe.name()))
            .append(LINE_BREAK);

        export.append("Portions = ")
            .append(
                recipe.servings() == null
                    ? ""
                    : recipe.servings()
            )
            .append(LINE_BREAK);

        export.append("Coût = ")
            .append(formatPrice(recipe.estimatedCost()))
            .append(LINE_BREAK);

        export.append("Instructions = ")
            .append(safeText(recipe.instructions()))
            .append(LINE_BREAK);

        export.append("Ingrédients = ")
            .append(safeText(ingredients))
            .append(LINE_BREAK);

        export.append("Saisons = ")
            .append(safeText(seasons))
            .append(LINE_BREAK);

        return export.toString();
    }

    /**
     * Exporte une liste de courses appartenant à l'utilisateur connecté.
     */
    public String exportShoppingList(
            Long shoppingListId,
            Long userId) {

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
                .append(formatQuantity(item.quantity()))
                .append(SEPARATOR)
                .append(escape(item.unit()))
                .append(SEPARATOR)
                .append(item.checked() ? "Oui" : "Non")
                .append(LINE_BREAK);
        }

        return csv.toString();
    }

    /**
     * Transforme un ingrédient en texte directement compréhensible.
     *
     * Exemple :
     * Riz : 200 g
     */
    private String formatIngredient(
            RecipeIngredientResponse ingredient) {

        String quantity =
            formatQuantity(ingredient.quantity());

        StringBuilder formatted = new StringBuilder();

        formatted.append(ingredient.ingredientName());

        if (!quantity.isBlank()) {
            formatted.append(" : ")
                .append(quantity);
        }

        if (
            ingredient.unit() != null
            && !ingredient.unit().isBlank()
        ) {
            formatted.append(" ")
                .append(ingredient.unit());
        }

        return formatted.toString();
    }

    /**
     * Retire les décimales inutiles et utilise la virgule française.
     */
    private String formatQuantity(BigDecimal quantity) {

        if (quantity == null) {
            return "";
        }

        BigDecimal normalized =
            quantity.stripTrailingZeros();

        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }

        return normalized
            .toPlainString()
            .replace('.', ',');
    }

    /**
     * Affiche toujours le prix avec deux décimales.
     */
    private String formatPrice(BigDecimal price) {

        if (price == null) {
            return "";
        }

        return price
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
            .replace('.', ',')
            + " €";
    }

    /**
     * Traduit les valeurs techniques de saison utilisées par l'API.
     */
    private String formatSeason(String season) {

        if (season == null || season.isBlank()) {
            return "";
        }

        return switch (season.toUpperCase(Locale.ROOT)) {
            case "SPRING" -> "Printemps";
            case "SUMMER" -> "Été";
            case "AUTUMN" -> "Automne";
            case "WINTER" -> "Hiver";
            case "ALL_YEAR" -> "Toute l’année";
            default -> season;
        };
    }

    /**
     * Nettoie le texte destiné à l'export lisible.
     */
    private String safeText(String value) {

        if (value == null) {
            return "";
        }

        return value
            .replace("\r", " ")
            .replace("\n", " ")
            .trim();
    }

    /**
     * Échappe les valeurs de l'export CSV de la liste de courses.
     */
    private String escape(String value) {

        if (value == null) {
            return "";
        }

        String safeValue =
            protectAgainstFormulaInjection(value);

        boolean mustBeQuoted =
            safeValue.contains(SEPARATOR)
            || safeValue.contains("\"")
            || safeValue.contains("\n")
            || safeValue.contains("\r");

        if (!mustBeQuoted) {
            return safeValue;
        }

        return "\""
            + safeValue.replace("\"", "\"\"")
            + "\"";
    }

    /**
     * Empêche un tableur d'interpréter une donnée utilisateur
     * comme une formule lors de l'ouverture du CSV.
     */
    private String protectAgainstFormulaInjection(
            String value) {

        String trimmed = value.stripLeading();

        if (trimmed.isEmpty()) {
            return value;
        }

        char firstCharacter = trimmed.charAt(0);

        if (
            firstCharacter == '='
            || firstCharacter == '+'
            || firstCharacter == '-'
            || firstCharacter == '@'
        ) {
            return "'" + value;
        }

        return value;
    }
}