package br.com.safe_line.safeline.modules.report.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReportRequestDTO(

     String phone,
     LocalDateTime callDate,
     String company,
     String description

    ){}

