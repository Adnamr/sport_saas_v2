import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { StorageService } from '../services/storage.service';

export const tenantInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(StorageService);
  const tenantSlug = storage.getTenantSlug();

  // Add X-Tenant-ID header if tenant is set
  if (tenantSlug) {
    req = req.clone({
      setHeaders: {
        'X-Tenant-ID': tenantSlug,
      },
    });
  }

  return next(req);
};
