import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

/**
 * Formulaire de connexion à My Popote.
 */
@Component({
  selector: 'app-sign-in',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.css',
})
export class SignInComponent {
  loading = false;
  errorMessage = '';

  readonly form;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.form = this.formBuilder.nonNullable.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  /**
   * Connecte l'utilisateur puis l'envoie vers son tableau de bord.
   */
  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/home']);
      },
      error: (error: unknown) => {
        this.loading = false;
        this.errorMessage = this.getLoginErrorMessage(error);
      },
    });
  }

  /**
   * Traduit les erreurs HTTP en messages compréhensibles.
   * Ne présente pas systématiquement une panne du serveur
   * comme une erreur de mot de passe.
   */
  private getLoginErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'La connexion a échoué. Veuillez réessayer.';
    }

    switch (error.status) {
      case 0:
        return 'Impossible de joindre le serveur. Vérifiez votre connexion Internet et réessayez.';
      case 400:
      case 401:
        return 'Adresse e-mail ou mot de passe incorrect.';
      case 429:
        return 'Trop de tentatives de connexion. Patientez quelques instants avant de réessayer.';
      default:
        if (error.status >= 500) {
          return 'Le service rencontre un problème temporaire. Réessayez dans quelques instants.';
        }

        return 'La connexion a échoué. Veuillez réessayer.';
    }
  }
}
