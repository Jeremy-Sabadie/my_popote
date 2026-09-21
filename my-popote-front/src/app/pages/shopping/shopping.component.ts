import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ShoppingService } from '../../core/services/shopping.service';
import { ShoppingList } from '../../models/shopping-list.model';
import { ShoppingListHistoryItem } from '../../models/shopping-list-history.model';

@Component({
  selector: 'app-shopping',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './shopping.component.html',
  styleUrl: './shopping.component.css',
})
export class ShoppingComponent implements OnInit {
  shoppingList: ShoppingList | null = null;
  shoppingListHistory: ShoppingListHistoryItem[] = [];

  loading = false;
  loadingHistory = true;
  sendingEmail = false;

  errorMessage = '';
  historyErrorMessage = '';
  emailMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly shoppingService: ShoppingService,
  ) {}

  ngOnInit(): void {
    this.loadHistory();

    // Réagit aussi aux changements d'URL sans recréer le composant.
    this.route.queryParamMap.subscribe((params) => {
      const rawListId = params.get('listId');
      const listId = rawListId === null ? null : Number(rawListId);

      if (listId === null) {
        this.shoppingList = null;
        this.errorMessage = '';
        this.emailMessage = '';
        this.loading = false;
        return;
      }

      if (!Number.isSafeInteger(listId) || listId <= 0) {
        this.shoppingList = null;
        this.loading = false;
        this.errorMessage = 'Identifiant de liste de courses invalide.';
        return;
      }

      this.loadShoppingList(listId);
    });
  }

  /**
   * Charge les semaines pour lesquelles une liste de courses existe.
   */
  private loadHistory(): void {
    this.loadingHistory = true;
    this.historyErrorMessage = '';

    this.shoppingService.getShoppingLists().subscribe({
      next: (history) => {
        this.shoppingListHistory = history;
        this.loadingHistory = false;
      },
      error: () => {
        this.shoppingListHistory = [];
        this.loadingHistory = false;
        this.historyErrorMessage =
          "Impossible de charger l'historique des listes de courses.";
      },
    });
  }

  /**
   * Charge une liste appartenant à l'utilisateur connecté.
   */
  private loadShoppingList(listId: number): void {
    this.loading = true;
    this.shoppingList = null;
    this.errorMessage = '';
    this.emailMessage = '';

    this.shoppingService.getShoppingList(listId).subscribe({
      next: (shoppingList) => {
        this.shoppingList = shoppingList;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Impossible de charger la liste de courses.';
      },
    });
  }

  /**
   * Ouvre la liste choisie en conservant son identifiant dans l'URL.
   */
  openShoppingList(listId: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { listId },
      queryParamsHandling: 'merge',
    });
  }

  /**
   * Revient à la vue de l'historique.
   */
  showHistory(): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { listId: null },
      queryParamsHandling: 'merge',
    });
  }

  /**
   * Prépare le contenu du fichier texte à télécharger.
   */
  private buildShoppingListText(): string {
    if (!this.shoppingList) {
      return '';
    }

    return [
      'MY POPOTE - LISTE DE COURSES',
      '',
      ...this.shoppingList.items.map((item) =>
        `- ${item.ingredientName} : ${item.quantity} ${item.unit ?? ''}`.trimEnd(),
      ),
    ].join('\n');
  }

  /**
   * Télécharge la liste au format texte.
   */
  exportTxt(): void {
    if (!this.shoppingList || this.shoppingList.items.length === 0) {
      return;
    }

    const blob = new Blob([this.buildShoppingListText()], {
      type: 'text/plain;charset=utf-8',
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    link.href = url;
    link.download = 'liste-de-courses.txt';

    document.body.appendChild(link);
    link.click();
    link.remove();

    URL.revokeObjectURL(url);
  }

  /**
   * Demande l'adresse du destinataire, puis confie l'envoi à l'API.
   * Aucun message de succès n'est affiché avant la réponse du serveur.
   */
  shareByEmail(): void {
    if (
      !this.shoppingList ||
      this.shoppingList.items.length === 0 ||
      this.sendingEmail
    ) {
      return;
    }

    const recipient = window.prompt(
      'À quelle adresse e-mail envoyer la liste de courses ?',
    );

    if (recipient === null) {
      return;
    }

    const email = recipient.trim();

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      this.emailMessage = 'Saisis une adresse e-mail valide.';
      return;
    }

    this.emailMessage = '';
    this.sendingEmail = true;

    this.shoppingService
      .sendShoppingListByEmail(this.shoppingList.id, email)
      .subscribe({
        next: () => {
          this.sendingEmail = false;
          this.emailMessage = `E-mail accepté pour envoi à ${email}.`;
        },
        error: () => {
          this.sendingEmail = false;
          this.emailMessage =
            "L'envoi a échoué. Vérifie la configuration e-mail de l'API et réessaie.";
        },
      });
  }
}
