import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../models/auth.model';

/**
 * Page de profil de l'utilisateur connecté.
 *
 * Elle affiche les informations principales du compte
 * et permet de fermer proprement la session courante.
 */
@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css',
})
export class SettingsComponent {
  readonly user: AuthResponse | null;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {
    this.user = this.authService.getCurrentUser();
  }

  /**
   * Supprime la session conservée en mémoire puis
   * redirige l'utilisateur vers la page de connexion.
   */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/sign-in']);
  }
}
