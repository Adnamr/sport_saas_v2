import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';
import { ApiService } from './api.service';
import { StorageService } from './storage.service';
import { User, UserRole, LoginRequest, LoginResponse, RegisterRequest } from '../models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly storage = inject(StorageService);
  private readonly router = inject(Router);

  private readonly currentUser = signal<User | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUser() !== null);
  readonly userRole = computed(() => this.currentUser()?.role || null);

  constructor() {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const user = this.storage.getUser<User>();
    const token = this.storage.getAccessToken();
    if (user && token) {
      this.currentUser.set(user);
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.api.post<LoginResponse>('/auth/login', credentials).pipe(
      tap((response) => {
        this.storage.setAccessToken(response.accessToken);
        this.storage.setRefreshToken(response.refreshToken);
        this.storage.setUser(response.user);
        this.currentUser.set(response.user);
      })
    );
  }

  register(data: RegisterRequest): Observable<LoginResponse> {
    return this.api.post<LoginResponse>('/auth/register', data).pipe(
      tap((response) => {
        this.storage.setAccessToken(response.accessToken);
        this.storage.setRefreshToken(response.refreshToken);
        this.storage.setUser(response.user);
        this.currentUser.set(response.user);
      })
    );
  }

  logout(): void {
    this.storage.clear();
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  refreshToken(): Observable<LoginResponse | null> {
    const refreshToken = this.storage.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return of(null);
    }

    return this.api.post<LoginResponse>('/auth/refresh', { refreshToken }).pipe(
      tap((response) => {
        this.storage.setAccessToken(response.accessToken);
        this.storage.setRefreshToken(response.refreshToken);
      }),
      catchError(() => {
        this.logout();
        return of(null);
      })
    );
  }

  forgotPassword(email: string): Observable<{ message: string }> {
    return this.api.post<{ message: string }>('/auth/forgot-password', { email });
  }

  resetPassword(token: string, password: string): Observable<{ message: string }> {
    return this.api.post<{ message: string }>('/auth/reset-password', { token, password });
  }

  verifyEmail(token: string): Observable<{ message: string }> {
    return this.api.get<{ message: string }>('/auth/verify-email', { token });
  }

  completeInvitation(token: string, password: string): Observable<{ message: string }> {
    return this.api.post<{ message: string }>('/users/complete-invitation', { token, password });
  }

  hasRole(role: UserRole): boolean {
    return this.currentUser()?.role === role;
  }

  hasAnyRole(...roles: UserRole[]): boolean {
    const currentRole = this.currentUser()?.role;
    return currentRole ? roles.includes(currentRole) : false;
  }

  isAdmin(): boolean {
    return this.hasAnyRole(UserRole.SUPER_ADMIN, UserRole.TENANT_ADMIN);
  }

  getAccessToken(): string | null {
    return this.storage.getAccessToken();
  }
}
