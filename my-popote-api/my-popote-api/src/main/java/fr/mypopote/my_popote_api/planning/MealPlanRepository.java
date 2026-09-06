package fr.mypopote.my_popote_api.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository d'accès aux plannings hebdomadaires.
 *
 * Les recherches fonctionnelles sont toujours limitées
 * à l'utilisateur propriétaire.
 */
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByUserIdAndWeekStartDate(
        Long userId,
        LocalDate weekStartDate
    );

    Optional<MealPlan> findByIdAndUserId(
        Long id,
        Long userId
    );

    /**
     * Historique du plus récent au plus ancien.
     */
    List<MealPlan> findAllByUserIdOrderByWeekStartDateDesc(
        Long userId
    );
}