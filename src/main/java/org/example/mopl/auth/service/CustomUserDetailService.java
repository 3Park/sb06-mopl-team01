package org.example.mopl.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.CustomUserDetails;
import org.example.mopl.user.dto.UserDto;
import org.example.mopl.user.entity.TemporaryPassword;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.TemporaryPasswordRepository;
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
    private final TemporaryPasswordRepository temporaryPasswordRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException(userEmail));
        UserDto userDto = UserDto.builder()
                .user(user)
                .build();

        //임시 비번 로그인일 경우를 대비해 임시비번 정보를 가져옴
        TemporaryPassword temporaryPassword = temporaryPasswordRepository.findByUserEmail(userDto.getEmail()).orElse(null);
        if(temporaryPassword == null)
            return new CustomUserDetails(userDto, user.getPassword(), "",null);

        return new CustomUserDetails(userDto, user.getPassword(), temporaryPassword.getPassword(),temporaryPassword.getCreatedAt());
    }
}
