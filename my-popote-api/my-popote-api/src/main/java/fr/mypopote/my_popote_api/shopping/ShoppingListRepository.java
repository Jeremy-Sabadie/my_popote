package fr.mypopote.my_popote_api.shopping;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository d'accès aux listes de courses.
 *
 * Les méthodes CRUD standards sont fournies automatiquement
 * par Spring Data JPA.
 */
public interface ShoppingListRepository
    extends JpaRepository<ShoppingList, Long> {
}