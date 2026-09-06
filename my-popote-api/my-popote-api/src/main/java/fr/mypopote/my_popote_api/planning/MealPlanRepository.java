package fr.mypopote.my_popote_api.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository d'accès aux plannings hebdomadaires.
 */
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByUserIdAndWeekStartDate(
        Long userId,
        LocalDate weekStartDate
    );
}