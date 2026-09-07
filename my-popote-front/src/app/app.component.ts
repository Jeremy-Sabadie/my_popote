import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { HeaderComponent } from './shared/header/header.component';
import { BottomNavComponent } from './shared/bottom-nav/bottom-nav.component';

/**
 * Composant racine de My Popote.
 *
 * Il contient uniquement la structure générale de l'application.
 * Les écrans métier sont affichés par le router.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    HeaderComponent,
    BottomNavComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
}