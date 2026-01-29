package org.example.mopl.user.dto;

import lombok.Builder;
import lombok.Getter;

import org.example.mopl.user.entity.User;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserDto {
    private UUID id;
    private Instant createdAt;
    private String email;
    private String name;
    private String profileImageUrl;
    private String role;
    private boolean locked;

    @Builder
    public UserDto(User user)
    {
        //PJG 프로필 관련 설정 추가 필요
        if(user == null || user.getUserRoles() ==null
           || user.getUserRoles().isEmpty())
            throw new IllegalArgumentException("User and Profile are both null");

        this.id = user.getUuid();
        this.createdAt = user.getCreatedAt();
        this.email = user.getEmail();
        //this.name = user.getProfile().getName();
        //this.profileImageUrl = user.getProfile().getProfileImageUrl();
        this.role = user.getUserRoles().get(0).getRole().getName().name();
        this.locked = user.getLocked();
    }
}
