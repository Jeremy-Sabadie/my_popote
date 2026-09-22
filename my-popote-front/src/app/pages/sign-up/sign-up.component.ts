import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

/**
 * Formulaire de création d'un compte My Popote.
 */
@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css',
})
export class SignUpComponent {
  loading = false;
  errorMessage = '';
  emailAlreadyUsed = false;

  readonly form;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.form = this.formBuilder.nonNullable.group({
      firstName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
    });

    // Efface l'erreur de doublon lorsque l'utilisateur corrige son adresse.
    this.form.controls.email.valueChanges.subscribe(() => {
      this.emailAlreadyUsed = false;
    });
  }

  /**
   * Crée le compte puis ouvre directement le tableau de bord.
   */
  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.emailAlreadyUsed = false;

    this.authService.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/home']);
      },
      error: (error: unknown) => {
        this.loading = false;

        if (!(error instanceof HttpErrorResponse)) {
          this.errorMessage = 'La création du compte a échoué. Réessayez.';
          return;
        }

        // Comportement actuel de l'API : 404 + ce message précis.
        if (
          error.status === 404 &&
          error.error?.message === 'Email already in use'
        ) {
          this.emailAlreadyUsed = true;
          return;
        }

        if (error.status === 0) {
          this.errorMessage =
            'Impossible de joindre le serveur. Vérifiez votre connexion Internet et réessayez.';
        } else if (error.status === 400) {
          this.errorMessage =
            'Certaines informations sont invalides. Vérifiez les champs du formulaire.';
        } else if (error.status >= 500) {
          this.errorMessage =
            'Le service rencontre un problème temporaire. Réessayez dans quelques instants.';
        } else {
          this.errorMessage = 'La création du compte a échoué. Réessayez.';
        }
      },
    });
  }
}
