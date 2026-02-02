package org.example.mopl.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mopl.user.entity.TemporaryPassword;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.repository.TemporaryPasswordRepository;
import org.example.mopl.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemporaryPasswordService {
    private final TemporaryPasswordRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void save(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        TemporaryPassword preTemporaryPassword = repository.findByUserEmail(email).orElse(null);
        if(preTemporaryPassword != null)
        {
            repository.delete(preTemporaryPassword);
        }

        TemporaryPassword temporaryPassword = TemporaryPassword.builder()
                .password(passwordEncoder.encode(password))
                .user(user)
                .build();

        repository.save(temporaryPassword);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFromUserByEmail(String email) {
        if(StringUtils.hasText(email) == false)
            return;

        repository.deleteByUserEmail(email);
    }
}
