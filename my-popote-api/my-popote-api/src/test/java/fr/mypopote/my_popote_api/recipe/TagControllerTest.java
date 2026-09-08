package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.TagResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur du référentiel des tags.
 */
@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    @Test
    void shouldReturnAvailableTags() {
        List<TagResponse> tags = List.of(
            new TagResponse(
                1L,
                "Sèche",
                "NUTRITION"
            ),
            new TagResponse(
                2L,
                "Rapide",
                "PRACTICAL"
            )
        );

        when(tagService.findAll())
            .thenReturn(tags);

        assertThat(tagController.findAll())
            .containsExactlyElementsOf(tags);
    }
}