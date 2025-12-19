import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { StorageService } from '../services/storage.service';

export const tenantGuard: CanActivateFn = () => {
  const storage = inject(StorageService);
  const router = inject(Router);

  const tenantSlug = storage.getTenantSlug();

  if (tenantSlug) {
    return true;
  }

  // Redirect to tenant selection or login
  router.navigate(['/auth/login']);
  return false;
};
