import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { RecipeService } from '../../core/services/recipe.service';
import { ShoppingService } from '../../core/services/shopping.service';
import { WeekService } from '../../core/services/week.service';
import { PlannedMeal } from '../../models/planned-meal.model';
import { Recipe, RecipeTag } from '../../models/recipe.model';
import { Week } from '../../models/week.model';

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

  /**
   * Charge le planning de la semaine à préparer.
   */
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

  /**
   * Charge les tags disponibles pour les préférences
   * de génération de la semaine.
   */
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

  /**
   * Active ou désactive un tag dans les préférences.
   */
  togglePreferredTag(tagId: number): void {
    if (this.selectedPreferredTagIds.has(tagId)) {
      this.selectedPreferredTagIds.delete(tagId);
      return;
    }

    this.selectedPreferredTagIds.add(tagId);
  }

  /**
   * Indique si un tag est actuellement sélectionné.
   */
  isPreferredTagSelected(tagId: number): boolean {
    return this.selectedPreferredTagIds.has(tagId);
  }

  /**
   * Efface toutes les préférences sélectionnées.
   */
  clearPreferredTags(): void {
    this.selectedPreferredTagIds.clear();
  }

  /**
   * Génère la semaine à partir des recettes disponibles.
   */
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
            maxBudget: null,
            recipeIds: recipes.map((recipe) => recipe.id),
            preferredTagIds: [...this.selectedPreferredTagIds],
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
   * Prépare le remplacement d'un repas.
   */
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

  /**
   * Annule le remplacement en cours.
   */
  cancelReplacement(): void {
    this.replacingMealId = null;
    this.replacementErrorMessage = '';
  }

  /**
   * Remplace uniquement la recette du repas sélectionné.
   */
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

  /**
   * Construit le libellé affiché dans la liste
   * des recettes de remplacement.
   *
   * Exemple :
   * Poulet curry — Hiver · Sport · Protéiné
   */
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
   * Valide la semaine en générant la liste de courses
   * correspondant au planning.
   */
  validateWeek(): void {
    if (!this.week || !this.viewingCurrentWeek) {
      return;
    }

    this.validatingWeek = true;
    this.validationErrorMessage = '';

    this.shoppingService.generateFromMealPlan(this.week.id).subscribe({
      next: (shoppingList) => {
        this.validatingWeek = false;

        this.router.navigate(['/shopping'], {
          queryParams: {
            listId: shoppingList.id,
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

  /**
   * Charge l'historique des semaines générées.
   */
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

  /**
   * Détermine le lundi de la semaine à planifier.
   *
   * Du lundi au vendredi :
   * on prépare la semaine en cours.
   *
   * Le samedi et le dimanche :
   * on prépare la semaine suivante.
   */
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
