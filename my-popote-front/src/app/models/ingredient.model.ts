/**
 * Ingrédient utilisé dans une recette.
 *
 * Les propriétés correspondent directement au DTO retourné
 * par l'API Spring Boot.
 */
export interface RecipeIngredient {
  ingredientId: number;
  ingredientName: string;
  quantity: number;
  unit: string;
}

/**
 * Données envoyées à l'API lors de la création ou de la modification
 * d'un ingrédient d'une recette.
 */
export interface RecipeIngredientRequest {
  ingredientName: string;
  quantity: number;
  unit: string;
}
