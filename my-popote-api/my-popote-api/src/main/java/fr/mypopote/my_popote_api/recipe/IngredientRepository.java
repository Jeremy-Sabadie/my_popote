package fr.mypopote.my_popote_api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository d'accès aux ingrédients.
 */
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByNameIgnoreCase(String name);
}