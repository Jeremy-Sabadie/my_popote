package fr.mypopote.my_popote_api.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration centrale de la sécurité HTTP de My Popote.
 *
 * L'API fonctionne sans session serveur :
 * chaque requête protégée doit fournir un Bearer JWT valide.
 */
@Configuration
public class SecurityConfig {

    private final String allowedOrigins;
    private final String jwtSecret;

    public SecurityConfig(
        @Value("${app.cors.allowed-origins:http://localhost:4200}")
        String allowedOrigins,

        @Value("${app.jwt.secret}")
        String jwtSecret
    ) {
        this.allowedOrigins = allowedOrigins;
        this.jwtSecret = jwtSecret;
    }

    /**
     * Définit les règles d'accès aux endpoints.
     *
     * Les endpoints d'authentification et le health check
     * restent publics. Toutes les autres routes nécessitent
     * maintenant un JWT valide.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http
    ) throws Exception {

        http
            .cors(Customizer.withDefaults())

            /*
             * Aucun état d'authentification n'est conservé
             * côté serveur entre deux requêtes.
             */
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            /*
             * Nous utilisons actuellement des Bearer tokens
             * et non une authentification par cookie.
             */
            .csrf(csrf -> csrf.disable())

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            /*
             * Spring Security utilise le JwtDecoder déclaré
             * plus bas pour valider les Bearer JWT.
             */
            .oauth2ResourceServer(resourceServer ->
                resourceServer.jwt(Customizer.withDefaults())
            )

            /*
             * Une requête non authentifiée reçoit explicitement
             * un HTTP 401 plutôt qu'une redirection HTML.
             */
            .exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(
                    (request, response, exception) ->
                        response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED
                        )
                )
            )

            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers(
                        HttpMethod.OPTIONS,
                        "/**"
                    )
                    .permitAll()

                    .requestMatchers(
                        "/actuator/health",
                        "/api/auth/**"
                    )
                    .permitAll()

                    .anyRequest()
                    .authenticated()
            );

        return http.build();
    }

    /**
     * Hash sécurisé utilisé pour les mots de passe.
     *
     * Un mot de passe utilisateur n'est jamais enregistré
     * directement en base de données.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Décode et vérifie la signature des JWT entrants.
     *
     * La même clé secrète sera utilisée par JwtService
     * pour signer les tokens créés lors de la connexion.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );

        return NimbusJwtDecoder
            .withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    /**
     * Configuration CORS utilisée par Spring Security.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
            buildCorsConfiguration();

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }

    /**
     * Construit la politique CORS de l'application.
     *
     * Plusieurs origines peuvent être fournies
     * séparées par des virgules.
     */
    CorsConfiguration buildCorsConfiguration() {
        CorsConfiguration configuration =
            new CorsConfiguration();

        List<String> origins = Arrays.stream(
                allowedOrigins.split(",")
            )
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toList();

        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        configuration.setExposedHeaders(
            List.of("Location")
        );

        configuration.setAllowCredentials(false);

        return configuration;
    }
}