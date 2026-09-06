package fr.mypopote.my_popote_api.recipe;

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
import java.time.LocalDateTime;

/**
 * Représente une recette créée par un utilisateur.
 *
 * Une recette appartient toujours à un utilisateur afin de garantir
 * l'isolation des données entre les différents comptes.
 */
@Entity
@Table(name = "recipe")
public class Recipe {

    /**
     * Identifiant technique généré automatiquement par MariaDB.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Utilisateur propriétaire de la recette.
     *
     * Le chargement est volontairement LAZY afin de ne pas charger
     * l'utilisateur systématiquement lorsqu'une recette est récupérée.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Nom affiché de la recette.
     */
    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Catégorie principale de la recette :
     * végétarien, viande, poisson, soupe, salade, etc.
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * Nombre de portions prévues par la recette.
     */
    @Column(nullable = false)
    private Integer servings;

    /**
     * Coût estimatif de la recette.
     *
     * BigDecimal est utilisé plutôt que double pour éviter
     * les problèmes de précision avec les valeurs monétaires.
     */
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    /**
     * Instructions de préparation de la recette.
     */
    @Column(columnDefinition = "TEXT")
    private String instructions;

    /**
     * Date de création générée directement par MariaDB.
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}