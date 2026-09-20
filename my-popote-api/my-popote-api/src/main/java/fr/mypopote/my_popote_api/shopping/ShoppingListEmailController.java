package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.shopping.dto.SendShoppingListEmailRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint d'envoi d'une liste de courses par e-mail.
 */
@RestController
@RequestMapping("/api/shopping-lists")
public class ShoppingListEmailController {

    private final ShoppingListEmailService emailService;
    private final CurrentUserService currentUserService;

    public ShoppingListEmailController(
        ShoppingListEmailService emailService,
        CurrentUserService currentUserService
    ) {
        this.emailService = emailService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/{shoppingListId}/email")
    public ResponseEntity<Void> sendEmail(
        @PathVariable Long shoppingListId,
        @Valid @RequestBody SendShoppingListEmailRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserService.getUserId(jwt);

        emailService.send(
            shoppingListId,
            userId,
            request.recipient()
        );

        return ResponseEntity.noContent().build();
    }
}