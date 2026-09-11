import { Component, HostListener } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../models/auth.model';

/**
 * En-tête principal de My Popote.
 *
 * Le menu utilisateur reste accessible depuis toutes les pages privées
 * afin de pouvoir consulter son profil ou se déconnecter rapidement.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  menuOpen = false;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  /**
   * Retourne l'utilisateur actuellement connecté.
   *
   * On le récupère à la demande afin que le header reflète
   * toujours l'état courant de la session.
   */
  get user(): AuthResponse | null {
    return this.authService.getCurrentUser();
  }

  /**
   * Indique si une session est actuellement ouverte.
   */
  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  /**
   * Retourne l'initiale utilisée dans l'avatar.
   */
  get userInitial(): string {
    return this.user?.firstName?.trim().charAt(0).toUpperCase() || '?';
  }

  /**
   * Ouvre ou ferme le menu utilisateur.
   */
  toggleUserMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuOpen = !this.menuOpen;
  }

  /**
   * Ferme explicitement le menu.
   */
  closeUserMenu(): void {
    this.menuOpen = false;
  }

  /**
   * Ferme la session puis renvoie vers la page de connexion.
   */
  logout(): void {
    this.closeUserMenu();
    this.authService.logout();
    this.router.navigate(['/sign-in']);
  }

  /**
   * Un clic ailleurs dans la page ferme naturellement le menu.
   */
  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeUserMenu();
  }

  /**
   * La touche Échap permet également de fermer le menu.
   */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeUserMenu();
  }
}
