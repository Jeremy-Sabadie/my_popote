package fr.mypopote.my_popote_api.planning;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé des repas positionnés dans un planning.
 *
 * Les règles de contrôle du créneau, du week-end et de propriété
 * des recettes seront ajoutées progressivement par TDD.
 */
@Service
public class PlannedMealService {

    private final PlannedMealRepository plannedMealRepository;

    public PlannedMealService(
        PlannedMealRepository plannedMealRepository
    ) {
        this.plannedMealRepository = plannedMealRepository;
    }

    /**
     * Enregistre un repas dans le planning.
     *
     * La transaction garantit que l'opération d'écriture est exécutée
     * dans un contexte transactionnel Spring.
     */
    @Transactional
    public PlannedMeal save(PlannedMeal plannedMeal) {
        return plannedMealRepository.save(plannedMeal);
    }
}