import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <h1 class="text-2xl font-bold text-secondary-900 mb-6">Mon Profil</h1>

      <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6 max-w-2xl">
        @if (user) {
          <div class="flex items-center gap-6 mb-8">
            <div class="w-20 h-20 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-2xl font-bold">
              {{ user.firstName.charAt(0) }}{{ user.lastName.charAt(0) }}
            </div>
            <div>
              <h2 class="text-xl font-semibold text-secondary-900">
                {{ user.firstName }} {{ user.lastName }}
              </h2>
              <p class="text-secondary-600">{{ user.email }}</p>
              <span class="inline-block mt-2 px-3 py-1 bg-primary-100 text-primary-700 text-sm rounded-full">
                {{ user.role }}
              </span>
            </div>
          </div>

          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-secondary-600">Prenom</label>
                <p class="mt-1 text-secondary-900">{{ user.firstName }}</p>
              </div>
              <div>
                <label class="block text-sm font-medium text-secondary-600">Nom</label>
                <p class="mt-1 text-secondary-900">{{ user.lastName }}</p>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-secondary-600">Email</label>
              <p class="mt-1 text-secondary-900">{{ user.email }}</p>
            </div>
          </div>
        } @else {
          <p class="text-secondary-600">Chargement...</p>
        }
      </div>
    </div>
  `,
})
export class ProfileComponent {
  private readonly authService = inject(AuthService);

  get user() {
    return this.authService.user();
  }
}
