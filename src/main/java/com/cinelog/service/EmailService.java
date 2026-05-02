package com.cinelog.service;

import com.cinelog.exception.EmailDeliveryException;
import com.cinelog.security.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String OTP_CODE_PLACEHOLDER = "{{OTP_CODE}}";
    private static final Resource OTP_EMAIL_TEMPLATE = new ClassPathResource("static/email/otp-verification.html");

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public void sendOtpEmail(String recipient, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setFrom(mailProperties.from());
            helper.setSubject("CineLog email verification code");
            helper.setText(buildPlainTextOtpEmail(code), buildHtmlOtpEmail(code));
            mailSender.send(message);
        } catch (MailException | MessagingException | IOException ex) {
            throw new EmailDeliveryException("Failed to send verification email.", ex);
        }
    }

    private String buildPlainTextOtpEmail(String code) {
        return "Your CineLog verification code is " + code + ". It expires in 10 minutes.";
    }

    private String buildHtmlOtpEmail(String code) throws IOException {
        return OTP_EMAIL_TEMPLATE.getContentAsString(StandardCharsets.UTF_8)
                .replace(OTP_CODE_PLACEHOLDER, code);
    }
}
