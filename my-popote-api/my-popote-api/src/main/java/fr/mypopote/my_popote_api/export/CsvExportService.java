package fr.mypopote.my_popote_api.export;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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
 * Génère les exports texte de l'application.
 *
 * Les recettes sont volontairement exportées sous forme de fiches
 * lisibles afin que le fichier puisse être consulté facilement
 * sans nécessiter l'ouverture dans un tableur.
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
     * Chaque recette est présentée sous forme d'une fiche lisible.
     * Une ligne vide sépare deux recettes.
     */
    public String exportRecipes(Long userId) {

        List<RecipeResponse> recipes =
            recipeService.findAllByUserId(userId, null, null);

        StringBuilder export = new StringBuilder();

        for (int index = 0; index < recipes.size(); index++) {

            RecipeResponse recipe = recipes.get(index);

            String ingredients = recipe.ingredients()
                .stream()
                .map(this::formatIngredient)
                .collect(Collectors.joining(", "));

            String seasons = recipe.seasons()
                .stream()
                .map(this::formatSeason)
                .sorted()
                .collect(Collectors.joining(", "));

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

            /*
             * Une ligne vide sépare visuellement les recettes,
             * sauf après la dernière fiche.
             */
            if (index < recipes.size() - 1) {
                export.append(LINE_BREAK);
            }
        }

        return export.toString();
    }

    /**
     * Exporte une liste de courses appartenant à l'utilisateur connecté.
     *
     * Pour le moment cet export reste au format CSV tabulaire,
     * adapté à une liste de courses.
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
     *
     * Exemples :
     * 10.000 -> 10
     * 2.500  -> 2,5
     * 0.250  -> 0,25
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
     *
     * Exemple :
     * 5.00 -> 5,00 €
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
     *
     * Les retours à la ligne sont remplacés par des espaces afin
     * qu'un champ reste sur une seule ligne dans la fiche.
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