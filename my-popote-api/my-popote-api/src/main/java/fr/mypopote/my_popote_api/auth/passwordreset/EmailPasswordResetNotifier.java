package fr.mypopote.my_popote_api.auth.passwordreset;

import fr.mypopote.my_popote_api.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Envoie le lien de réinitialisation du mot de passe par e-mail.
 *
 * Le token brut n'est utilisé que pour construire le lien envoyé
 * à l'utilisateur. Il n'est ni persisté ni écrit dans les logs.
 */
@Component
public class EmailPasswordResetNotifier
    implements PasswordResetNotifier {

    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String senderAddress;

    public EmailPasswordResetNotifier(
        JavaMailSender mailSender,
        @Value("${app.frontend.url}") String frontendUrl,
        @Value("${app.mail.from}") String senderAddress
    ) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.senderAddress = senderAddress;
    }

    @Override
    public void sendPasswordReset(
        User user,
        String rawToken
    ) {
        /*
         * UriComponentsBuilder encode correctement le token
         * lorsqu'il est placé dans la query string.
         */
        String resetUrl = UriComponentsBuilder
            .fromUriString(frontendUrl)
            .path("/reset-password")
            .queryParam("token", rawToken)
            .build()
            .encode()
            .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderAddress);
        message.setTo(user.getEmail());
        message.setSubject(
            "My Popote - Réinitialisation de votre mot de passe"
        );

        message.setText(
            """
            Bonjour,

            Une demande de réinitialisation du mot de passe de votre compte My Popote a été effectuée.

            Utilisez le lien suivant pour choisir un nouveau mot de passe :

            %s

            Ce lien expire dans 30 minutes et ne peut être utilisé qu'une seule fois.

            Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet e-mail.

            My Popote
            """.formatted(resetUrl)
        );

        mailSender.send(message);
    }
}