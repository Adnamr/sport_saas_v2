import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar/sidebar.component';
import { HeaderComponent } from './header/header.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, HeaderComponent],
  templateUrl: './admin-layout.component.html',
})
export class AdminLayoutComponent {
  sidebarCollapsed = signal(false);

  get mainContentClasses(): string {
    return this.sidebarCollapsed()
      ? 'ml-20 transition-all duration-300'
      : 'ml-64 transition-all duration-300';
  }

  toggleSidebar(): void {
    this.sidebarCollapsed.update((v) => !v);
  }
}
