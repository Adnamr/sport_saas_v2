import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (isOpen) {
      <div class="fixed inset-0 z-50 overflow-y-auto" role="dialog" aria-modal="true">
        <!-- Backdrop -->
        <div
          class="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
          (click)="closeOnBackdrop && close.emit()"
        ></div>

        <!-- Modal -->
        <div class="flex min-h-full items-center justify-center p-4">
          <div
            class="relative bg-white rounded-xl shadow-xl transform transition-all w-full"
            [class]="sizeClasses"
          >
            <!-- Header -->
            @if (title || showCloseButton) {
              <div class="flex items-center justify-between px-6 py-4 border-b border-secondary-200">
                @if (title) {
                  <h3 class="text-lg font-semibold text-secondary-900">{{ title }}</h3>
                }
                @if (showCloseButton) {
                  <button
                    type="button"
                    class="text-secondary-400 hover:text-secondary-500 focus:outline-none"
                    (click)="close.emit()"
                  >
                    <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                }
              </div>
            }

            <!-- Body -->
            <div class="px-6 py-4">
              <ng-content></ng-content>
            </div>

            <!-- Footer -->
            @if (showFooter) {
              <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-secondary-200 bg-secondary-50 rounded-b-xl">
                <ng-content select="[modal-footer]"></ng-content>
              </div>
            }
          </div>
        </div>
      </div>
    }
  `,
})
export class ModalComponent {
  @Input() isOpen = false;
  @Input() title = '';
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' | 'full' = 'md';
  @Input() showCloseButton = true;
  @Input() showFooter = true;
  @Input() closeOnBackdrop = true;

  @Output() close = new EventEmitter<void>();

  get sizeClasses(): string {
    const sizes: Record<string, string> = {
      sm: 'max-w-sm',
      md: 'max-w-md',
      lg: 'max-w-lg',
      xl: 'max-w-xl',
      full: 'max-w-4xl',
    };
    return sizes[this.size];
  }
}
