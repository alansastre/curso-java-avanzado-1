package com.certidevs.dto;

import java.time.LocalDateTime;

public record ReportCreationRequest(
    LocalDateTime startDate,
    LocalDateTime endDate,
    Long companyId,
    String email
) {
}
