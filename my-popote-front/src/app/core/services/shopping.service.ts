import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ShoppingItem, ShoppingList } from '../../models/shopping-list.model';

export interface ManualShoppingItemRequest {
  name: string;
  quantity: number;
  unit: string;
}

@Injectable({
  providedIn: 'root',
})
export class ShoppingService {
  private readonly shoppingListsUrl = `${environment.apiUrl}/api/shopping-lists`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Génère la liste de courses correspondant au planning choisi.
   */
  generateFromMealPlan(mealPlanId: number): Observable<ShoppingList> {
    return this.http.post<ShoppingList>(
      `${this.shoppingListsUrl}/meal-plans/${mealPlanId}`,
      null,
    );
  }

  /**
   * Recharge une liste de courses existante.
   */
  getShoppingList(shoppingListId: number): Observable<ShoppingList> {
    return this.http.get<ShoppingList>(
      `${this.shoppingListsUrl}/${shoppingListId}`,
    );
  }

  /**
   * Ajoute un produit saisi manuellement.
   */
  addManualItem(
    shoppingListId: number,
    item: ManualShoppingItemRequest,
  ): Observable<ShoppingItem> {
    return this.http.post<ShoppingItem>(
      `${this.shoppingListsUrl}/${shoppingListId}/items`,
      item,
    );
  }

  /**
   * Demande à l'API d'envoyer la liste par e-mail via le SMTP configuré.
   * La réponse 204 ne contient pas de corps.
   */
  sendShoppingListByEmail(
    shoppingListId: number,
    recipient: string,
  ): Observable<void> {
    return this.http.post<void>(
      `${this.shoppingListsUrl}/${shoppingListId}/email`,
      { recipient },
    );
  }
}
