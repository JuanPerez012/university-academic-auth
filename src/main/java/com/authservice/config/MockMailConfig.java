package com.authservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MockMailConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockMailConfig.class);

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {

            @Override
            public void send(SimpleMailMessage simpleMessage) {
                LOGGER.info("=== Simulación de envío de correo ===");
                LOGGER.info("Para: {}", String.join(", ", simpleMessage.getTo()));
                LOGGER.info("Asunto: {}", simpleMessage.getSubject());
                LOGGER.info("Mensaje: {}", simpleMessage.getText());
                LOGGER.info("=====================================");
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                for (SimpleMailMessage msg : simpleMessages) {
                    send(msg);
                }
            }

            @Override
            public jakarta.mail.internet.MimeMessage createMimeMessage() {
                throw new UnsupportedOperationException("MimeMessage no soportado en MockMailConfig");
            }

            @Override
            public jakarta.mail.internet.MimeMessage createMimeMessage(java.io.InputStream contentStream) {
                throw new UnsupportedOperationException("MimeMessage no soportado en MockMailConfig");
            }

            @Override
            public void send(jakarta.mail.internet.MimeMessage mimeMessage) {
                throw new UnsupportedOperationException("MimeMessage no soportado en MockMailConfig");
            }

            @Override
            public void send(jakarta.mail.internet.MimeMessage... mimeMessages) {
                throw new UnsupportedOperationException("MimeMessage no soportado en MockMailConfig");
            }
        };
    }
}
