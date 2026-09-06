package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.recipe.Recipe;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Représente un repas précis dans la planification hebdomadaire.
 *
 * Un repas correspond à une recette placée sur une date
 * et un créneau donné, par exemple déjeuner ou dîner.
 */
@Entity
@Table(name = "planned_meal")
public class PlannedMeal {

    /**
     * Identifiant technique généré automatiquement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Planification hebdomadaire à laquelle appartient ce repas.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    /**
     * Recette prévue pour ce repas.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Date à laquelle le repas est prévu.
     */
    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    /**
     * Type de repas, par exemple LUNCH ou DINNER.
     *
     * Nous le gardons pour l'instant sous forme de String
     * afin de rester alignés avec le schéma Flyway V1.
     */
    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    /**
     * Date de création générée par MariaDB.
     */
    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    protected PlannedMeal() {
        // Constructeur requis par JPA
    }

    public PlannedMeal(
        MealPlan mealPlan,
        Recipe recipe,
        LocalDate mealDate,
        String mealType
    ) {
        this.mealPlan = mealPlan;
        this.recipe = recipe;
        this.mealDate = mealDate;
        this.mealType = mealType;
    }

    public Long getId() {
        return id;
    }

    public MealPlan getMealPlan() {
        return mealPlan;
    }

    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}