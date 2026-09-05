package fr.mypopote.my_popote_api.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Représente un ingrédient utilisable dans les recettes.
 *
 * Dans la première version de My Popote, les ingrédients constituent
 * un référentiel commun et ne sont pas directement rattachés à un utilisateur.
 */
@Entity
@Table(name = "ingredient")
public class Ingredient {

    /**
     * Identifiant technique généré automatiquement par MariaDB.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom unique de l'ingrédient.
     *
     * L'unicité permet notamment de regrouper correctement les ingrédients
     * lors de la génération de la liste de courses.
     */
    @Column(nullable = false, unique = true, length = 150)
    private String name;

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

    protected Ingredient() {
        // Constructeur requis par JPA
    }

    public Ingredient(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}