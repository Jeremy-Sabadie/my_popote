import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { RecipeService } from '../../core/services/recipe.service';
import {
  ManualShoppingItemRequest,
  ShoppingService,
} from '../../core/services/shopping.service';
import { WeekService } from '../../core/services/week.service';
import { PlannedMeal } from '../../models/planned-meal.model';
import { Recipe, RecipeTag } from '../../models/recipe.model';
import { PreferredSeason, Week } from '../../models/week.model';

interface WeekDay {
  date: string;
  label: string;
  meals: PlannedMeal[];
}

@Component({
  selector: 'app-week',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './week.component.html',
  styleUrl: './week.component.css',
})
export class WeekComponent implements OnInit {
  week: Week | null = null;
  history: Week[] = [];
  recipes: Recipe[] = [];

  preferenceTags: RecipeTag[] = [];
  selectedPreferredTagIds = new Set<number>();

  /**
   * Par défaut, l'API déduit la saison de la date du planning.
   * L'utilisateur peut choisir une autre saison pour sa proposition.
   */
  selectedPreferredSeason: PreferredSeason = null;

  readonly seasonOptions: {
    value: PreferredSeason;
    label: string;
  }[] = [
    { value: null, label: 'Automatique' },
    { value: 'SPRING', label: 'Printemps' },
    { value: 'SUMMER', label: 'Été' },
    { value: 'AUTUMN', label: 'Automne' },
    { value: 'WINTER', label: 'Hiver' },
  ];

  /**
   * Budget maximum souhaité pour la semaine.
   */
  weeklyMaxBudget = 60;

  /**
   * Produits supplémentaires préparés par l'utilisateur.
   * Ils ne sont envoyés à l'API qu'après génération
   * de la liste de courses.
   */
  manualShoppingItems: ManualShoppingItemRequest[] = [];

  manualItemName = '';
  manualItemQuantity = 1;
  manualItemUnit = 'pièce';

  days: WeekDay[] = [];

  loading = true;
  generating = false;
  historyLoading = true;
  validatingWeek = false;
  preferencesLoading = true;

  replacingMealId: number | null = null;

  selectedRecipeByMeal: Record<number, number | null> = {};

  errorMessage = '';
  generationErrorMessage = '';
  replacementErrorMessage = '';
  validationErrorMessage = '';
  preferencesErrorMessage = '';
  manualItemErrorMessage = '';

  weekStartDate = '';

  constructor(
    private readonly weekService: WeekService,
    private readonly recipeService: RecipeService,
    private readonly shoppingService: ShoppingService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.weekStartDate = this.getPlanningWeekMonday();

    this.loadWeek();
    this.loadHistory();
    this.loadPreferenceTags();
  }

  private loadWeek(): void {
    this.loading = true;
    this.errorMessage = '';

    this.weekService.getWeek(this.weekStartDate).subscribe({
      next: (week) => {
        this.displayWeek(week);
        this.loading = false;
      },

      error: (error: HttpErrorResponse) => {
        this.loading = false;

        if (error.status === 404) {
          this.week = null;
          this.days = [];
          return;
        }

        this.errorMessage = 'Impossible de charger le planning pour le moment.';
      },
    });
  }

  private loadPreferenceTags(): void {
    this.preferencesLoading = true;
    this.preferencesErrorMessage = '';

    this.recipeService.getTags().subscribe({
      next: (tags) => {
        this.preferenceTags = tags;
        this.preferencesLoading = false;
      },

      error: () => {
        this.preferenceTags = [];
        this.preferencesLoading = false;
        this.preferencesErrorMessage =
          'Impossible de charger les préférences pour le moment.';
      },
    });
  }

  togglePreferredTag(tagId: number): void {
    if (this.selectedPreferredTagIds.has(tagId)) {
      this.selectedPreferredTagIds.delete(tagId);
      return;
    }

    this.selectedPreferredTagIds.add(tagId);
  }

  isPreferredTagSelected(tagId: number): boolean {
    return this.selectedPreferredTagIds.has(tagId);
  }

  /**
   * Remet les tags et la saison à leur état par défaut.
   * Le budget reste inchangé.
   */
  clearPreferences(): void {
    this.selectedPreferredTagIds.clear();
    this.selectedPreferredSeason = null;
  }

  generateWeek(): void {
    if (this.generating) {
      return;
    }

    this.generating = true;
    this.generationErrorMessage = '';

    this.recipeService.getRecipes().subscribe({
      next: (recipes) => {
        if (recipes.length === 0) {
          this.generating = false;
          this.generationErrorMessage =
            'Ajoutez au moins une recette avant de générer votre semaine.';
          return;
        }

        this.recipes = recipes;

        this.weekService
          .generateWeek({
            weekStartDate: this.weekStartDate,
            includeWeekend: true,
            maxBudget: this.weeklyMaxBudget,
            recipeIds: recipes.map((recipe) => recipe.id),
            preferredTagIds: [...this.selectedPreferredTagIds],
            preferredSeason: this.selectedPreferredSeason,
          })
          .subscribe({
            next: (week) => {
              this.displayWeek(week);
              this.generating = false;
              this.loadHistory();
            },

            error: () => {
              this.generating = false;
              this.generationErrorMessage =
                'La semaine n’a pas pu être générée. Veuillez réessayer.';
            },
          });
      },

      error: () => {
        this.generating = false;
        this.generationErrorMessage = 'Impossible de charger vos recettes.';
      },
    });
  }

  /**
   * Ouvre la fiche déjà existante dans l'écran Recettes.
   */
  viewRecipe(recipeId: number): void {
    this.router.navigate(['/recipes'], {
      queryParams: {
        recipeId,
        from: 'week',
      },
    });
  }

  startReplacement(meal: PlannedMeal): void {
    this.replacementErrorMessage = '';
    this.replacingMealId = meal.id;

    this.selectedRecipeByMeal[meal.id] = null;

    if (this.recipes.length > 0) {
      return;
    }

    this.recipeService.getRecipes().subscribe({
      next: (recipes) => {
        this.recipes = recipes;
      },

      error: () => {
        this.replacingMealId = null;
        this.replacementErrorMessage =
          'Impossible de charger les recettes disponibles.';
      },
    });
  }

  cancelReplacement(): void {
    this.replacingMealId = null;
    this.replacementErrorMessage = '';
  }

  replaceMeal(meal: PlannedMeal, recipeId: number): void {
    if (!recipeId) {
      this.replacementErrorMessage = 'Choisissez une recette de remplacement.';
      return;
    }

    this.replacementErrorMessage = '';

    this.weekService.replaceRecipe(meal.id, recipeId).subscribe({
      next: (updatedMeal) => {
        if (!this.week) {
          return;
        }

        this.week = {
          ...this.week,
          meals: this.week.meals.map((currentMeal) =>
            currentMeal.id === updatedMeal.id ? updatedMeal : currentMeal,
          ),
        };

        this.days = this.buildDays(this.week);

        this.replacingMealId = null;

        delete this.selectedRecipeByMeal[meal.id];
      },

      error: () => {
        this.replacementErrorMessage =
          'Le repas n’a pas pu être remplacé. Veuillez réessayer.';
      },
    });
  }

  recipeOptionLabel(recipe: Recipe): string {
    const seasons = recipe.seasons ?? [];
    const tags = (recipe.tags ?? []).map((tag) => tag.name);

    const characteristics = [...seasons, ...tags];

    if (characteristics.length === 0) {
      return recipe.name;
    }

    return `${recipe.name} — ${characteristics.join(' · ')}`;
  }

  /**
   * Prépare un produit supplémentaire.
   * Rien n'est encore envoyé à l'API à cette étape.
   */
  addManualShoppingItem(): void {
    const name = this.manualItemName.trim();
    const unit = this.manualItemUnit.trim();

    this.manualItemErrorMessage = '';

    if (!name) {
      this.manualItemErrorMessage = 'Indiquez le nom du produit à ajouter.';
      return;
    }

    if (!this.manualItemQuantity || this.manualItemQuantity <= 0) {
      this.manualItemErrorMessage = 'La quantité doit être supérieure à zéro.';
      return;
    }

    if (!unit) {
      this.manualItemErrorMessage = 'Indiquez une unité.';
      return;
    }

    this.manualShoppingItems.push({
      name,
      quantity: this.manualItemQuantity,
      unit,
    });

    // On remet le formulaire dans son état pratique
    // pour pouvoir ajouter rapidement le produit suivant.
    this.manualItemName = '';
    this.manualItemQuantity = 1;
    this.manualItemUnit = 'pièce';
  }

  removeManualShoppingItem(index: number): void {
    this.manualShoppingItems.splice(index, 1);
  }

  validateWeek(): void {
    if (!this.week || !this.viewingCurrentWeek) {
      return;
    }

    this.validatingWeek = true;
    this.validationErrorMessage = '';

    this.shoppingService.generateFromMealPlan(this.week.id).subscribe({
      next: (shoppingList) => {
        if (this.manualShoppingItems.length === 0) {
          this.openShoppingList(shoppingList.id);
          return;
        }

        /**
         * La liste doit exister avant les ajouts manuels :
         * on envoie donc les produits seulement après sa génération.
         */
        const manualItemRequests = this.manualShoppingItems.map((item) =>
          this.shoppingService.addManualItem(shoppingList.id, item),
        );

        forkJoin(manualItemRequests).subscribe({
          next: () => {
            this.manualShoppingItems = [];
            this.openShoppingList(shoppingList.id);
          },

          error: () => {
            this.validatingWeek = false;
            this.validationErrorMessage =
              'La liste a été générée, mais certains produits supplémentaires n’ont pas pu être ajoutés.';
          },
        });
      },

      error: () => {
        this.validatingWeek = false;
        this.validationErrorMessage =
          'La liste de courses n’a pas pu être générée.';
      },
    });
  }

  private openShoppingList(shoppingListId: number): void {
    this.validatingWeek = false;

    this.router.navigate(['/shopping'], {
      queryParams: {
        listId: shoppingListId,
      },
    });
  }

  private loadHistory(): void {
    this.historyLoading = true;

    this.weekService.getHistory().subscribe({
      next: (history) => {
        this.history = history
          .filter((week) => week.weekStartDate < this.weekStartDate)
          .sort((weekA, weekB) =>
            weekB.weekStartDate.localeCompare(weekA.weekStartDate),
          );

        this.historyLoading = false;
      },

      error: () => {
        this.history = [];
        this.historyLoading = false;
      },
    });
  }

  viewHistoryWeek(week: Week): void {
    this.displayWeek(week);
  }

  viewCurrentWeek(): void {
    this.loadWeek();
  }

  private displayWeek(week: Week): void {
    this.week = week;
    this.days = this.buildDays(week);
  }

  private getPlanningWeekMonday(): string {
    const today = new Date();
    const day = today.getDay();

    const monday = new Date(today);

    if (day === 0) {
      monday.setDate(today.getDate() + 1);
    } else if (day === 6) {
      monday.setDate(today.getDate() + 2);
    } else {
      monday.setDate(today.getDate() + (1 - day));
    }

    return this.formatDateForApi(monday);
  }

  private buildDays(week: Week): WeekDay[] {
    const groupedMeals = new Map<string, PlannedMeal[]>();

    for (const meal of week.meals) {
      const meals = groupedMeals.get(meal.mealDate) ?? [];

      meals.push(meal);

      groupedMeals.set(meal.mealDate, meals);
    }

    return [...groupedMeals.entries()]
      .sort(([dateA], [dateB]) => dateA.localeCompare(dateB))
      .map(([date, meals]) => ({
        date,
        label: this.formatDayLabel(date),
        meals: [...meals].sort(
          (mealA, mealB) =>
            this.mealTypeOrder(mealA.mealType) -
            this.mealTypeOrder(mealB.mealType),
        ),
      }));
  }

  private formatDayLabel(date: string): string {
    const parsedDate = new Date(`${date}T12:00:00`);

    const label = new Intl.DateTimeFormat('fr-FR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
    }).format(parsedDate);

    return label.charAt(0).toUpperCase() + label.slice(1);
  }

  weekLabel(date: string): string {
    const parsedDate = new Date(`${date}T12:00:00`);

    return new Intl.DateTimeFormat('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(parsedDate);
  }

  mealTypeLabel(mealType: string): string {
    const labels: Record<string, string> = {
      BREAKFAST: 'Petit-déjeuner',
      LUNCH: 'Déjeuner',
      DINNER: 'Dîner',
      SNACK: 'Collation',
    };

    return labels[mealType.toUpperCase()] ?? mealType;
  }

  private mealTypeOrder(mealType: string): number {
    const order: Record<string, number> = {
      BREAKFAST: 1,
      LUNCH: 2,
      SNACK: 3,
      DINNER: 4,
    };

    return order[mealType.toUpperCase()] ?? 99;
  }

  private formatDateForApi(date: Date): string {
    const year = date.getFullYear();

    const month = String(date.getMonth() + 1).padStart(2, '0');

    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  get viewingCurrentWeek(): boolean {
    return this.week?.weekStartDate === this.weekStartDate;
  }
}
