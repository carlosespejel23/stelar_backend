package com.education.stelar.kernel.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementación de EmailService para tests — simplemente loguea el contenido.
 * Activa en el perfil test para no requerir servidor SMTP.
 */
@Slf4j
@Service
@Profile("test")
public class ConsoleEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String htmlBody) {
        log.info("[EMAIL SIMULADO] Para: {} | Asunto: {}", to, subject);
        log.debug("[EMAIL SIMULADO] Cuerpo: {}", htmlBody);
    }
}
