package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.user.User;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Représente la planification des repas d'un utilisateur pour une semaine.
 */
@Entity
@Table(name = "meal_plan")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Utilisateur propriétaire du planning.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Premier jour de la semaine.
     * My Popote utilise toujours un lundi.
     */
    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    /**
     * Indique si samedi et dimanche sont inclus.
     */
    @Column(name = "include_weekend", nullable = false)
    private boolean includeWeekend;

    @Column(name = "max_budget", precision = 10, scale = 2)
    private BigDecimal maxBudget;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

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

    protected MealPlan() {
        // Constructeur requis par JPA
    }

    public MealPlan(
        User user,
        LocalDate weekStartDate,
        boolean includeWeekend,
        BigDecimal maxBudget,
        BigDecimal estimatedCost
    ) {
        this.user = user;
        this.weekStartDate = weekStartDate;
        this.includeWeekend = includeWeekend;
        this.maxBudget = maxBudget;
        this.estimatedCost = estimatedCost;
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

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public boolean isIncludeWeekend() {
        return includeWeekend;
    }

    public void setIncludeWeekend(boolean includeWeekend) {
        this.includeWeekend = includeWeekend;
    }

    public BigDecimal getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(BigDecimal maxBudget) {
        this.maxBudget = maxBudget;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}