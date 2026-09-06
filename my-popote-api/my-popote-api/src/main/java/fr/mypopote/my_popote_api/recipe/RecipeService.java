package fr.mypopote.my_popote_api.recipe;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service chargé des opérations métier liées aux recettes.
 *
 * Les recettes appartiennent à un utilisateur. Les recherches
 * doivent donc toujours préserver cette isolation des données.
 */
@Service
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * Retourne uniquement les recettes appartenant à l'utilisateur demandé.
     *
     * Cette restriction doit être appliquée côté serveur :
     * le frontend ne constitue jamais une barrière de sécurité.
     */
    public List<Recipe> findAllByUserId(Long userId) {
        return recipeRepository.findAllByUserId(userId);
    }
}