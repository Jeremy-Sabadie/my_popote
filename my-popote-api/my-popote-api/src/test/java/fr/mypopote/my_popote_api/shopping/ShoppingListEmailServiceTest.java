package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.planning.MealPlan;
import fr.mypopote.my_popote_api.planning.MealPlanRepository;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShoppingListEmailServiceTest {

    @Test
    void sendsOnlyItemsToBuyWithWeekDatesToChosenRecipient() {
        ShoppingListService shoppingListService = mock(ShoppingListService.class);
        MealPlanRepository mealPlanRepository = mock(MealPlanRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);

        ShoppingItemResponse milk = new ShoppingItemResponse(
            1L, 10L, "Lait", new BigDecimal("2"), "L",
            false, false, false
        );

        ShoppingItemResponse flour = new ShoppingItemResponse(
            2L, 11L, "Farine", new BigDecimal("500"), "g",
            false, true, false
        );

        when(shoppingListService.findByIdAndUserId(42L, 7L))
            .thenReturn(new ShoppingListResponse(
                42L, 5L, List.of(milk), List.of(flour)
            ));

        MealPlan mealPlan = mock(MealPlan.class);
        when(mealPlan.getWeekStartDate())
            .thenReturn(LocalDate.of(2026, 9, 21));
        when(mealPlan.isIncludeWeekend()).thenReturn(true);

        when(mealPlanRepository.findByIdAndUserId(5L, 7L))
            .thenReturn(Optional.of(mealPlan));

        ShoppingListEmailService service = new ShoppingListEmailService(
            shoppingListService,
            mealPlanRepository,
            mailSender,
            "contact@example.com"
        );

        service.send(42L, 7L, "destinataire@example.com");

        ArgumentCaptor<SimpleMailMessage> captor =
            ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("contact@example.com", message.getFrom());
        assertEquals("destinataire@example.com", message.getTo()[0]);
        assertEquals("My Popote - Liste de courses", message.getSubject());
        assertTrue(message.getText().contains(
            "Voici votre liste de courses pour la semaine du 21 au 27 septembre 2026 :"
        ));
        assertTrue(message.getText().contains("Lait"));
        assertTrue(message.getText().contains("2 L"));
        assertFalse(message.getText().contains("Farine"));
    }

    @Test
    void endsWeekOnFridayWhenWeekendIsExcluded() {
        ShoppingListService shoppingListService = mock(ShoppingListService.class);
        MealPlanRepository mealPlanRepository = mock(MealPlanRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);

        when(shoppingListService.findByIdAndUserId(42L, 7L))
            .thenReturn(new ShoppingListResponse(
                42L, 5L, List.of()
            ));

        MealPlan mealPlan = mock(MealPlan.class);
        when(mealPlan.getWeekStartDate())
            .thenReturn(LocalDate.of(2026, 9, 21));
        when(mealPlan.isIncludeWeekend()).thenReturn(false);

        when(mealPlanRepository.findByIdAndUserId(5L, 7L))
            .thenReturn(Optional.of(mealPlan));

        ShoppingListEmailService service = new ShoppingListEmailService(
            shoppingListService,
            mealPlanRepository,
            mailSender,
            "contact@example.com"
        );

        service.send(42L, 7L, "destinataire@example.com");

        ArgumentCaptor<SimpleMailMessage> captor =
            ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        assertTrue(captor.getValue().getText().contains(
            "du 21 au 25 septembre 2026"
        ));
    }

    @Test
    void doesNotSendWhenShoppingListDoesNotBelongToUser() {
        ShoppingListService shoppingListService = mock(ShoppingListService.class);
        MealPlanRepository mealPlanRepository = mock(MealPlanRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);

        when(shoppingListService.findByIdAndUserId(42L, 7L))
            .thenThrow(new IllegalArgumentException("Liste introuvable"));

        ShoppingListEmailService service = new ShoppingListEmailService(
            shoppingListService,
            mealPlanRepository,
            mailSender,
            "contact@example.com"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.send(42L, 7L, "destinataire@example.com")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void doesNotSendWhenMealPlanDoesNotBelongToUser() {
        ShoppingListService shoppingListService = mock(ShoppingListService.class);
        MealPlanRepository mealPlanRepository = mock(MealPlanRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);

        when(shoppingListService.findByIdAndUserId(42L, 7L))
            .thenReturn(new ShoppingListResponse(
                42L, 5L, List.of()
            ));

        when(mealPlanRepository.findByIdAndUserId(5L, 7L))
            .thenReturn(Optional.empty());

        ShoppingListEmailService service = new ShoppingListEmailService(
            shoppingListService,
            mealPlanRepository,
            mailSender,
            "contact@example.com"
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.send(42L, 7L, "destinataire@example.com")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}