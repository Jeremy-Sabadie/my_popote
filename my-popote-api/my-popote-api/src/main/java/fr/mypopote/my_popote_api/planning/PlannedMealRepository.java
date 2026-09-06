package fr.mypopote.my_popote_api.planning;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository d'accès aux repas planifiés.
 */
public interface PlannedMealRepository extends JpaRepository<PlannedMeal, Long> {
}