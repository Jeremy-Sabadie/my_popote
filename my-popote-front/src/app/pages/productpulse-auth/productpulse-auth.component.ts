import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-productpulse-auth',
  standalone: true,
  template: `
    <main style="max-width:480px;margin:80px auto;padding:24px">
      <h1>ProductPulse</h1>
      <p>{{ message }}</p>
      <button type="button" (click)="openProductPulse()">
        Ouvrir ProductPulse
      </button>
    </main>
  `,
})
export class ProductpulseAuthComponent implements OnInit, OnDestroy {
  private readonly auth = inject(AuthService);
  private readonly productPulseOrigin = 'http://localhost:4200';

  private productPulseWindow: Window | null = null;
  private requestHandled = false;

  message = 'Votre session My Popote est prête.';

  private readonly onMessage = (event: MessageEvent): void => {
    if (event.origin !== this.productPulseOrigin) return;
    if (event.source !== this.productPulseWindow) return;
    if (event.data?.type !== 'PRODUCTPULSE_AUTH_REQUEST') return;
    if (this.requestHandled) return;

    const nonce = event.data?.nonce;

    if (typeof nonce !== 'string' || !/^[a-f0-9]{32}$/.test(nonce)) {
      return;
    }

    const token = this.auth.getAccessToken();

    if (!token) {
      this.message = 'Session expirée. Reconnectez-vous à My Popote.';
      return;
    }

    this.requestHandled = true;

    this.productPulseWindow?.postMessage(
      {
        type: 'PRODUCTPULSE_AUTH_RESPONSE',
        nonce,
        accessToken: token,
      },
      this.productPulseOrigin,
    );

    this.message = 'Connexion transmise à ProductPulse.';
  };

  ngOnInit(): void {
    window.addEventListener('message', this.onMessage);
  }

  openProductPulse(): void {
    if (!this.auth.getAccessToken()) {
      this.message = 'Reconnectez-vous à My Popote.';
      return;
    }

    this.requestHandled = false;

    this.productPulseWindow = window.open(
      `${this.productPulseOrigin}/auth`,
      '_blank',
    );

    if (!this.productPulseWindow) {
      this.message = 'Autorisez les fenêtres contextuelles.';
    }
  }

  ngOnDestroy(): void {
    window.removeEventListener('message', this.onMessage);
  }
}
