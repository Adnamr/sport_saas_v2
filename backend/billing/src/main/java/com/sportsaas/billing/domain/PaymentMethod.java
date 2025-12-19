package com.sportsaas.billing.domain;

/**
 * Methodes de paiement acceptees.
 */
public enum PaymentMethod {
    /** Especes */
    CASH,
    /** Carte bancaire */
    CARD,
    /** Virement bancaire */
    BANK_TRANSFER,
    /** Cheque */
    CHECK,
    /** PayPal */
    PAYPAL,
    /** Stripe */
    STRIPE,
    /** Autre */
    OTHER
}
