package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
class MealPlanRepositoryTest {

    @Container
    @ServiceConnection
    static final MariaDBContainer<?> mariadb =
        new MariaDBContainer<>("mariadb:11.8");

    @Autowired
    private MealPlanRepository mealPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindMealPlanByUserAndWeekStartDate() {
        User user = userRepository.save(
            new User(
                "jeremy@example.com",
                "hashed-password",
                "Jérémy"
            )
        );

        LocalDate weekStartDate = LocalDate.of(2026, 9, 7);

        mealPlanRepository.save(
            new MealPlan(
                user,
                weekStartDate,
                false,
                null,
                null
            )
        );

        Optional<MealPlan> mealPlan =
            mealPlanRepository.findByUserIdAndWeekStartDate(
                user.getId(),
                weekStartDate
            );

        assertThat(mealPlan).isPresent();
        assertThat(mealPlan.get().getWeekStartDate())
            .isEqualTo(weekStartDate);
    }
}