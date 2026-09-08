package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.TagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de consultation du référentiel des tags.
 *
 * Les tags sont partagés entre les utilisateurs et sont uniquement
 * consultés ici. Leur administration pourra être ajoutée plus tard
 * si le besoin apparaît.
 */
@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Retourne tous les tags dans un ordre stable pour le frontend.
     */
    public List<TagResponse> findAll() {
        return tagRepository
            .findAllByOrderByGroupNameAscNameAsc()
            .stream()
            .map(tag ->
                new TagResponse(
                    tag.getId(),
                    tag.getName(),
                    tag.getGroupName()
                )
            )
            .toList();
    }
}