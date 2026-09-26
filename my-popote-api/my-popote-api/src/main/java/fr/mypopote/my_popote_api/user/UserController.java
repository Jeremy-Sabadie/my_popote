package fr.mypopote.my_popote_api.user;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.user.dto.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST dédiée à l'utilisateur connecté.
 *
 * L'identité utilisateur provient exclusivement du JWT.
 * Aucun identifiant utilisateur fourni par le client n'est utilisé.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(
        UserService userService,
        CurrentUserService currentUserService
    ) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    /**
     * Retourne les informations du compte actuellement authentifié.
     *
     * L'identifiant est extrait du JWT afin d'empêcher un utilisateur
     * de consulter le compte d'un autre utilisateur en modifiant l'URL.
     */
    @GetMapping("/me")
    public UserResponse findCurrentUser(
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserService.getUserId(jwt);

        User user = userService.findCurrentUser(userId);

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName()
        );
    }
}