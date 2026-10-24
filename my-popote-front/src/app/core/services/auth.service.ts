
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

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
 * Le JWT reste volontairement conservé en mémoire.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly authUrl = `${environment.apiUrl}/api/auth`;

  private accessToken: string | null = null;
  private currentUser: AuthResponse | null = null;

  private readonly sessionSubject =
    new BehaviorSubject<AuthResponse | null>(null);

  readonly session$ = this.sessionSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, request).pipe(
      tap((response) => this.storeSession(response)),
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.authUrl}/register`, request)
      .pipe(
        tap((response) => this.storeSession(response)),
      );
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.authUrl}/forgot-password`,
      request,
    );
  }

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
    this.sessionSubject.next(null);
  }

  private storeSession(response: AuthResponse): void {
    this.accessToken = response.accessToken;
    this.currentUser = response;
    this.sessionSubject.next(response);
  }
}
