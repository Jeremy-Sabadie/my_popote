import { Routes } from '@angular/router';

import { TodayComponent } from './pages/today/today.component';
import { WeekComponent } from './pages/week/week.component';
import { ShoppingComponent } from './pages/shopping/shopping.component';
import { RecipesComponent } from './pages/recipes/recipes.component';
import { HistoryComponent } from './pages/history/history.component';
import { SettingsComponent } from './pages/settings/settings.component';
import { HomeComponent } from './pages/home/home.component';
import { SignInComponent } from './pages/sign-in/sign-in.component';
import { SignUpComponent } from './pages/sign-up/sign-up.component';

/**
 * Routes principales de My Popote.
 *
 * Les guards d'authentification seront activés lorsque
 * nous connecterons réellement le frontend à l'API.
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
  },

  {
    path: 'week',
    component: WeekComponent,
  },

  {
    path: 'shopping',
    component: ShoppingComponent,
  },

  {
    path: 'recipes',
    component: RecipesComponent,
  },

  {
    path: 'history',
    component: HistoryComponent,
  },

  {
    path: 'settings',
    component: SettingsComponent,
  },

  {
    path: '**',
    redirectTo: '',
  },
];
