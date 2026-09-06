package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.recipe.Ingredient;
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
 * Représente une ligne de la liste de courses.
 *
 * Chaque élément correspond à un ingrédient agrégé
 * avec une quantité et une unité.
 */
@Entity
@Table(name = "shopping_item")
public class ShoppingItem {

    /**
     * Identifiant technique généré automatiquement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Liste de courses à laquelle appartient cet élément.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    /**
     * Ingrédient à acheter.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    /**
     * Quantité totale nécessaire pour la semaine.
     */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    /**
     * Unité associée à la quantité :
     * g, kg, ml, pièce, etc.
     */
    @Column(nullable = false, length = 30)
    private String unit;

    /**
     * Indique si l'utilisateur a déjà pris cet article
     * pendant ses courses.
     */
    @Column(name = "is_checked", nullable = false)
    private boolean checked;

    protected ShoppingItem() {
        // Constructeur requis par JPA
    }

    public ShoppingItem(
        ShoppingList shoppingList,
        Ingredient ingredient,
        BigDecimal quantity,
        String unit,
        boolean checked
    ) {
        this.shoppingList = shoppingList;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = checked;
    }

    public Long getId() {
        return id;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}