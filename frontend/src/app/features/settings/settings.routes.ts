import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { UserRole } from '../../core/models';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./settings.component').then((m) => m.SettingsComponent),
    canActivate: [roleGuard],
    data: { roles: [UserRole.TENANT_ADMIN, UserRole.SUPER_ADMIN] },
  },
];
