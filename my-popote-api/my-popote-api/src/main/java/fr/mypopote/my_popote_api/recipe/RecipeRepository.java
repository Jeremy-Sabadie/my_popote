package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository d'accès aux recettes.
 *
 * Les recherches sont volontairement limitées par utilisateur
 * afin de respecter l'isolation des données entre comptes.
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * Retourne toutes les recettes appartenant à un utilisateur donné.
     *
     * Spring Data JPA génère automatiquement la requête à partir
     * du nom de la méthode.
     */
    List<Recipe> findAllByUserId(Long userId);
}