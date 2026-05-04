package com.education.stelar.kernel.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.education.stelar.kernel.config.AppProperties;

import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

/**
 * Implementación de EmailService via AWS SES v2.
 * Activa únicamente en el perfil prod.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("prod")
public class SesEmailService implements EmailService {

    private final SesV2Client sesV2Client;
    private final AppProperties appProperties;

    @Override
    @Async
    public void send(String to, String subject, String htmlBody) {
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .fromEmailAddress(appProperties.getEmail().getFrom())
                    .destination(Destination.builder().toAddresses(to).build())
                    .content(EmailContent.builder()
                            .simple(Message.builder()
                                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                                    .body(Body.builder()
                                            .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                            .build())
                                    .build())
                            .build())
                    .build();

            sesV2Client.sendEmail(emailRequest);
            log.debug("Email enviado via SES a: {}", to);
        } catch (Exception e) {
            log.error("Error enviando email via SES a {}: {}", to, e.getMessage());
        }
    }
}
