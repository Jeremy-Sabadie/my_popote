import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Recipe, RecipeRequest, RecipeTag } from '../../models/recipe.model';

import { environment } from '../../../environments/environment';

/**
 * Centralise les échanges HTTP concernant les recettes et leurs tags.
 *
 * L'URL de l'API dépend de l'environnement Angular afin d'éviter
 * de coder localhost ou l'adresse Render directement dans le service.
 */
@Injectable({
  providedIn: 'root',
})
export class RecipeService {
  private readonly recipesUrl = `${environment.apiUrl}/api/recipes`;

  private readonly tagsUrl = `${environment.apiUrl}/api/tags`;

  private readonly exportsUrl = `${environment.apiUrl}/api/exports`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Récupère les recettes de l'utilisateur connecté.
   *
   * Plusieurs tags peuvent être fournis. Le backend applique alors
   * une logique ET : la recette doit posséder tous les tags sélectionnés.
   */
  getRecipes(tagIds: number[] = []): Observable<Recipe[]> {
    let params = new HttpParams();

    if (tagIds.length > 0) {
      params = params.set('tagIds', tagIds.join(','));
    }

    return this.http.get<Recipe[]>(this.recipesUrl, { params });
  }

  /**
   * Charge le référentiel des tags depuis l'API.
   */
  getTags(): Observable<RecipeTag[]> {
    return this.http.get<RecipeTag[]>(this.tagsUrl);
  }

  /**
   * Crée une nouvelle recette.
   */
  createRecipe(request: RecipeRequest): Observable<Recipe> {
    return this.http.post<Recipe>(this.recipesUrl, request);
  }

  /**
   * Modifie une recette existante.
   */
  updateRecipe(recipeId: number, request: RecipeRequest): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.recipesUrl}/${recipeId}`, request);
  }

  /**
   * Supprime une recette appartenant à l'utilisateur connecté.
   */
  deleteRecipe(recipeId: number): Observable<void> {
    return this.http.delete<void>(`${this.recipesUrl}/${recipeId}`);
  }

  /**
   * Télécharge une recette précise sous forme de fichier texte lisible.
   *
   * L'identité de l'utilisateur reste déterminée par le JWT côté API.
   */
  downloadRecipe(recipeId: number): Observable<Blob> {
    return this.http.get(`${this.exportsUrl}/recipes/${recipeId}.txt`, {
      responseType: 'blob',
    });
  }
}
