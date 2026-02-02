package org.example.mopl.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.exception.AuthErrorCode;
import org.example.mopl.auth.exception.AuthException;
import org.example.mopl.auth.port.AuthPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final AuthPort authPort;
    private final MailSenderService mailService;

    public void sendResetPasswordMail(String email) {
        if(authPort.invalidEmail(email)) {
            throw new AuthException((AuthErrorCode.INVALID_EMAIL));
        }

        mailService.sendResetPasswordMail(email);
    }
}
