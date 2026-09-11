package fr.mypopote.my_popote_api.auth.passwordreset;

import fr.mypopote.my_popote_api.user.User;

/**
 * Abstraction chargée de transmettre le lien
 * de réinitialisation à l'utilisateur.
 *
 * Le service métier ne dépend ainsi d'aucun fournisseur
 * d'e-mail particulier.
 */
public interface PasswordResetNotifier {

    /**
     * Transmet le token brut uniquement au canal chargé
     * de l'envoyer à son propriétaire.
     *
     * Le token ne doit jamais être persisté ou écrit dans les logs.
     */
    void sendPasswordReset(
        User user,
        String rawToken
    );
}