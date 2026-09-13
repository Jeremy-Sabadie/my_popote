import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PlannedMeal } from '../../models/planned-meal.model';
import { GenerateWeekRequest, Week } from '../../models/week.model';

/**
 * Centralise les échanges HTTP liés au planning hebdomadaire.
 *
 * L'identité de l'utilisateur n'est jamais envoyée par le frontend :
 * elle est déterminée par le JWT côté API.
 */
@Injectable({
  providedIn: 'root',
})
export class WeekService {
  private readonly mealPlansUrl = `${environment.apiUrl}/api/meal-plans`;
  private readonly plannedMealsUrl = `${environment.apiUrl}/api/planned-meals`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Récupère le planning correspondant au lundi fourni.
   */
  getWeek(weekStartDate: string): Observable<Week> {
    const params = new HttpParams().set('weekStartDate', weekStartDate);

    return this.http.get<Week>(this.mealPlansUrl, { params });
  }

  /**
   * Génère ou régénère un planning hebdomadaire
   * à partir des recettes choisies.
   */
  generateWeek(request: GenerateWeekRequest): Observable<Week> {
    return this.http.post<Week>(`${this.mealPlansUrl}/generate`, request);
  }

  /**
   * Remplace uniquement la recette d'un repas planifié.
   *
   * Le reste de la semaine reste inchangé.
   */
  replaceRecipe(
    plannedMealId: number,
    recipeId: number,
  ): Observable<PlannedMeal> {
    const params = new HttpParams().set('recipeId', recipeId.toString());

    return this.http.patch<PlannedMeal>(
      `${this.plannedMealsUrl}/${plannedMealId}/recipe`,
      null,
      { params },
    );
  }

  /**
   * Récupère les semaines précédemment générées.
   */
  getHistory(): Observable<Week[]> {
    return this.http.get<Week[]>(`${this.mealPlansUrl}/history`);
  }

  /**
   * Retourne les repas planifiés pour une date précise.
   */
  getMealsForDate(date: string): Observable<PlannedMeal[]> {
    const params = new HttpParams().set('date', date);

    return this.http.get<PlannedMeal[]>(`${this.mealPlansUrl}/day`, { params });
  }
}
