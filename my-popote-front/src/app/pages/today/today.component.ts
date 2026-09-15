import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { WeekService } from '../../core/services/week.service';
import { PlannedMeal } from '../../models/planned-meal.model';

@Component({
  selector: 'app-today',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './today.component.html',
  styleUrl: './today.component.css',
})
export class TodayComponent implements OnInit {
  meals: PlannedMeal[] = [];

  loading = true;
  errorMessage = '';

  readonly today = new Date();

  constructor(
    private readonly weekService: WeekService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadTodayMeals();
  }

  /**
   * Date affichée dans l'interface.
   *
   * On la formate ici pour garantir un affichage français
   * sans dépendre de la configuration globale du DatePipe.
   */
  get todayLabel(): string {
    return new Intl.DateTimeFormat('fr-FR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(this.today);
  }

  /**
   * Retourne le déjeuner prévu aujourd'hui.
   */
  get lunch(): PlannedMeal | undefined {
    return this.meals.find((meal) => meal.mealType === 'LUNCH');
  }

  /**
   * Retourne le dîner prévu aujourd'hui.
   */
  get dinner(): PlannedMeal | undefined {
    return this.meals.find((meal) => meal.mealType === 'DINNER');
  }

  /**
   * Ouvre la recette depuis le repas du jour.
   *
   * On conserve l'origine de navigation afin de pouvoir
   * revenir ensuite naturellement sur la page Aujourd'hui.
   */
  viewRecipe(recipeId: number): void {
    this.router.navigate(['/recipes'], {
      queryParams: {
        recipeId,
        from: 'today',
      },
    });
  }

  /**
   * Charge uniquement les repas de la journée.
   *
   * L'API possède déjà un endpoint dédié : inutile de
   * récupérer tout le planning hebdomadaire.
   */
  private loadTodayMeals(): void {
    this.loading = true;
    this.errorMessage = '';

    this.weekService.getMealsForDate(this.formatDate(this.today)).subscribe({
      next: (meals) => {
        this.meals = meals;
        this.loading = false;
      },

      error: () => {
        this.loading = false;
        this.errorMessage =
          'Impossible de charger les repas prévus aujourd’hui.';
      },
    });
  }

  /**
   * Produit la date au format attendu par l'API :
   * YYYY-MM-DD.
   *
   * On utilise la date locale plutôt que toISOString()
   * pour éviter un décalage de journée lié au fuseau horaire.
   */
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
