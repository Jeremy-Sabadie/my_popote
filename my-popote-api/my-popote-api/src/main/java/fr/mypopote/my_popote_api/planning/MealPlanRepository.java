package fr.mypopote.my_popote_api.planning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository d'accès aux plannings hebdomadaires.
 *
 * Les recherches sont limitées par utilisateur afin
 * de garantir l'isolation des données entre comptes.
 */
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByUserIdAndWeekStartDate(
        Long userId,
        LocalDate weekStartDate
    );

    /**
     * Recherche un planning uniquement s'il appartient
     * à l'utilisateur authentifié.
     */
    Optional<MealPlan> findByIdAndUserId(
        Long id,
        Long userId
    );
}