package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.planning.MealPlan;
import fr.mypopote.my_popote_api.planning.MealPlanRepository;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Construit et envoie une liste de courses par e-mail.
 *
 * La liste et son planning sont recherchés pour l'utilisateur connecté
 * afin de ne pas exposer les données d'un autre utilisateur.
 */
@Service
public class ShoppingListEmailService {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    private final ShoppingListService shoppingListService;
    private final MealPlanRepository mealPlanRepository;
    private final JavaMailSender mailSender;
    private final String senderAddress;

    public ShoppingListEmailService(
        ShoppingListService shoppingListService,
        MealPlanRepository mealPlanRepository,
        JavaMailSender mailSender,
        @Value("${app.mail.from}") String senderAddress
    ) {
        this.shoppingListService = shoppingListService;
        this.mealPlanRepository = mealPlanRepository;
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
    }

    public void send(Long shoppingListId, Long userId, String recipient) {
        ShoppingListResponse shoppingList =
            shoppingListService.findByIdAndUserId(shoppingListId, userId);

        MealPlan mealPlan = mealPlanRepository
            .findByIdAndUserId(shoppingList.mealPlanId(), userId)
            .orElseThrow(() ->
                new IllegalArgumentException("Planning introuvable")
            );

        LocalDate weekStart = mealPlan.getWeekStartDate();
        LocalDate weekEnd = weekStart.plusDays(
            mealPlan.isIncludeWeekend() ? 6 : 4
        );

        StringBuilder body = new StringBuilder()
            .append("Bonjour,\n\n")
            .append("Voici votre liste de courses pour la semaine du ")
            .append(formatWeekRange(weekStart, weekEnd))
            .append(" :\n\n");

        if (shoppingList.items().isEmpty()) {
            body.append("Aucun article à acheter.\n");
        } else {
            for (ShoppingItemResponse item : shoppingList.items()) {
                body.append("- ")
                    .append(item.ingredientName())
                    .append(" : ")
                    .append(formatQuantity(item.quantity()));

                if (item.unit() != null && !item.unit().isBlank()) {
                    body.append(" ").append(item.unit());
                }

                body.append("\n");
            }
        }

        body.append("\nBonnes courses !\n");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(recipient);
        message.setSubject("My Popote - Liste de courses");
        message.setText(body.toString());

        mailSender.send(message);
    }

    /**
     * Évite de répéter le mois et l'année lorsque les deux dates
     * appartiennent au même mois.
     */
    private String formatWeekRange(LocalDate start, LocalDate end) {
        if (
            start.getMonth() == end.getMonth() &&
            start.getYear() == end.getYear()
        ) {
            return start.getDayOfMonth() + " au " + end.format(DATE_FORMAT);
        }

        return start.format(DATE_FORMAT) + " au " + end.format(DATE_FORMAT);
    }

    private String formatQuantity(BigDecimal quantity) {
        return quantity == null
            ? "Quantité non précisée"
            : quantity.stripTrailingZeros().toPlainString();
    }
}