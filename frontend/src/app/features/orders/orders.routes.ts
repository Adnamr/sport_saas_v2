import { Routes } from '@angular/router';

export const ORDERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./orders-list.component').then((m) => m.OrdersListComponent),
  },
];
