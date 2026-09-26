package fr.mypopote.my_popote_api.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Service permettant de récupérer l'identité
 * de l'utilisateur authentifié depuis son JWT.
 */
@Service
public class CurrentUserService {

    /**
     * Retourne l'identifiant utilisateur contenu
     * dans le subject du JWT.
     *
     * @param jwt JWT authentifié par Spring Security
     * @return identifiant de l'utilisateur connecté
     */
    public Long getUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}