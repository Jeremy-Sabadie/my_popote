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
 * Les ingrédients constituent un référentiel commun
 * partagé entre les utilisateurs.
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
     */
    @Column(nullable = false, unique = true, length = 150)
    private String name;

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