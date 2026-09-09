import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from '../../models/auth.model';
import { environment } from '../../../environments/environment';

/**
 * Centralise l'authentification et les informations
 * de l'utilisateur connecté.
 *
 * Le JWT reste volontairement conservé en mémoire :
 * aucune donnée d'authentification sensible n'est persistée
 * dans le stockage du navigateur.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authUrl = `${environment.apiUrl}/api/auth`;

  private accessToken: string | null = null;
  private currentUser: AuthResponse | null = null;

  constructor(private readonly http: HttpClient) {}

  /**
   * Authentifie l'utilisateur et initialise sa session locale.
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.authUrl}/login`, request)
      .pipe(
        tap((response) => {
          this.storeSession(response);
        }),
      );
  }

  /**
   * Crée le compte puis initialise directement la session.
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.authUrl}/register`, request)
      .pipe(
        tap((response) => {
          this.storeSession(response);
        }),
      );
  }

  /**
   * Fournit le JWT à l'intercepteur HTTP.
   */
  getAccessToken(): string | null {
    return this.accessToken;
  }

  /**
   * Retourne les informations de l'utilisateur connecté.
   */
  getCurrentUser(): AuthResponse | null {
    return this.currentUser;
  }

  /**
   * Indique si une session authentifiée existe actuellement.
   */
  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  /**
   * Déconnecte localement l'utilisateur.
   *
   * Le JWT et les informations associées à la session
   * sont supprimés ensemble.
   */
  logout(): void {
    this.accessToken = null;
    this.currentUser = null;
  }

  /**
   * Centralise l'initialisation de la session pour éviter
   * de dupliquer cette logique entre connexion et inscription.
   */
  private storeSession(response: AuthResponse): void {
    this.accessToken = response.accessToken;
    this.currentUser = response;
  }
}
