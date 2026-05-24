package com.education.stelar.kernel.email;

/**
 * Contrato genérico para el envío de emails.
 * El módulo identity (y futuros módulos) usa esta interface,
 * nunca directamente el SDK de email/SES.
 */
public interface EmailService {

    void send(String to, String subject, String htmlBody);
}
