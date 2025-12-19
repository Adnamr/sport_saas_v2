import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <h1 class="text-2xl font-bold text-secondary-900 mb-6">Dashboard</h1>

      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-secondary-600">Chiffre d'affaires</p>
              <p class="text-2xl font-bold text-secondary-900 mt-1">--</p>
            </div>
            <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <span class="text-2xl">&#x1F4B0;</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-secondary-600">Commandes</p>
              <p class="text-2xl font-bold text-secondary-900 mt-1">--</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <span class="text-2xl">&#x1F6D2;</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-secondary-600">Clients</p>
              <p class="text-2xl font-bold text-secondary-900 mt-1">--</p>
            </div>
            <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
              <span class="text-2xl">&#x1F465;</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-secondary-600">Produits</p>
              <p class="text-2xl font-bold text-secondary-900 mt-1">--</p>
            </div>
            <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
              <span class="text-2xl">&#x1F4E6;</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Placeholder content -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <h2 class="text-lg font-semibold text-secondary-900 mb-4">Revenus</h2>
          <div class="h-64 flex items-center justify-center text-secondary-400">
            Graphique a venir
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm border border-secondary-200 p-6">
          <h2 class="text-lg font-semibold text-secondary-900 mb-4">Commandes recentes</h2>
          <div class="h-64 flex items-center justify-center text-secondary-400">
            Liste a venir
          </div>
        </div>
      </div>
    </div>
  `,
})
export class DashboardComponent {}
