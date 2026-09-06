package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.auth.dto.AuthResponse;
import fr.mypopote.my_popote_api.auth.dto.LoginRequest;
import fr.mypopote.my_popote_api.auth.dto.RegisterRequest;
import fr.mypopote.my_popote_api.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints publics liés à l'authentification.
 *
 * Le contrôleur gère uniquement la couche HTTP :
 * - validation des données reçues ;
 * - délégation de la logique métier à AuthService ;
 * - transformation des résultats en DTO publics.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Crée un nouveau compte utilisateur.
     *
     * @Valid déclenche automatiquement Bean Validation.
     * Une requête incorrecte est donc rejetée avec HTTP 400
     * avant même d'atteindre AuthService.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
        @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(
            request.email(),
            request.password(),
            request.firstName()
        );

        return toResponse(user);
    }

    /**
     * Vérifie les identifiants de connexion.
     *
     * À ce stade, nous validons les identifiants mais
     * nous ne créons pas encore de véritable authentification.
     */
    @PostMapping("/login")
    public AuthResponse login(
        @Valid @RequestBody LoginRequest request
    ) {
        User user = authService.authenticate(
            request.email(),
            request.password()
        );

        return toResponse(user);
    }

    /**
     * Transforme l'entité interne en DTO public.
     *
     * Le passwordHash n'est volontairement jamais exposé
     * dans les réponses de l'API.
     */
    private AuthResponse toResponse(User user) {
        return new AuthResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName()
        );
    }
}