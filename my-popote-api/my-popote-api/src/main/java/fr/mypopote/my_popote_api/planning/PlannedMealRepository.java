package fr.mypopote.my_popote_api.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository d'accès aux repas planifiés.
 */
public interface PlannedMealRepository
    extends JpaRepository<PlannedMeal, Long> {

    List<PlannedMeal> findAllByMealPlanIdOrderByMealDateAscMealTypeAsc(
        Long mealPlanId
    );

    /**
     * Recherche un repas uniquement s'il appartient
     * à l'utilisateur authentifié.
     */
    Optional<PlannedMeal> findByIdAndMealPlanUserId(
        Long plannedMealId,
        Long userId
    );

    List<PlannedMeal> findAllByMealPlanUserIdAndMealDate(
        Long userId,
        LocalDate mealDate
    );

    void deleteAllByMealPlanId(Long mealPlanId);
}