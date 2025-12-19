import { Routes } from '@angular/router';

export const BILLING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./billing-list.component').then((m) => m.BillingListComponent),
  },
];
