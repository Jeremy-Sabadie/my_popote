package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une recette créée par un utilisateur.
 *
 * Une recette appartient toujours à un utilisateur afin de garantir
 * l'isolation des données entre les différents comptes.
 */
@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Utilisateur propriétaire de la recette.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer servings;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    /**
     * Ingrédients et quantités nécessaires à la recette.
     *
     * orphanRemoval permet de supprimer automatiquement une ancienne
     * association lorsqu'une recette est modifiée.
     */
    @OneToMany(
        mappedBy = "recipe",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    /**
     * Saisons pendant lesquelles la recette est pertinente.
     */
    @OneToMany(
        mappedBy = "recipe",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<RecipeSeason> seasons = new ArrayList<>();

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime updatedAt;

    protected Recipe() {
        // Constructeur requis par JPA
    }

    public Recipe(
        User user,
        String name,
        String category,
        Integer servings,
        BigDecimal estimatedCost,
        String instructions
    ) {
        this.user = user;
        this.name = name;
        this.category = category;
        this.servings = servings;
        this.estimatedCost = estimatedCost;
        this.instructions = instructions;
    }

    /**
     * Ajoute un ingrédient en maintenant la relation avec la recette.
     */
    public void addIngredient(
        Ingredient ingredient,
        BigDecimal quantity,
        String unit
    ) {
        ingredients.add(
            new RecipeIngredient(this, ingredient, quantity, unit)
        );
    }

    /**
     * Ajoute une saison à la recette.
     */
    public void addSeason(String season) {
        seasons.add(new RecipeSeason(this, season));
    }

    /**
     * Vide les ingrédients avant reconstruction lors d'une modification.
     */
    public void clearIngredients() {
        ingredients.clear();
    }

    /**
     * Vide les saisons avant reconstruction lors d'une modification.
     */
    public void clearSeasons() {
        seasons.clear();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public List<RecipeSeason> getSeasons() {
        return seasons;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}