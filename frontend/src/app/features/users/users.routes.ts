import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';
import { UserRole } from '../../core/models';

export const USERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./users-list.component').then((m) => m.UsersListComponent),
    canActivate: [roleGuard],
    data: { roles: [UserRole.TENANT_ADMIN, UserRole.SUPER_ADMIN] },
  },
];
