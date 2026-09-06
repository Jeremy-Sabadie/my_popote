package fr.mypopote.my_popote_api.recipe;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé de la gestion des ingrédients.
 *
 * Les ingrédients sont partagés entre les utilisateurs.
 * Un même ingrédient doit donc être réutilisé autant que possible.
 */
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    /**
     * Recherche un ingrédient sans tenir compte de la casse.
     *
     * S'il n'existe pas encore, il est créé afin d'éviter
     * de dupliquer cette logique dans RecipeService.
     */
    @Transactional
    public Ingredient findOrCreate(String name) {
        return ingredientRepository.findByNameIgnoreCase(name)
            .orElseGet(() ->
                ingredientRepository.save(new Ingredient(name))
            );
    }
}