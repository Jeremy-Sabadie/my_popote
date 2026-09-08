import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { RecipesComponent } from './recipes.component';

/**
 * Tests de la page des recettes.
 *
 * HttpClientTesting évite tout véritable appel vers l'API
 * pendant l'exécution des tests unitaires.
 */
describe('RecipesComponent', () => {
  let component: RecipesComponent;
  let fixture: ComponentFixture<RecipesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecipesComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(RecipesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
