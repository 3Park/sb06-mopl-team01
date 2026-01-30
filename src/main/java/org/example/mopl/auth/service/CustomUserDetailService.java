package org.example.mopl.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException(userEmail));
        UserDto userDto = UserDto.builder()
                .user(user)
                .build();

        return new CustomUserDetails(userDto, user.getPassword());
    }
}
