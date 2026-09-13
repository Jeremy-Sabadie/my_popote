import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { RecipeService } from '../../core/services/recipe.service';
import { WeekService } from '../../core/services/week.service';
import { PlannedMeal } from '../../models/planned-meal.model';
import { WeekComponent } from './week.component';

describe('WeekComponent', () => {
  let component: WeekComponent;
  let fixture: ComponentFixture<WeekComponent>;

  const weekServiceMock = {
    getWeek: jasmine.createSpy('getWeek').and.returnValue(
      of({
        id: 1,
        weekStartDate: '2026-09-07',
        estimatedCost: 25,
        meals: [],
      }),
    ),

    getHistory: jasmine.createSpy('getHistory').and.returnValue(of([])),

    replaceRecipe: jasmine.createSpy('replaceRecipe'),
  };

  const recipeServiceMock = {
    getRecipes: jasmine.createSpy('getRecipes').and.returnValue(of([])),
  };

  beforeEach(async () => {
    weekServiceMock.getWeek.calls.reset();
    weekServiceMock.getHistory.calls.reset();
    weekServiceMock.replaceRecipe.calls.reset();

    weekServiceMock.getWeek.and.returnValue(
      of({
        id: 1,
        weekStartDate: '2026-09-07',
        estimatedCost: 25,
        meals: [],
      }),
    );

    weekServiceMock.getHistory.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [WeekComponent],
      providers: [
        {
          provide: WeekService,
          useValue: weekServiceMock,
        },
        {
          provide: RecipeService,
          useValue: recipeServiceMock,
        },
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
      id: 1,
      weekStartDate: '2026-09-07',
      estimatedCost: 25,
      meals: [originalMeal],
    };

    weekServiceMock.replaceRecipe.and.returnValue(of(replacementMeal));

    component.replaceMeal(originalMeal, 2);

    expect(weekServiceMock.replaceRecipe).toHaveBeenCalledOnceWith(10, 2);

    expect(component.week.meals[0].recipeId).toBe(2);

    expect(component.week.meals[0].recipeName).toBe('Hachis parmentier');
  });
});
