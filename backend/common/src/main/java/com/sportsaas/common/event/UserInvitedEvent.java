package com.sportsaas.common.event;

import java.util.UUID;

/**
 * Evenement emis lorsqu'un utilisateur est invite.
 */
public record UserInvitedEvent(
    UUID userId,
    String email,
    String firstName,
    String lastName,
    String invitationToken,
    String tenantName,
    UUID tenantId
) {}
