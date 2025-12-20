import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { tenantGuard } from './core/guards/tenant.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      ),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/register/register.component').then(
        (m) => m.RegisterComponent
      ),
  },
  {
    path: 'forgot-password',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password.component').then(
        (m) => m.ForgotPasswordComponent
      ),
  },
  {
    path: 'reset-password',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password.component').then(
        (m) => m.ResetPasswordComponent
      ),
  },
  {
    path: '',
    loadComponent: () =>
      import('./layouts/admin/admin-layout.component').then(
        (m) => m.AdminLayoutComponent
      ),
    canActivate: [authGuard, tenantGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'catalog',
        loadChildren: () =>
          import('./features/catalog/catalog.routes').then(
            (m) => m.CATALOG_ROUTES
          ),
      },
      {
        path: 'inventory',
        loadChildren: () =>
          import('./features/inventory/inventory.routes').then(
            (m) => m.INVENTORY_ROUTES
          ),
      },
      {
        path: 'orders',
        loadChildren: () =>
          import('./features/orders/orders.routes').then(
            (m) => m.ORDERS_ROUTES
          ),
      },
      {
        path: 'rentals',
        loadChildren: () =>
          import('./features/rentals/rentals.routes').then(
            (m) => m.RENTALS_ROUTES
          ),
      },
      {
        path: 'billing',
        loadChildren: () =>
          import('./features/billing/billing.routes').then(
            (m) => m.BILLING_ROUTES
          ),
      },
      {
        path: 'customers',
        loadChildren: () =>
          import('./features/customers/customers.routes').then(
            (m) => m.CUSTOMERS_ROUTES
          ),
      },
      {
        path: 'users',
        loadChildren: () =>
          import('./features/users/users.routes').then((m) => m.USERS_ROUTES),
      },
      {
        path: 'settings',
        loadChildren: () =>
          import('./features/settings/settings.routes').then(
            (m) => m.SETTINGS_ROUTES
          ),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/profile.component').then(
            (m) => m.ProfileComponent
          ),
      },
    ],
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./features/auth/unauthorized/unauthorized.component').then(
        (m) => m.UnauthorizedComponent
      ),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./features/not-found/not-found.component').then(
        (m) => m.NotFoundComponent
      ),
  },
];
