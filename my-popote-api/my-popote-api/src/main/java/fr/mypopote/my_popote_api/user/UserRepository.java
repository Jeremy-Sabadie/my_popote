package fr.mypopote.my_popote_api.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository d'accès aux utilisateurs.
 *
 * Spring Data JPA fournit automatiquement les opérations CRUD standards.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}