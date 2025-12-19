package com.sportsaas.notification.api;

import com.sportsaas.notification.api.dto.EmailLogResponse;
import com.sportsaas.notification.api.dto.EmailStatsResponse;
import com.sportsaas.notification.domain.EmailLog;
import com.sportsaas.notification.domain.EmailService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "createdAt", source = "createdAt")
    EmailLogResponse toEmailLogResponse(EmailLog emailLog);

    List<EmailLogResponse> toEmailLogResponseList(List<EmailLog> emailLogs);

    default EmailStatsResponse toEmailStatsResponse(EmailService.EmailStats stats) {
        return new EmailStatsResponse(stats.sentLast24h(), stats.failedLast24h(), stats.pending());
    }
}
