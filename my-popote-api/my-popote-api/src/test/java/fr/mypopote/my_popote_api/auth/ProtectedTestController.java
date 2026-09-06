package fr.mypopote.my_popote_api.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur utilisé uniquement pour tester
 * la protection JWT des endpoints HTTP.
 */
@RestController
public class ProtectedTestController {

    @GetMapping("/api/test/protected")
    public String protectedEndpoint() {
        return "protected";
    }
}