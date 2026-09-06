package fr.mypopote.my_popote_api.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository d'accès aux utilisateurs.
 *
 * Les opérations CRUD standards sont fournies par Spring Data JPA.
 * La recherche par email est utilisée pour l'inscription
 * et l'authentification.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur sans tenir compte de la casse de l'email.
     *
     * Cela évite par exemple de considérer deux comptes différents
     * "Jeremy@example.com" et "jeremy@example.com".
     */
    Optional<User> findByEmailIgnoreCase(String email);
}