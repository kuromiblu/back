package br.com.safe_line.safeline.modules.user.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponseDTO(String name, String email, LocalDateTime createdAt){
}
