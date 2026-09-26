package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    private static final String INVALID_CREDENTIALS =
        "Invalid credentials";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Hash BCrypt factice utilisé lorsqu'un email n'existe pas.
     *
     * Cela permet d'effectuer malgré tout une comparaison BCrypt
     * et réduit les différences de temps de réponse entre :
     * - un email inconnu ;
     * - un mot de passe incorrect.
     */
    private final String dummyPasswordHash;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        this.dummyPasswordHash =
            passwordEncoder.encode(
                "my-popote-security-dummy-password"
            );
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
     * et pour un mauvais mot de passe.
     *
     * Une comparaison BCrypt est également effectuée lorsque
     * l'adresse email n'existe pas afin de limiter les attaques
     * d'énumération basées sur le temps de réponse.
     */
    public User authenticate(
        String email,
        String password
    ) {
        Optional<User> optionalUser =
            userRepository.findByEmailIgnoreCase(email);

        String passwordHash =
            optionalUser
                .map(User::getPasswordHash)
                .orElse(dummyPasswordHash);

        boolean passwordMatches =
            passwordEncoder.matches(
                password,
                passwordHash
            );

        if (optionalUser.isEmpty() || !passwordMatches) {
            throw new IllegalArgumentException(
                INVALID_CREDENTIALS
            );
        }

        return optionalUser.get();
    }
}