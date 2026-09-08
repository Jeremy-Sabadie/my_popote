package fr.mypopote.my_popote_api.recipe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests du repository des tags avec une vraie instance MariaDB temporaire.
 */
@DataJpaTest
@Testcontainers
class TagRepositoryTest {

    @Container
    @ServiceConnection
    static final MariaDBContainer<?> mariadb =
        new MariaDBContainer<>("mariadb:11.8");

    @Autowired
    private TagRepository tagRepository;

    @Test
    void shouldFindTagByNameIgnoringCase() {

        Tag tag = tagRepository.save(
            new Tag(
                "Batch cooking",
                "PRACTICAL"
            )
        );

        Optional<Tag> result =
            tagRepository.findByNameIgnoreCase("batch cooking");

        assertThat(result)
            .isPresent()
            .contains(tag);
    }

    @Test
    void shouldPersistTagGroup() {

        Tag tag = tagRepository.save(
            new Tag(
                "Sans gluten",
                "DIET"
            )
        );

        Tag savedTag =
            tagRepository.findById(tag.getId())
                .orElseThrow();

        assertThat(savedTag.getName())
            .isEqualTo("Sans gluten");

        assertThat(savedTag.getGroupName())
            .isEqualTo("DIET");
    }
}