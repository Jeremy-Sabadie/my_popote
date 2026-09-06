package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository d'accès aux recettes.
 *
 * Toutes les recherches métier sont limitées à l'utilisateur propriétaire.
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByUserId(Long userId);

    Optional<Recipe> findByIdAndUserId(
        Long id,
        Long userId
    );

    List<Recipe> findAllByUserIdAndCategoryIgnoreCase(
        Long userId,
        String category
    );

    List<Recipe> findDistinctByUserIdAndSeasonsSeasonIgnoreCase(
        Long userId,
        String season
    );

    List<Recipe>
        findDistinctByUserIdAndCategoryIgnoreCaseAndSeasonsSeasonIgnoreCase(
            Long userId,
            String category,
            String season
        );
}