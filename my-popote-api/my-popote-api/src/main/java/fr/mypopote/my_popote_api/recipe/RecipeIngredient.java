package fr.mypopote.my_popote_api.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Représente l'utilisation d'un ingrédient dans une recette.
 *
 * Cette entité porte les informations propres à la relation :
 * la quantité nécessaire et son unité.
 */
@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredient {

    /**
     * Identifiant technique généré automatiquement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Recette dans laquelle l'ingrédient est utilisé.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Ingrédient utilisé dans la recette.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * Quantité nécessaire.
     *
     * BigDecimal permet de représenter proprement des valeurs
     * comme 0.5, 1.25 ou 250.000.
     */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    /**
     * Unité associée à la quantité :
     * g, kg, ml, pièce, cuillère, etc.
     */
    @Column(nullable = false, length = 30)
    private String unit;

    protected RecipeIngredient() {
        // Constructeur requis par JPA
    }

    public RecipeIngredient(
        Recipe recipe,
        Ingredient ingredient,
        BigDecimal quantity,
        String unit
    ) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}