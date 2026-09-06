package fr.mypopote.my_popote_api.user;

import fr.mypopote.my_popote_api.user.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST dédiée aux utilisateurs.
 *
 * Le contrôleur délègue la logique métier au UserService
 * et n'expose jamais directement l'entité JPA.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Injection par constructeur pour garder la dépendance explicite
     * et faciliter les tests unitaires.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retourne un utilisateur à partir de son identifiant.
     *
     * Le DTO évite notamment d'exposer le hash du mot de passe.
     */
    @GetMapping("/{userId}")
    public UserResponse findById(
        @PathVariable Long userId
    ) {
        User user = userService.findById(userId);

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName()
        );
    }
}