import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';

import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { HistoryComponent } from './pages/history/history.component';
import { HomeComponent } from './pages/home/home.component';
import { RecipesComponent } from './pages/recipes/recipes.component';
import { ResetPasswordComponent } from './pages/reset-password/reset-password.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { ShoppingComponent } from './pages/shopping/shopping.component';
import { SignInComponent } from './pages/sign-in/sign-in.component';
import { SignUpComponent } from './pages/sign-up/sign-up.component';
import { TodayComponent } from './pages/today/today.component';
import { WeekComponent } from './pages/week/week.component';

/**
 * Routes principales de My Popote.
 *
 * Les écrans d'authentification et de récupération du compte
 * restent publics. Toutes les données personnelles et les
 * fonctionnalités métier nécessitent une session authentifiée.
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full',
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
    path: 'forgot-password',
    component: ForgotPasswordComponent,
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
  },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [authGuard],
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
    redirectTo: 'home',
  },
];
