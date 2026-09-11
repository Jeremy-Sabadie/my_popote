import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import {
  AuthResponse,
  ForgotPasswordRequest,
  LoginRequest,
  MessageResponse,
  RegisterRequest,
  ResetPasswordRequest,
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
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, request).pipe(
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
   * Demande l'envoi d'un lien de réinitialisation.
   *
   * L'API retourne volontairement la même réponse
   * que l'adresse existe ou non.
   */
  forgotPassword(request: ForgotPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.authUrl}/forgot-password`,
      request,
    );
  }

  /**
   * Remplace le mot de passe à partir du token temporaire.
   */
  resetPassword(request: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.authUrl}/reset-password`,
      request,
    );
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUser;
  }

  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  logout(): void {
    this.accessToken = null;
    this.currentUser = null;
  }

  /**
   * Centralise l'initialisation de la session.
   */
  private storeSession(response: AuthResponse): void {
    this.accessToken = response.accessToken;
    this.currentUser = response;
  }
}
