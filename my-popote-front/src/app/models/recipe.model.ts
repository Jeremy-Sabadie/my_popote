import { RecipeIngredient, RecipeIngredientRequest } from './ingredient.model';

/**
 * Tag disponible pour caractériser une recette.
 *
 * Le groupe permet à l'interface de distinguer les tags nutritionnels,
 * alimentaires et pratiques sans coder leurs valeurs en dur.
 */
export interface RecipeTag {
  id: number;
  name: string;
  groupName: string;
}

/**
 * Recette telle qu'elle est retournée par l'API.
 */
export interface Recipe {
  id: number;
  name: string;
  category: string;
  servings: number;
  estimatedCost: number | null;
  instructions: string | null;
  ingredients: RecipeIngredient[];
  seasons: string[];
  tags: RecipeTag[];
}

/**
 * Données envoyées à l'API pour créer ou modifier une recette.
 *
 * category reste présent temporairement tant que la migration
 * complète vers les tags n'est pas terminée côté backend.
 */
export interface RecipeRequest {
  name: string;
  category: string;
  servings: number;
  estimatedCost: number | null;
  instructions: string | null;
  ingredients: RecipeIngredientRequest[];
  seasons: string[];
  tagIds: number[];
}
