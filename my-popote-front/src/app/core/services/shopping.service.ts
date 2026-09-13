import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ShoppingList } from '../../models/shopping-list.model';

@Injectable({
  providedIn: 'root',
})
export class ShoppingService {
  private readonly shoppingListsUrl = `${environment.apiUrl}/api/shopping-lists`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Génère la liste de courses correspondant
   * au planning hebdomadaire choisi.
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
}
