package fr.mypopote.my_popote_api.planning;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service chargé de la gestion des plannings hebdomadaires.
 *
 * Un utilisateur ne peut avoir qu'un planning pour une même semaine,
 * contrainte également garantie par la base de données.
 */
@Service
@Transactional(readOnly = true)
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;

    public MealPlanService(MealPlanRepository mealPlanRepository) {
        this.mealPlanRepository = mealPlanRepository;
    }

    /**
     * Recherche le planning d'un utilisateur pour une semaine donnée.
     *
     * Le couple utilisateur + semaine est utilisé afin de préserver
     * l'isolation des plannings entre les différents comptes.
     */
    public Optional<MealPlan> findByUserAndWeek(
        Long userId,
        LocalDate weekStartDate
    ) {
        return mealPlanRepository.findByUserIdAndWeekStartDate(
            userId,
            weekStartDate
        );
    }
}