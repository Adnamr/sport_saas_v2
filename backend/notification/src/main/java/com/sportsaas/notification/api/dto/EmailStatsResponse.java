package com.sportsaas.notification.api.dto;

/**
 * DTO pour les statistiques d'envoi d'emails.
 */
public record EmailStatsResponse(
    long sentLast24h,
    long failedLast24h,
    long pending
) {}
