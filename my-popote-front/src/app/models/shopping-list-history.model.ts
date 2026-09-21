/**
 * Résumé d'une liste de courses pour l'affichage de l'historique.
 * La date correspond au lundi de la semaine planifiée.
 */
export interface ShoppingListHistoryItem {
  id: number;
  mealPlanId: number;
  weekStartDate: string;
}
