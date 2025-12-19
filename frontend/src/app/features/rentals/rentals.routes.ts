import { Routes } from '@angular/router';

export const RENTALS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./rentals-list.component').then((m) => m.RentalsListComponent),
  },
];
