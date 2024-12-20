package com.certidevs.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Envía un email con un adjunto de forma asíncrona.
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String fileName) {
        return CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();

                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(body);

                helper.addAttachment(fileName, new ByteArrayResource(attachment));

                mailSender.send(message);
                log.info("Email enviado a {}", to);
            } catch (MessagingException e) {
                log.error("Error al enviar email: {}", e.getMessage());
            }
        });
    }
}
