package fr.mypopote.my_popote_api.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Fournit l'identité de l'utilisateur authentifié.
 *
 * L'identifiant vient du sujet du JWT signé par le backend.
 * On ne fait donc jamais confiance à un userId envoyé par le frontend.
 */
@Service
public class CurrentUserService {

    /**
     * Récupère l'identifiant utilisateur contenu dans le sujet du JWT.
     */
    public Long getUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}