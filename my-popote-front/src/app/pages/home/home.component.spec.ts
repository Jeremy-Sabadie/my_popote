import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../models/auth.model';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  /**
   * Utilisateur simulé correspondant au contrat actuel
   * retourné par l'API d'authentification.
   */
  const currentUser: AuthResponse = {
    id: 42,
    email: 'jeremy@example.com',
    firstName: 'Jeremy',
    accessToken: 'test-access-token',
  };

  const authServiceMock = {
    getCurrentUser: () => currentUser,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: authServiceMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose the authenticated user', () => {
    expect(component.user).toEqual(currentUser);
  });
});
