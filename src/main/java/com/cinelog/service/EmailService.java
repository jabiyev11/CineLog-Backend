package com.cinelog.service;

import com.cinelog.exception.EmailDeliveryException;
import com.cinelog.security.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void sendOtpEmail(String recipient, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setFrom(mailProperties.from());
            message.setSubject("CineLog email verification code");
            message.setText("Your CineLog verification code is " + code + ". It expires in 10 minutes.");
            mailSender.send(message);
        } catch (MailException ex) {
            throw new EmailDeliveryException("Failed to send verification email.", ex);
        }
    }
}
