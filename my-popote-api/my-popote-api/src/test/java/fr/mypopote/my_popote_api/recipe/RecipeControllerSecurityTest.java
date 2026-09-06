package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie que l'API des recettes utilise uniquement
 * l'identité de l'utilisateur authentifié.
 */
@WebMvcTest(RecipeController.class)
@Import(SecurityConfig.class)
class RecipeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @Test
    void shouldUseAuthenticatedUserIdToFindRecipes()
        throws Exception {

        when(currentUserService.getUserId(any(Jwt.class)))
            .thenReturn(42L);

        when(
            recipeService.findAllByUserId(
                42L,
                null,
                null
            )
        ).thenReturn(List.of());

        mockMvc.perform(
                get("/api/recipes")
                    .with(jwt().jwt(jwt ->
                        jwt.subject("42")
                    ))
            )
            .andExpect(status().isOk());

        verify(recipeService)
            .findAllByUserId(
                42L,
                null,
                null
            );
    }
}