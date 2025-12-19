import { Component, Input, Output, EventEmitter, HostListener, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.component.html',
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
