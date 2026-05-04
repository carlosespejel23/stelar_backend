package com.education.stelar.kernel.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.education.stelar.kernel.config.AppProperties;

/**
 * Implementación de EmailService via JavaMail (SMTP).
 * Activa en los perfiles dev y prod.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("dev")
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    @Async
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appProperties.getEmail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("Email enviado a: {}", to);
        } catch (MessagingException e) {
            log.error("Error enviando email a {}: {}", to, e.getMessage());
        }
    }
}
