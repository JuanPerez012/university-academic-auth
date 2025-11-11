package com.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MockMailConfig {
    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public void send(SimpleMailMessage simpleMessage) {
                System.out.println("=== Simulación de envío de correo ===");
                System.out.println("Para: " + String.join(", ", simpleMessage.getTo()));
                System.out.println("Asunto: " + simpleMessage.getSubject());
                System.out.println("Mensaje: " + simpleMessage.getText());
                System.out.println("=====================================");
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                for (SimpleMailMessage msg : simpleMessages) send(msg);
            }

            @Override public jakarta.mail.internet.MimeMessage createMimeMessage() { return null; }
            @Override public jakarta.mail.internet.MimeMessage createMimeMessage(java.io.InputStream contentStream) { return null; }
            @Override public void send(jakarta.mail.internet.MimeMessage mimeMessage) {}
            @Override public void send(jakarta.mail.internet.MimeMessage... mimeMessages) {}
        };
    }
}