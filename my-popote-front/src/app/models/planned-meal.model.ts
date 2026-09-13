/**
 * Repas planifié tel qu'il est retourné par l'API.
 *
 * L'id correspond au repas planifié lui-même.
 * Il est notamment utilisé pour remplacer uniquement
 * la recette d'un repas sans régénérer toute la semaine.
 */
export interface PlannedMeal {
  id: number;
  recipeId: number;
  recipeName: string;
  mealDate: string;
  mealType: string;
}
