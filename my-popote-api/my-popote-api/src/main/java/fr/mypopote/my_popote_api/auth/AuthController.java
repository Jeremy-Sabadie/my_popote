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
 * Endpoints HTTP responsables de l'inscription
 * et de la connexion des utilisateurs.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
        AuthService authService,
        JwtService jwtService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Crée un nouvel utilisateur puis lui fournit
     * immédiatement un JWT utilisable par le frontend.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
        @Valid
        @RequestBody
        RegisterRequest request
    ) {
        User user = authService.register(
            request.email(),
            request.password(),
            request.firstName()
        );

        return buildAuthResponse(user);
    }

    /**
     * Vérifie les identifiants de l'utilisateur
     * puis génère un nouveau JWT d'accès.
     */
    @PostMapping("/login")
    public AuthResponse login(
        @Valid
        @RequestBody
        LoginRequest request
    ) {
        User user = authService.authenticate(
            request.email(),
            request.password()
        );

        return buildAuthResponse(user);
    }

    /**
     * Centralise la construction de la réponse
     * pour éviter de dupliquer la génération du token.
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(
            user.getId(),
            user.getEmail()
        );

        return new AuthResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            accessToken
        );
    }
}