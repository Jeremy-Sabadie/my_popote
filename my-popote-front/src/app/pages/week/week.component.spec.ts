
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { RecipeService } from '../../core/services/recipe.service';
import { ShoppingService } from '../../core/services/shopping.service';
import { WeekService } from '../../core/services/week.service';
import { PlannedMeal } from '../../models/planned-meal.model';
import { Week } from '../../models/week.model';
import { WeekComponent } from './week.component';

describe('WeekComponent', () => {
  let component: WeekComponent;
  let fixture: ComponentFixture<WeekComponent>;

  const currentWeek: Week = {
    id: 1,
    weekStartDate: '2026-09-07',
    estimatedCost: 25,
    meals: [
      {
        id: 10,
        recipeId: 1,
        recipeName: 'Poulet curry',
        mealDate: '2026-09-07',
        mealType: 'LUNCH',
      },
    ],
  };

  const historicalWeek: Week = {
    id: 2,
    weekStartDate: '2026-08-31',
    estimatedCost: 30,
    meals: [
      {
        id: 20,
        recipeId: 2,
        recipeName: 'Hachis parmentier',
        mealDate: '2026-08-31',
        mealType: 'DINNER',
      },
    ],
  };

  const weekServiceMock = {
    getWeek: jasmine.createSpy('getWeek').and.returnValue(of(currentWeek)),
    getHistory: jasmine.createSpy('getHistory').and.returnValue(of([])),
    replaceRecipe: jasmine.createSpy('replaceRecipe'),
  };

  const recipeServiceMock = {
    getRecipes: jasmine.createSpy('getRecipes').and.returnValue(of([])),
    getTags: jasmine.createSpy('getTags').and.returnValue(of([])),
  };

  const shoppingServiceMock = {
    generateFromMealPlan: jasmine.createSpy('generateFromMealPlan'),
    addManualItem: jasmine.createSpy('addManualItem'),
  };

  beforeEach(async () => {
    weekServiceMock.getWeek.calls.reset();
    weekServiceMock.getHistory.calls.reset();
    weekServiceMock.replaceRecipe.calls.reset();
    recipeServiceMock.getRecipes.calls.reset();
    recipeServiceMock.getTags.calls.reset();

    weekServiceMock.getWeek.and.returnValue(of(currentWeek));
    weekServiceMock.getHistory.and.returnValue(of([]));
    recipeServiceMock.getRecipes.and.returnValue(of([]));
    recipeServiceMock.getTags.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [WeekComponent],
      providers: [
        { provide: WeekService, useValue: weekServiceMock },
        { provide: RecipeService, useValue: recipeServiceMock },
        { provide: ShoppingService, useValue: shoppingServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WeekComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should replace only the selected planned meal', () => {
    const originalMeal: PlannedMeal = {
      id: 10,
      recipeId: 1,
      recipeName: 'Poulet curry',
      mealDate: '2026-09-07',
      mealType: 'LUNCH',
    };

    const replacementMeal: PlannedMeal = {
      id: 10,
      recipeId: 2,
      recipeName: 'Hachis parmentier',
      mealDate: '2026-09-07',
      mealType: 'LUNCH',
    };

    component.week = {
      ...currentWeek,
      meals: [originalMeal],
    };

    weekServiceMock.replaceRecipe.and.returnValue(of(replacementMeal));

    component.replaceMeal(originalMeal, 2);

    expect(weekServiceMock.replaceRecipe).toHaveBeenCalledOnceWith(10, 2);
    expect(component.week?.meals[0].recipeId).toBe(2);
    expect(component.week?.meals[0].recipeName).toBe('Hachis parmentier');
  });

  it('should show a print button when a week is loaded', () => {
    const printButton: HTMLButtonElement | null =
      fixture.nativeElement.querySelector('[data-testid="print-week-button"]');

    expect(printButton).not.toBeNull();
    expect(printButton?.textContent).toContain('Imprimer mon planning');
  });

  it('should open the browser print dialog when the print button is clicked', () => {
    const printSpy = spyOn(window, 'print').and.stub();

    const printButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('[data-testid="print-week-button"]');

    expect(printButton).not.toBeNull();

    printButton.click();

    expect(printSpy).toHaveBeenCalledTimes(1);
  });

  it('should display the selected historical week in the printable planning', () => {
    component.viewHistoryWeek(historicalWeek);
    fixture.detectChanges();

    const printablePlanning: HTMLElement | null =
      fixture.nativeElement.querySelector('[data-testid="printable-week"]');

    expect(printablePlanning).not.toBeNull();
    expect(printablePlanning?.textContent).toContain('31 août 2026');
    expect(printablePlanning?.textContent).toContain('Hachis parmentier');
    expect(printablePlanning?.textContent).not.toContain('Poulet curry');
  });
});
