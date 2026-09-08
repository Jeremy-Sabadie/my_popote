package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    /**
     * Retourne uniquement les recettes possédant tous les tags demandés.
     *
     * Le filtre reste limité à l'utilisateur propriétaire.
     * Le GROUP BY avec HAVING donne une logique AND :
     * une recette doit contenir chacun des tagIds reçus.
     */
    @Query("""
        SELECT r
        FROM Recipe r
        JOIN r.tags t
        WHERE r.user.id = :userId
          AND t.id IN :tagIds
        GROUP BY r
        HAVING COUNT(DISTINCT t.id) = :tagCount
        """)
    List<Recipe> findAllByUserIdAndAllTagIds(
        @Param("userId") Long userId,
        @Param("tagIds") Set<Long> tagIds,
        @Param("tagCount") long tagCount
    );
}