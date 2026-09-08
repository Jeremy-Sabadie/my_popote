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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Ancienne catégorisation principale de la recette.
     *
     * Elle est conservée temporairement pendant la migration vers
     * le système de tags multiples afin de ne pas casser les usages existants.
     */
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
     *
     * La saisonnalité reste volontairement distincte des tags.
     */
    @OneToMany(
        mappedBy = "recipe",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<RecipeSeason> seasons = new ArrayList<>();

    /**
     * Caractéristiques multiples attribuées à la recette.
     *
     * La table recipe_tag ne porte aucune donnée métier supplémentaire :
     * une relation ManyToMany suffit donc pour le besoin actuel.
     *
     * Aucun cascade n'est utilisé car les tags forment un référentiel
     * partagé par toutes les recettes.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recipe_tag",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

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
     * Ajoute un tag existant du référentiel à la recette.
     */
    public void addTag(Tag tag) {
        tags.add(tag);
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

    /**
     * Retire toutes les associations de tags de la recette.
     *
     * Les tags eux-mêmes ne sont pas supprimés car ils appartiennent
     * au référentiel partagé.
     */
    public void clearTags() {
        tags.clear();
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

    public Set<Tag> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}