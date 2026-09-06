package fr.mypopote.my_popote_api.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé des opérations métier liées aux utilisateurs.
 *
 * Les contrôleurs ne doivent pas accéder directement au repository :
 * le service constitue le point d'entrée de la logique applicative.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Injection par constructeur pour garder la dépendance explicite
     * et faciliter les tests unitaires avec Mockito.
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Recherche un utilisateur par son identifiant.
     *
     * La gestion d'exception sera affinée lorsque nous mettrons
     * en place les exceptions métier et le handler HTTP global.
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "User not found with id: " + userId
            ));
    }
}