package org.example.mopl.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.mopl.user.dto.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private final UserDto userDto;
    private final String password;
    private final String temporaryPassword;
    private final Instant temporaryPasswordCreatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(userDto == null)
            return List.of();

        return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.getRole()));
    }

    @Override
    public String getUsername() {
        if(userDto == null)
           return "";

        return userDto.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        if(userDto == null)
            return UserDetails.super.isAccountNonLocked();

        return !userDto.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
