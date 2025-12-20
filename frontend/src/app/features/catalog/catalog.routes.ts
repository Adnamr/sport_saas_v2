import { Routes } from '@angular/router';

export const CATALOG_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./products/products-list.component').then((m) => m.ProductsListComponent),
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./products/product-form.component').then((m) => m.ProductFormComponent),
  },
  {
    path: 'categories',
    loadComponent: () =>
      import('./categories/categories-list.component').then((m) => m.CategoriesListComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./products/product-form.component').then((m) => m.ProductFormComponent),
  },
];
