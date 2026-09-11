package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.auth.dto.AuthResponse;
import fr.mypopote.my_popote_api.auth.dto.ForgotPasswordRequest;
import fr.mypopote.my_popote_api.auth.dto.LoginRequest;
import fr.mypopote.my_popote_api.auth.dto.MessageResponse;
import fr.mypopote.my_popote_api.auth.dto.RegisterRequest;
import fr.mypopote.my_popote_api.auth.dto.ResetPasswordRequest;
import fr.mypopote.my_popote_api.auth.passwordreset.PasswordResetService;
import fr.mypopote.my_popote_api.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints HTTP responsables de l'inscription,
 * de la connexion et de la récupération du compte.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String RESET_REQUEST_MESSAGE =
        "If an account exists for this email, a reset email has been sent.";

    private static final String RESET_SUCCESS_MESSAGE =
        "Password successfully reset.";

    private final AuthService authService;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    public AuthController(
        AuthService authService,
        JwtService jwtService,
        PasswordResetService passwordResetService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
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
     * Vérifie les identifiants puis génère un JWT d'accès.
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
     * Lance une demande de réinitialisation.
     *
     * La réponse est volontairement identique que le compte
     * existe ou non afin d'empêcher l'énumération des utilisateurs.
     */
    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(
        @Valid
        @RequestBody
        ForgotPasswordRequest request
    ) {
        passwordResetService.requestPasswordReset(
            request.email()
        );

        return new MessageResponse(
            RESET_REQUEST_MESSAGE
        );
    }

    /**
     * Remplace le mot de passe lorsqu'un token valide
     * et encore utilisable est fourni.
     */
    @PostMapping("/reset-password")
    public MessageResponse resetPassword(
        @Valid
        @RequestBody
        ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(
            request.token(),
            request.password()
        );

        return new MessageResponse(
            RESET_SUCCESS_MESSAGE
        );
    }

    /**
     * Centralise la construction de la réponse d'authentification.
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