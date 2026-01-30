package org.example.mopl.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;

    @Async()
    public void sendResetPasswordMail(String email) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("임시 비밀번호 발급");
        mailMessage.setText("임시 비밀번호");
        mailMessage.setFrom("sbmopl01@gmail.com");
        javaMailSender.send(mailMessage);
    }
}
