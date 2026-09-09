import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../models/auth.model';

/**
 * Tableau de bord affiché après la connexion.
 *
 * Il permet d'accéder rapidement aux principales fonctionnalités
 * et affiche les informations de base de l'utilisateur connecté.
 */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  readonly user: AuthResponse | null;

  constructor(private readonly authService: AuthService) {
    this.user = this.authService.getCurrentUser();
  }
}
