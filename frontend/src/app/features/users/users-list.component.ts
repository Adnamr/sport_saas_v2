import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-users-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <h1 class="text-2xl font-bold text-secondary-900 mb-6">Utilisateurs</h1>
      <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
        <p class="text-secondary-600">Gestion des utilisateurs a venir</p>
      </div>
    </div>
  `,
})
export class UsersListComponent {}
