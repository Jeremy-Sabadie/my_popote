import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  ShoppingItem,
  ShoppingList,
} from '../../models/shopping-list.model';
import { ShoppingListHistoryItem } from '../../models/shopping-list-history.model';

export interface ManualShoppingItemRequest {
  name: string;
  quantity: number;
  unit: string;
}

@Injectable({
  providedIn: 'root',
})
export class ShoppingService {
  private readonly shoppingListsUrl =
    `${environment.apiUrl}/api/shopping-lists`;

  constructor(private readonly http: HttpClient) {}

  generateFromMealPlan(mealPlanId: number): Observable<ShoppingList> {
    return this.http.post<ShoppingList>(
      `${this.shoppingListsUrl}/meal-plans/${mealPlanId}`,
      null,
    );
  }

  /**
   * Récupère les résumés des listes de courses de l'utilisateur connecté.
   */
  getShoppingLists(): Observable<ShoppingListHistoryItem[]> {
    return this.http.get<ShoppingListHistoryItem[]>(
      this.shoppingListsUrl,
    );
  }

  getShoppingList(shoppingListId: number): Observable<ShoppingList> {
    return this.http.get<ShoppingList>(
      `${this.shoppingListsUrl}/${shoppingListId}`,
    );
  }

  addManualItem(
    shoppingListId: number,
    item: ManualShoppingItemRequest,
  ): Observable<ShoppingItem> {
    return this.http.post<ShoppingItem>(
      `${this.shoppingListsUrl}/${shoppingListId}/items`,
      item,
    );
  }

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
