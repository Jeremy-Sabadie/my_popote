import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../models/auth.model';
import { SettingsComponent } from './settings.component';

describe('SettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;
  let router: Router;

  /**
   * Utilisateur simulé conforme à la réponse
   * d'authentification actuelle de l'API.
   */
  const currentUser: AuthResponse = {
    id: 42,
    email: 'jeremy@example.com',
    firstName: 'Jeremy',
    accessToken: 'test-access-token',
  };

  const authServiceMock = {
    getCurrentUser: () => currentUser,
    logout: jasmine.createSpy('logout'),
  };

  beforeEach(async () => {
    authServiceMock.logout.calls.reset();

    await TestBed.configureTestingModule({
      imports: [SettingsComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: authServiceMock,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose the authenticated user', () => {
    expect(component.user).toEqual(currentUser);
  });

  it('should logout and redirect to sign-in', () => {
    spyOn(router, 'navigate');

    component.logout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/sign-in']);
  });
});
