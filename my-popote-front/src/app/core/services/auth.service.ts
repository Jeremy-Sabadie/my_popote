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
 * Centralise l'authentification de l'utilisateur.
 *
 * Le JWT est volontairement conservé en mémoire pour éviter
 * de stocker durablement le token dans le navigateur.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authUrl = `${environment.apiUrl}/api/auth`;

  private accessToken: string | null = null;

  constructor(private readonly http: HttpClient) {}

  /**
   * Authentifie l'utilisateur puis conserve le JWT
   * retourné par l'API.
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, request).pipe(
      tap((response) => {
        this.accessToken = response.accessToken;
      }),
    );
  }

  /**
   * Crée le compte puis conserve directement le JWT.
   *
   * L'API connecte ainsi l'utilisateur immédiatement
   * après son inscription.
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.authUrl}/register`, request)
      .pipe(
        tap((response) => {
          this.accessToken = response.accessToken;
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
   * Indique si un JWT est actuellement présent.
   */
  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  /**
   * Supprime l'authentification locale.
   */
  logout(): void {
    this.accessToken = null;
  }
}
