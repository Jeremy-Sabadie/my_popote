
import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { environment } from '../../../environments/environment';
import { ShoppingService } from './shopping.service';

describe('ShoppingService', () => {
  let service: ShoppingService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(ShoppingService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Aucun appel HTTP ne doit rester sans réponse à la fin d'un test.
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve the shopping list history', () => {
    const expectedHistory = [
      {
        id: 12,
        mealPlanId: 8,
        weekStartDate: '2026-09-14',
      },
    ];

    let receivedHistory: typeof expectedHistory | undefined;

    service.getShoppingLists().subscribe((history) => {
      receivedHistory = history;
    });

    const request = httpTestingController.expectOne(
      `${environment.apiUrl}/api/shopping-lists`,
    );

    expect(request.request.method).toBe('GET');

    request.flush(expectedHistory);

    expect(receivedHistory).toEqual(expectedHistory);
  });
});
