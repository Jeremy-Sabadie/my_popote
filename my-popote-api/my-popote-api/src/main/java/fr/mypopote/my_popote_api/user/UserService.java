package fr.mypopote.my_popote_api.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé des opérations métier liées aux utilisateurs.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retourne l'utilisateur actuellement authentifié.
     *
     * Le userId doit provenir du JWT et non d'une valeur
     * fournie directement par le client.
     */
    public User findCurrentUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );
    }
}