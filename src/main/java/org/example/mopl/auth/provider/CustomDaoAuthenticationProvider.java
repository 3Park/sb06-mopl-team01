package org.example.mopl.auth.provider;

import org.example.mopl.auth.CustomUserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        String normalPassword = authentication.getCredentials().toString();
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        boolean normalPasswordMatches = getPasswordEncoder().matches(normalPassword, customUserDetails.getPassword());
        if(normalPasswordMatches == false) {
            Instant temporaryPasswordCreatedAt = customUserDetails.getTemporaryPasswordCreatedAt();
            String temporaryPassword = customUserDetails.getTemporaryPassword();

            if(temporaryPasswordCreatedAt == null
                    || StringUtils.hasText(temporaryPassword) == false
                    || Instant.now().minus(3, ChronoUnit.MINUTES).isAfter(temporaryPasswordCreatedAt)) {
                throw new BadCredentialsException("Bad credentials");
            }

            boolean tempPasswordMatches = getPasswordEncoder().matches(normalPassword,
                    temporaryPassword);

            if(tempPasswordMatches == false) {
                throw new BadCredentialsException("Bad credentials");
            }
        }
    }
}
