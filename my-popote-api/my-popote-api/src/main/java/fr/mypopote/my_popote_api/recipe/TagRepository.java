package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository d'accès au référentiel des tags.
 *
 * Les tags sont partagés par l'ensemble des utilisateurs :
 * contrairement aux recettes, ils ne sont pas rattachés à un compte.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Recherche un tag par son nom sans tenir compte de la casse.
     */
    Optional<Tag> findByNameIgnoreCase(String name);

    /**
     * Retourne les tags dans un ordre stable pour l'affichage
     * dans les formulaires et filtres du frontend.
     */
    List<Tag> findAllByOrderByGroupNameAscNameAsc();
}