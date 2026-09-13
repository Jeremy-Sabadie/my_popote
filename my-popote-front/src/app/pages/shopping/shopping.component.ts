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
  errorMessage = '';

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
   * Télécharge la liste de courses au format texte.
   */
  exportTxt(): void {
    if (!this.shoppingList || this.shoppingList.items.length === 0) {
      return;
    }

    const lines = [
      'MY POPOTE - LISTE DE COURSES',
      '',
      ...this.shoppingList.items.map(
        (item) => `- ${item.ingredientName} : ${item.quantity} ${item.unit}`,
      ),
    ];

    const content = lines.join('\n');

    const blob = new Blob([content], {
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
}
