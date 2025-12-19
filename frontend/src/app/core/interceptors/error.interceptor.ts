import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Une erreur est survenue';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = error.error.message;
      } else {
        // Server-side error
        switch (error.status) {
          case 400:
            errorMessage = error.error?.message || 'Requete invalide';
            break;
          case 401:
            errorMessage = 'Non autorise';
            break;
          case 403:
            errorMessage = 'Acces refuse';
            break;
          case 404:
            errorMessage = 'Ressource non trouvee';
            break;
          case 409:
            errorMessage = error.error?.message || 'Conflit de donnees';
            break;
          case 422:
            errorMessage = error.error?.message || 'Donnees invalides';
            break;
          case 500:
            errorMessage = 'Erreur serveur interne';
            break;
          default:
            errorMessage = error.error?.message || `Erreur ${error.status}`;
        }
      }

      console.error('HTTP Error:', {
        status: error.status,
        message: errorMessage,
        url: req.url,
        error: error.error,
      });

      return throwError(() => ({
        status: error.status,
        message: errorMessage,
        errors: error.error?.errors,
        originalError: error,
      }));
    })
  );
};
