import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { LucideChefHat, LucideHistory, LucideSettings } from '@lucide/angular';

/**
 * En-tête principal de My Popote.
 *
 * Sur mobile, il affiche principalement l'identité de l'application.
 * Sur desktop, il participe également à la navigation latérale.
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    LucideChefHat,
    LucideHistory,
    LucideSettings,
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {}
