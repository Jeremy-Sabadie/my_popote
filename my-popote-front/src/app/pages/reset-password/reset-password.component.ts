import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

/**
 * Permet de définir un nouveau mot de passe
 * à partir du token temporaire reçu par e-mail.
 */
@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css',
})
export class ResetPasswordComponent {
  loading = false;
  success = false;
  errorMessage = '';

  private readonly token: string;

  readonly form;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly route: ActivatedRoute,
  ) {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';

    this.form = this.formBuilder.nonNullable.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmation: ['', Validators.required],
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }

    const { password, confirmation } = this.form.getRawValue();

    if (password !== confirmation) {
      this.errorMessage = 'Les deux mots de passe doivent être identiques.';
      return;
    }

    if (!this.token) {
      this.errorMessage =
        'Ce lien de réinitialisation est invalide ou incomplet.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService
      .resetPassword({
        token: this.token,
        password,
      })
      .subscribe({
        next: () => {
          this.loading = false;
          this.success = true;
        },
        error: () => {
          this.loading = false;
          this.errorMessage =
            'Ce lien est invalide ou a expiré. Veuillez demander un nouveau lien.';
        },
      });
  }
}
