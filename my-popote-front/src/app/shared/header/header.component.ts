
import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../models/auth.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit, OnDestroy {
  menuOpen = false;
  isAdmin = false;
  roleLoading = true;

  private readonly subscriptions = new Subscription();
  private roleSubscription?: Subscription;

  private readonly productPulseApiUrl = 'http://localhost:8080';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly http: HttpClient,
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.authService.session$.subscribe((session) => {
        this.roleSubscription?.unsubscribe();
        this.isAdmin = false;
        this.roleLoading = true;

        if (!session) {
          this.roleLoading = false;
          return;
        }

        this.loadProductPulseRole(session.accessToken);
      }),
    );
  }

  ngOnDestroy(): void {
    this.roleSubscription?.unsubscribe();
    this.subscriptions.unsubscribe();
  }

  get user(): AuthResponse | null {
    return this.authService.getCurrentUser();
  }

  get isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  get userInitial(): string {
    return this.user?.firstName?.trim().charAt(0).toUpperCase() || '?';
  }

  private loadProductPulseRole(token: string): void {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });

    this.roleSubscription = this.http
      .get<{ role: 'ADMIN' | 'USER' }>(
        `${this.productPulseApiUrl}/api/auth/me`,
        { headers },
      )
      .subscribe({
        next: (response) => {
          this.isAdmin = response.role === 'ADMIN';
          this.roleLoading = false;
        },
        error: (error) => {
          console.error(
            'Impossible de récupérer le rôle ProductPulse :',
            error.status,
          );
          this.isAdmin = false;
          this.roleLoading = false;
        },
      });
  }

  toggleUserMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuOpen = !this.menuOpen;
  }

  closeUserMenu(): void {
    this.menuOpen = false;
  }

  logout(): void {
    this.closeUserMenu();
    this.authService.logout();
    this.router.navigate(['/sign-in']);
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.closeUserMenu();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeUserMenu();
  }
}
