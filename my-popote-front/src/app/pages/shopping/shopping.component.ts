import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ShoppingService } from '../../core/services/shopping.service';
import { ShoppingList } from '../../models/shopping-list.model';

@Component({
  selector: 'app-shopping',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './shopping.component.html',
  styleUrl: './shopping.component.css',
})
export class ShoppingComponent implements OnInit {
  shoppingList: ShoppingList | null = null;

  loading = true;
  sendingEmail = false;
  errorMessage = '';
  emailMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly shoppingService: ShoppingService,
  ) {}

  ngOnInit(): void {
    const listId = Number(this.route.snapshot.queryParamMap.get('listId'));

    if (!listId) {
      this.loading = false;
      this.errorMessage = 'Aucune liste de courses sélectionnée.';
      return;
    }

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

