import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { RecipeService } from './recipe.service';
import { environment } from '../../../environments/environment';

/**
 * Tests du service chargé des échanges avec l'API des recettes.
 *
 * Les tests utilisent l'URL définie par l'environnement Angular chargé
 * par Karma. Ils ne dépendent donc pas d'une adresse locale codée en dur.
 */
describe('RecipeService', () => {
  let service: RecipeService;
  let httpTestingController: HttpTestingController;

  const recipesUrl = `${environment.apiUrl}/api/recipes`;
  const tagsUrl = `${environment.apiUrl}/api/tags`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(RecipeService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should request recipes without tag filter', () => {
    service.getRecipes().subscribe();

    const request = httpTestingController.expectOne(recipesUrl);

    expect(request.request.method).toBe('GET');

    request.flush([]);
  });

  it('should send selected tag ids when filtering recipes', () => {
    service.getRecipes([2, 5]).subscribe();

    const request = httpTestingController.expectOne(
      (request) =>
        request.url === recipesUrl && request.params.get('tagIds') === '2,5',
    );

    expect(request.request.method).toBe('GET');

    request.flush([]);
  });

  it('should request available tags', () => {
    service.getTags().subscribe();

    const request = httpTestingController.expectOne(tagsUrl);

    expect(request.request.method).toBe('GET');

    request.flush([]);
  });
});
