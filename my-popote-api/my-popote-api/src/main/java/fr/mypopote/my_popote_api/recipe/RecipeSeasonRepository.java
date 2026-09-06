package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository des saisons associées aux recettes.
 */
public interface RecipeSeasonRepository
    extends JpaRepository<RecipeSeason, Long> {
}