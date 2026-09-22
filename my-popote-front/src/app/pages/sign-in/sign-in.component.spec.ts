import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { SignInComponent } from './sign-in.component';

/**
 * Vérifie que la connexion affiche un message adapté
 * à la cause de l'échec, sans appel réel à l'API.
 */
describe('SignInComponent', () => {
  let component: SignInComponent;
  let fixture: ComponentFixture<SignInComponent>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [SignInComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SignInComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function submitValidForm(): void {
    component.form.setValue({
      email: 'test@example.com',
      password: 'mot-de-passe-test',
    });

    component.submit();
    fixture.detectChanges();
  }

  it('crée le composant', () => {
    expect(component).toBeTruthy();
  });

  it('indique que les identifiants sont incorrects en cas de réponse 401', () => {
    authService.login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401 })),
    );

    submitValidForm();

    expect(component.errorMessage).toBe(
      'Adresse e-mail ou mot de passe incorrect.',
    );
    expect(component.loading).toBeFalse();
  });

  it('indique un problème de connexion lorsque le réseau ou l’API est inaccessible', () => {
    authService.login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 0 })),
    );

    submitValidForm();

    expect(component.errorMessage).toBe(
      'Impossible de joindre le serveur. Vérifiez votre connexion Internet et réessayez.',
    );
    expect(component.loading).toBeFalse();
  });

  it('indique une erreur temporaire en cas de réponse serveur 500', () => {
    authService.login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 500 })),
    );

    submitValidForm();

    expect(component.errorMessage).toBe(
      'Le service rencontre un problème temporaire. Réessayez dans quelques instants.',
    );
    expect(component.loading).toBeFalse();
  });

  it('ne contacte pas l’API si le formulaire est invalide', () => {
    component.submit();

    expect(authService.login).not.toHaveBeenCalled();
  });
});
