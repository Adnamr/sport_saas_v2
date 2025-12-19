package com.sportsaas.notification.infra;

import com.sportsaas.common.event.UserInvitedEvent;
import com.sportsaas.notification.domain.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener pour les evenements utilisateur.
 * Gere l'envoi automatique d'emails suite aux actions utilisateur.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Envoie un email d'invitation lorsqu'un utilisateur est invite.
     */
    @Async
    @EventListener
    public void handleUserInvited(UserInvitedEvent event) {
        log.info("Handling UserInvitedEvent for user: {} ({})", event.email(), event.userId());

        try {
            String invitationLink = frontendUrl + "/auth/accept-invitation?token=" + event.invitationToken();

            emailService.sendInvitationEmail(
                    event.email(),
                    event.firstName() + " " + event.lastName(),
                    "L'equipe " + event.tenantName(),
                    event.tenantName(),
                    invitationLink
            );

            log.info("Invitation email sent successfully to: {}", event.email());
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", event.email(), e.getMessage());
        }
    }
}
