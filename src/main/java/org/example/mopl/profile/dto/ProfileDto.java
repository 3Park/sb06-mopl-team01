package org.example.mopl.profile.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonPropertyOrder({"id", "createdAt", "email", "name", "profileImageUrl", "role", "locked", "updatedAt"})
public class ProfileDto {

    private UUID id;
    private LocalDateTime createdAt;
    private String email;
    private String name;
    private String profileImageUrl;
    private String role;
    private Boolean locked;
    private LocalDateTime updatedAt;
}
