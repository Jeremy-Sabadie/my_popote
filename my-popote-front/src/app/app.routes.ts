import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

import { HomeComponent } from './pages/home/home.component';
import { SignInComponent } from './pages/sign-in/sign-in.component';
import { SignUpComponent } from './pages/sign-up/sign-up.component';
import { TodayComponent } from './pages/today/today.component';
import { WeekComponent } from './pages/week/week.component';
import { ShoppingComponent } from './pages/shopping/shopping.component';
import { RecipesComponent } from './pages/recipes/recipes.component';
import { HistoryComponent } from './pages/history/history.component';
import { SettingsComponent } from './pages/settings/settings.component';

/**
 * Routes principales de My Popote.
 *
 * Les pages publiques restent accessibles sans compte.
 * Les fonctionnalités personnelles nécessitent un utilisateur connecté.
 */
export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
  },
  {
    path: 'sign-in',
    component: SignInComponent,
  },
  {
    path: 'sign-up',
    component: SignUpComponent,
  },
  {
    path: 'today',
    component: TodayComponent,
    canActivate: [authGuard],
  },
  {
    path: 'week',
    component: WeekComponent,
    canActivate: [authGuard],
  },
  {
    path: 'shopping',
    component: ShoppingComponent,
    canActivate: [authGuard],
  },
  {
    path: 'recipes',
    component: RecipesComponent,
    canActivate: [authGuard],
  },
  {
    path: 'history',
    component: HistoryComponent,
    canActivate: [authGuard],
  },
  {
    path: 'settings',
    component: SettingsComponent,
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
