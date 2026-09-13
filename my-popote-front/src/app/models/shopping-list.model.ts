export interface ShoppingItem {
  id: number;
  ingredientId: number | null;
  ingredientName: string;
  quantity: number;
  unit: string;
  checked: boolean;
  alreadyOwned: boolean;
  manual: boolean;
}

export interface ShoppingList {
  id: number;
  mealPlanId: number;
  items: ShoppingItem[];
  alreadyOwnedItems: ShoppingItem[];
}