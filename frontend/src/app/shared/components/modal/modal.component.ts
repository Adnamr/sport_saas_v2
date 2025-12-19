import { Component, Input, Output, EventEmitter, HostListener, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (isOpen) {
      <div class="fixed inset-0 z-50 overflow-y-auto" role="dialog" aria-modal="true" [attr.aria-labelledby]="title ? 'modal-title' : null">
        <!-- Backdrop -->
        <div
          class="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
          (click)="closeOnBackdrop && onClose()"
        ></div>

        <!-- Modal -->
        <div class="flex min-h-full items-center justify-center p-4">
          <div
            #modalContent
            class="relative bg-white rounded-xl shadow-xl transform transition-all w-full"
            [class]="sizeClasses"
            tabindex="-1"
          >
            <!-- Header -->
            @if (title || showCloseButton) {
              <div class="flex items-center justify-between px-6 py-4 border-b border-secondary-200">
                @if (title) {
                  <h3 id="modal-title" class="text-lg font-semibold text-secondary-900">{{ title }}</h3>
                }
                @if (showCloseButton) {
                  <button
                    type="button"
                    class="text-secondary-400 hover:text-secondary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 rounded"
                    (click)="onClose()"
                    aria-label="Fermer"
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
export class ModalComponent implements AfterViewInit, OnDestroy {
  @Input() isOpen = false;
  @Input() title = '';
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' | 'full' = 'md';
  @Input() showCloseButton = true;
  @Input() showFooter = true;
  @Input() closeOnBackdrop = true;
  @Input() closeOnEscape = true;

  @Output() close = new EventEmitter<void>();

  private previousActiveElement: HTMLElement | null = null;

  constructor(private readonly elementRef: ElementRef) {}

  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent): void {
    if (this.isOpen && this.closeOnEscape) {
      event.preventDefault();
      this.onClose();
    }
  }

  ngAfterViewInit(): void {
    if (this.isOpen) {
      this.trapFocus();
    }
  }

  ngOnDestroy(): void {
    this.restoreFocus();
  }

  onClose(): void {
    this.restoreFocus();
    this.close.emit();
  }

  private trapFocus(): void {
    this.previousActiveElement = document.activeElement as HTMLElement;
    const modalContent = this.elementRef.nativeElement.querySelector('[tabindex="-1"]');
    if (modalContent) {
      setTimeout(() => modalContent.focus(), 0);
    }
  }

  private restoreFocus(): void {
    if (this.previousActiveElement) {
      this.previousActiveElement.focus();
      this.previousActiveElement = null;
    }
  }

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
