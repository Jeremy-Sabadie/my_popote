package fr.mypopote.my_popote_api.export;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.mypopote.my_popote_api.security.CurrentUserService;

/**
 * Expose les endpoints permettant de télécharger les exports de l'application.
 *
 * Les recettes sont exportées sous forme de fichier texte lisible.
 * Les listes de courses restent au format CSV tabulaire.
 *
 * L'utilisateur est toujours identifié grâce au JWT.
 * Aucun userId envoyé par le frontend n'est utilisé.
 */
@RestController
@RequestMapping("/api/exports")
public class CsvExportController {

    private static final MediaType TEXT_MEDIA_TYPE =
        MediaType.parseMediaType("text/plain;charset=UTF-8");

    private static final MediaType CSV_MEDIA_TYPE =
        MediaType.parseMediaType("text/csv;charset=UTF-8");

    private final CsvExportService csvExportService;
    private final CurrentUserService currentUserService;

    public CsvExportController(
            CsvExportService csvExportService,
            CurrentUserService currentUserService) {

        this.csvExportService = csvExportService;
        this.currentUserService = currentUserService;
    }

    /**
     * Télécharge toutes les recettes de l'utilisateur connecté
     * sous forme d'une fiche texte lisible.
     */
    @GetMapping("/recipes.txt")
    public ResponseEntity<byte[]> exportRecipes(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = currentUserService.getUserId(jwt);

        String text =
            csvExportService.exportRecipes(userId);

        return buildTextResponse(
            text,
            "my-popote-recettes.txt"
        );
    }

    /**
     * Télécharge une liste de courses au format CSV.
     */
    @GetMapping("/shopping-lists/{shoppingListId}.csv")
    public ResponseEntity<byte[]> exportShoppingList(
            @PathVariable Long shoppingListId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = currentUserService.getUserId(jwt);

        String csv =
            csvExportService.exportShoppingList(
                shoppingListId,
                userId
            );

        return buildCsvResponse(
            csv,
            "my-popote-courses-" + shoppingListId + ".csv"
        );
    }

    /**
     * Prépare la réponse HTTP pour l'export texte des recettes.
     *
     * Le contenu est explicitement renvoyé en UTF-8 afin de conserver
     * correctement les accents dans les éditeurs de texte.
     */
    private ResponseEntity<byte[]> buildTextResponse(
            String text,
            String filename) {

        byte[] content =
            text.getBytes(StandardCharsets.UTF_8);

        ContentDisposition disposition =
            ContentDisposition.attachment()
                .filename(
                    filename,
                    StandardCharsets.UTF_8
                )
                .build();

        return ResponseEntity.ok()
            .contentType(TEXT_MEDIA_TYPE)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                disposition.toString()
            )
            .body(content);
    }

    /**
     * Prépare la réponse HTTP utilisée pour télécharger un fichier CSV.
     *
     * Un BOM UTF-8 est ajouté au début du fichier afin d'améliorer
     * la reconnaissance des caractères accentués par certains tableurs.
     */
    private ResponseEntity<byte[]> buildCsvResponse(
            String csv,
            String filename) {

        byte[] bom = new byte[] {
            (byte) 0xEF,
            (byte) 0xBB,
            (byte) 0xBF
        };

        byte[] content =
            csv.getBytes(StandardCharsets.UTF_8);

        byte[] result =
            new byte[bom.length + content.length];

        System.arraycopy(
            bom,
            0,
            result,
            0,
            bom.length
        );

        System.arraycopy(
            content,
            0,
            result,
            bom.length,
            content.length
        );

        ContentDisposition disposition =
            ContentDisposition.attachment()
                .filename(
                    filename,
                    StandardCharsets.UTF_8
                )
                .build();

        return ResponseEntity.ok()
            .contentType(CSV_MEDIA_TYPE)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                disposition.toString()
            )
            .body(result);
    }
}