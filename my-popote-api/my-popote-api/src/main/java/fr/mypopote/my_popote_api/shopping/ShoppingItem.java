package fr.mypopote.my_popote_api.shopping;

import java.math.BigDecimal;

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

/**
 * Représente une ligne de la liste de courses.
 *
 * Une ligne peut provenir :
 * - d'une recette de la semaine ;
 * - d'un ajout manuel saisi par l'utilisateur.
 */
@Entity
@Table(name = "shopping_item")
public class ShoppingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    /**
     * Nullable car un ajout manuel n'est pas forcément
     * associé à un Ingredient de la bibliothèque.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    /**
     * Nom libre utilisé pour un article ajouté manuellement.
     */
    @Column(name = "custom_name", length = 150)
    private String customName;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, length = 30)
    private String unit;

    /**
     * Article déjà placé dans le panier pendant les courses.
     */
    @Column(name = "checked", nullable = false)
    private boolean checked;

    /**
     * Article que l'utilisateur possède déjà chez lui.
     */
    @Column(name = "already_owned", nullable = false)
    private boolean alreadyOwned;

    /**
     * Distingue les articles ajoutés à la volée
     * de ceux générés depuis les recettes.
     */
    @Column(name = "manual", nullable = false)
    private boolean manual;

    protected ShoppingItem() {
        // Constructeur requis par JPA.
    }

    /**
     * Constructeur historique pour un article issu d'une recette.
     */
    public ShoppingItem(
            ShoppingList shoppingList,
            Ingredient ingredient,
            BigDecimal quantity,
            String unit,
            boolean checked) {

        this(
            shoppingList,
            ingredient,
            null,
            quantity,
            unit,
            checked,
            false,
            false
        );
    }

    /**
     * Constructeur complet.
     */
    public ShoppingItem(
            ShoppingList shoppingList,
            Ingredient ingredient,
            String customName,
            BigDecimal quantity,
            String unit,
            boolean checked,
            boolean alreadyOwned,
            boolean manual) {

        this.shoppingList = shoppingList;
        this.ingredient = ingredient;
        this.customName = customName;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = checked;
        this.alreadyOwned = alreadyOwned;
        this.manual = manual;
    }

    /**
     * Fabrique un article ajouté manuellement.
     */
    public static ShoppingItem manual(
            ShoppingList shoppingList,
            String customName,
            BigDecimal quantity,
            String unit) {

        return new ShoppingItem(
            shoppingList,
            null,
            customName,
            quantity,
            unit,
            false,
            false,
            true
        );
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

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
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

    public boolean isAlreadyOwned() {
        return alreadyOwned;
    }

    public void setAlreadyOwned(boolean alreadyOwned) {
        this.alreadyOwned = alreadyOwned;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    /**
     * Nom présenté par l'API, quelle que soit l'origine de la ligne.
     */
    public String getDisplayName() {

        if (manual) {
            return customName;
        }

        return ingredient == null
            ? null
            : ingredient.getName();
    }
}