import { PlannedMeal } from './planned-meal.model';

/**
 * Planning hebdomadaire retourné par l'API.
 */
export interface Week {
  id: number;
  weekStartDate: string;
  estimatedCost: number | null;
  meals: PlannedMeal[];
}

/**
 * Données nécessaires pour demander à l'API
 * de générer ou régénérer une semaine.
 */
export interface GenerateWeekRequest {
  weekStartDate: string;
  includeWeekend: boolean;
  maxBudget: number | null;
  recipeIds: number[];

  /**
   * Tags à privilégier pendant la génération.
   *
   * Ils représentent des préférences et non
   * des contraintes obligatoires.
   */
  preferredTagIds: number[];
}
