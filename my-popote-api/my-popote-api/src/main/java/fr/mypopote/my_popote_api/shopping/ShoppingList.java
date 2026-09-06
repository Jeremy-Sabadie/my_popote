package fr.mypopote.my_popote_api.shopping;

import java.time.LocalDateTime;

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

/**
 * Représente la liste de courses associée à une semaine planifiée.
 *
 * Une semaine possède au maximum une liste de courses.
 */
@Entity
@Table(name = "shopping_list")
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Planning à partir duquel la liste de courses est générée.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false, unique = true)
    private MealPlan mealPlan;

    // Dates gérées directement par MariaDB.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected ShoppingList() {
        // Constructeur vide requis par JPA.
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setMealPlan(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }
}