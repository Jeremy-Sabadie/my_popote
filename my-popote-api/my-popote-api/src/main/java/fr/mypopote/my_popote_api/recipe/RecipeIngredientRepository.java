package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository de la relation entre une recette et ses ingrédients.
 */
public interface RecipeIngredientRepository
    extends JpaRepository<RecipeIngredient, Long> {
}