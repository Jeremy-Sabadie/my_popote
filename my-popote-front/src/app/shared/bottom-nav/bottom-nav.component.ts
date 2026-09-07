import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import {
  LucideBookOpen,
  LucideCalendarDays,
  LucideCalendarRange,
  LucideShoppingBasket,
} from '@lucide/angular';

/**
 * Navigation métier principale de My Popote.
 *
 * Sur mobile, elle reste en bas de l'écran pour être facilement
 * accessible au pouce.
 *
 * Sur desktop, elle devient une navigation verticale.
 */
@Component({
  selector: 'app-bottom-nav',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    LucideBookOpen,
    LucideCalendarDays,
    LucideCalendarRange,
    LucideShoppingBasket,
  ],
  templateUrl: './bottom-nav.component.html',
  styleUrl: './bottom-nav.component.css',
})
export class BottomNavComponent {}
