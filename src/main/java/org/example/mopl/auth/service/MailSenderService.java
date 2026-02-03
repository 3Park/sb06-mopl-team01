package org.example.mopl.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.event.TemporaryPasswordIssuedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender javaMailSender;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${spring.mail.username}")
    private String senderEmailAddress;

    @Async()
    public void sendResetPasswordMail(String email) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("[Mopl] 임시 비밀번호 발급");
        SecureRandom random = new SecureRandom();
        String code = String.format("%06d", random.nextInt(1_000_000));
        mailMessage.setText(String.format("임시 비밀번호 입니다.\n3분안에 로그인 후 비밀번호를 변경해 주세요.\n임시 로그인 코드 : %s",code));
        mailMessage.setFrom(senderEmailAddress);
        javaMailSender.send(mailMessage);

        eventPublisher.publishEvent(TemporaryPasswordIssuedEvent
                .builder()
                .temporaryPassword(code)
                .receiverEmail(email)
                .build());
    }
}
