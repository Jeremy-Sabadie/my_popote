package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository d'accès aux recettes.
 *
 * Les recherches sont volontairement limitées par utilisateur
 * afin de respecter l'isolation des données entre comptes.
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * Retourne toutes les recettes appartenant à un utilisateur donné.
     */
    List<Recipe> findAllByUserId(Long userId);

    /**
     * Recherche une recette uniquement si elle appartient
     * à l'utilisateur authentifié.
     */
    Optional<Recipe> findByIdAndUserId(
        Long id,
        Long userId
    );
}