package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.TagResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST permettant au frontend de consulter le référentiel des tags.
 *
 * Le frontend ne contient donc pas une liste de tags codée en dur :
 * les valeurs disponibles viennent directement de la base.
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Retourne tous les tags disponibles.
     */
    @GetMapping
    public List<TagResponse> findAll() {
        return tagService.findAll();
    }
}