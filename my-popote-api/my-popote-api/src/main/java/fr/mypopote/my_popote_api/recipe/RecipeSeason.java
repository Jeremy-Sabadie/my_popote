package fr.mypopote.my_popote_api.recipe;

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
 * Associe une recette à une saison.
 *
 * Une recette peut avoir plusieurs saisons, par exemple
 * printemps et été, ou être disponible toute l'année.
 */
@Entity
@Table(name = "recipe_season")
public class RecipeSeason {

    /**
     * Identifiant technique généré automatiquement par MariaDB.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Recette concernée par la saison.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    /**
     * Saison associée à la recette :
     * SPRING, SUMMER, AUTUMN, WINTER ou ALL_YEAR.
     *
     * Nous la gardons pour l'instant sous forme de String afin
     * de rester strictement alignés avec le schéma V1.
     */
    @Column(nullable = false, length = 20)
    private String season;

    protected RecipeSeason() {
        // Constructeur requis par JPA
    }

    public RecipeSeason(Recipe recipe, String season) {
        this.recipe = recipe;
        this.season = season;
    }

    public Long getId() {
        return id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}