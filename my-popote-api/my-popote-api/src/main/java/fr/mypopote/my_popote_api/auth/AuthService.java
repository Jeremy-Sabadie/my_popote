package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé de l'inscription et de la vérification
 * des identifiants utilisateur.
 *
 * Toute la logique liée aux mots de passe reste ici,
 * et non dans le contrôleur HTTP.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crée un nouvel utilisateur après vérification de l'adresse email.
     *
     * Le mot de passe brut est encodé avant la création de l'entité :
     * il ne doit jamais être enregistré directement en base.
     */
    @Transactional
    public User register(
        String email,
        String password,
        String firstName
    ) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException(
                "Email already in use"
            );
        }

        String passwordHash =
            passwordEncoder.encode(password);

        User user = new User(
            email,
            passwordHash,
            firstName
        );

        return userRepository.save(user);
    }

    /**
     * Vérifie les identifiants fournis par l'utilisateur.
     *
     * La même erreur est utilisée pour un email inconnu
     * et pour un mauvais mot de passe afin de ne pas révéler
     * l'existence d'un compte.
     */
    public User authenticate(
        String email,
        String password
    ) {
        User user = userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Invalid credentials"
                )
            );

        boolean passwordMatches =
            passwordEncoder.matches(
                password,
                user.getPasswordHash()
            );

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                "Invalid credentials"
            );
        }

        return user;
    }
}