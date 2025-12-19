import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/models';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: UserRole[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <aside
      [class]="sidebarClasses"
      class="fixed left-0 top-0 h-screen bg-white border-r border-secondary-200 z-40 transition-all duration-300"
    >
      <!-- Logo -->
      <div class="h-16 flex items-center justify-between px-4 border-b border-secondary-200">
        @if (!collapsed) {
          <span class="text-xl font-bold text-primary-600">Sport SaaS</span>
        } @else {
          <span class="text-xl font-bold text-primary-600">SS</span>
        }
        <button
          (click)="toggle.emit()"
          class="p-1.5 rounded-lg text-secondary-500 hover:bg-secondary-100"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            @if (collapsed) {
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
            } @else {
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
            }
          </svg>
        </button>
      </div>

      <!-- Navigation -->
      <nav class="p-4 space-y-1 overflow-y-auto scrollbar-thin" style="height: calc(100vh - 4rem)">
        @for (item of visibleNavItems; track item.route) {
          <a
            [routerLink]="item.route"
            routerLinkActive="bg-primary-50 text-primary-700 border-r-2 border-primary-600"
            [routerLinkActiveOptions]="{ exact: item.route === '/dashboard' }"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-secondary-600 hover:bg-secondary-100 transition-colors"
            [title]="collapsed ? item.label : ''"
          >
            <span class="text-lg" [innerHTML]="item.icon"></span>
            @if (!collapsed) {
              <span class="text-sm font-medium">{{ item.label }}</span>
            }
          </a>
        }
      </nav>
    </aside>
  `,
})
export class SidebarComponent {
  @Input() collapsed = false;
  @Output() toggle = new EventEmitter<void>();

  private readonly authService = inject(AuthService);

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: '&#x1F4CA;', route: '/dashboard' },
    { label: 'Catalogue', icon: '&#x1F4E6;', route: '/catalog' },
    { label: 'Inventaire', icon: '&#x1F4E6;', route: '/inventory' },
    { label: 'Commandes', icon: '&#x1F6D2;', route: '/orders' },
    { label: 'Locations', icon: '&#x1F4C5;', route: '/rentals' },
    { label: 'Facturation', icon: '&#x1F4B3;', route: '/billing' },
    { label: 'Clients', icon: '&#x1F465;', route: '/customers' },
    { label: 'Utilisateurs', icon: '&#x1F464;', route: '/users', roles: [UserRole.TENANT_ADMIN, UserRole.SUPER_ADMIN] },
    { label: 'Parametres', icon: '&#x2699;', route: '/settings', roles: [UserRole.TENANT_ADMIN, UserRole.SUPER_ADMIN] },
  ];

  get visibleNavItems(): NavItem[] {
    return this.navItems.filter((item) => {
      if (!item.roles) return true;
      return this.authService.hasAnyRole(...item.roles);
    });
  }

  get sidebarClasses(): string {
    return this.collapsed ? 'w-20' : 'w-64';
  }
}
