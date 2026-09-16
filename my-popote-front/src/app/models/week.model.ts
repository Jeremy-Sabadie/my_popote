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
 * Saisons reconnues par l'API.
 * null conserve la saison déduite de la date du planning.
 */
export type PreferredSeason = 'SPRING' | 'SUMMER' | 'AUTUMN' | 'WINTER' | null;

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

  /**
   * Saison choisie pour la génération.
   * null laisse l'API déterminer la saison automatiquement.
   */
  preferredSeason: PreferredSeason;
}
