import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-secondary-50">
      <div class="text-center">
        <h1 class="text-6xl font-bold text-red-600">403</h1>
        <h2 class="mt-4 text-2xl font-semibold text-secondary-900">
          Acces non autorise
        </h2>
        <p class="mt-2 text-secondary-600">
          Vous n'avez pas les permissions necessaires pour acceder a cette page.
        </p>
        <a
          routerLink="/dashboard"
          class="mt-6 inline-block px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
        >
          Retour au dashboard
        </a>
      </div>
    </div>
  `,
})
export class UnauthorizedComponent {}
