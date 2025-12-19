import { Component, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  @Output() toggleSidebar = new EventEmitter<void>();

  private readonly authService = inject(AuthService);

  showUserMenu = false;

  get userName(): string {
    const user = this.authService.user();
    return user ? `${user.firstName} ${user.lastName}` : '';
  }

  get userInitials(): string {
    const user = this.authService.user();
    if (!user) return '';
    return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
  }

  get userRole(): string {
    const user = this.authService.user();
    return user?.role || '';
  }

  logout(): void {
    this.authService.logout();
  }
}
