import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { WeekService } from '../../core/services/week.service';
import { PlannedMeal } from '../../models/planned-meal.model';
import { TodayComponent } from './today.component';

registerLocaleData(localeFr);

describe('TodayComponent', () => {
  let component: TodayComponent;
  let fixture: ComponentFixture<TodayComponent>;

  const plannedMeals: PlannedMeal[] = [
    {
      id: 1,
      recipeId: 10,
      recipeName: 'Poulet et riz',
      mealDate: '2026-09-15',
      mealType: 'LUNCH',
    },
    {
      id: 2,
      recipeId: 20,
      recipeName: 'Omelette aux légumes',
      mealDate: '2026-09-15',
      mealType: 'DINNER',
    },
  ];

  const weekServiceMock = {
    getMealsForDate: jasmine
      .createSpy('getMealsForDate')
      .and.returnValue(of(plannedMeals)),
  };

  beforeEach(async () => {
    weekServiceMock.getMealsForDate.calls.reset();
    weekServiceMock.getMealsForDate.and.returnValue(of(plannedMeals));

    await TestBed.configureTestingModule({
      imports: [TodayComponent],
      providers: [
        {
          provide: WeekService,
          useValue: weekServiceMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TodayComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load meals for today', () => {
    expect(weekServiceMock.getMealsForDate).toHaveBeenCalled();

    expect(component.meals.length).toBe(2);
  });

  it('should identify lunch', () => {
    expect(component.lunch?.recipeName).toBe('Poulet et riz');
  });

  it('should identify dinner', () => {
    expect(component.dinner?.recipeName).toBe('Omelette aux légumes');
  });
});
