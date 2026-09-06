package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.planning.MealPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Représente la liste de courses générée à partir
 * d'une planification hebdomadaire.
 *
 * Une planification possède au maximum une liste de courses.
 */
@Entity
@Table(name = "shopping_list")
public class ShoppingList {

    /**
     * Identifiant technique généré automatiquement.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Planification hebdomadaire utilisée pour générer la liste.
     *
     * La relation est OneToOne car une seule liste de courses
     * est associée à une planification.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false, unique = true)
    private MealPlan mealPlan;

    /**
     * Date de création gérée par MariaDB.
     */
    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification gérée par MariaDB.
     */
    @Column(
        name = "updated_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime updatedAt;

    protected ShoppingList() {
        // Constructeur requis par JPA
    }

    public ShoppingList(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}