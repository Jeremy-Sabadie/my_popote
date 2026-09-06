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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration centrale de la sécurité HTTP de My Popote.
 *
 * Cette fondation protège les endpoints métier,
 * configure CORS et prépare le stockage sécurisé des mots de passe.
 *
 * L'authentification réelle sera ajoutée dans le bloc suivant.
 */
@Configuration
public class SecurityConfig {

    private final String allowedOrigins;

    /**
     * Les origines autorisées viennent de la configuration.
     *
     * En développement, Angular utilise localhost:4200.
     * En production, cette valeur sera fournie par l'environnement.
     */
    public SecurityConfig(
        @Value("${app.cors.allowed-origins:http://localhost:4200}")
        String allowedOrigins
    ) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Définit les règles de sécurité appliquées à l'API.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http
    ) throws Exception {

        http
            .cors(Customizer.withDefaults())

            /*
             * My Popote utilisera une API REST sans session serveur.
             */
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            /*
             * Cette configuration sera réévaluée si nous choisissons
             * une authentification utilisant des cookies.
             */
            .csrf(csrf -> csrf.disable())

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            /*
             * Une requête sans authentification reçoit 401.
             * Un utilisateur authentifié sans permission recevra 403.
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
     * BCrypt est utilisé pour le hash des mots de passe.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Rend la configuration CORS disponible pour Spring Security.
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
     * Construit les règles CORS de l'application.
     *
     * Plusieurs origines peuvent être fournies,
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

        /*
         * Pas de credentials cross-origin pour le moment.
         * Ce choix dépendra du mécanisme d'authentification final.
         */
        configuration.setAllowCredentials(false);

        return configuration;
    }
}