package br.com.safe_line.safeline.modules.report.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReportResponseDTO (
        UUID id,
        String phone,
        LocalDateTime callDate,
        String company,
        String description,
        Boolean status
        ){
}