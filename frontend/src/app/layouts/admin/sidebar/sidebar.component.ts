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
  templateUrl: './sidebar.component.html',
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
