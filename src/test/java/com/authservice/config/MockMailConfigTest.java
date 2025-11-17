package com.authservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;

class MockMailConfigTest {

    @Test
    void javaMailSender_shouldPrintSingleMessageToLogger() {
        MockMailConfig config = new MockMailConfig();
        JavaMailSender mailSender = config.javaMailSender();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("user@example.com");
        message.setSubject("Prueba");
        message.setText("Hola mundo");

        assertDoesNotThrow(() -> mailSender.send(message));
    }

    @Test
    void javaMailSender_shouldHandleMultipleMessagesAndThrowOnMimeMethods() {
        MockMailConfig config = new MockMailConfig();
        JavaMailSender mailSender = config.javaMailSender();

        SimpleMailMessage msg1 = new SimpleMailMessage();
        msg1.setTo("one@example.com");
        msg1.setSubject("Asunto 1");
        msg1.setText("Mensaje 1");

        SimpleMailMessage msg2 = new SimpleMailMessage();
        msg2.setTo("two@example.com");
        msg2.setSubject("Asunto 2");
        msg2.setText("Mensaje 2");

        assertDoesNotThrow(() -> mailSender.send(msg1, msg2));

        assertThrows(UnsupportedOperationException.class, mailSender::createMimeMessage);
        assertThrows(UnsupportedOperationException.class,
                () -> mailSender.createMimeMessage(new java.io.ByteArrayInputStream(new byte[0])));
        assertThrows(UnsupportedOperationException.class,
                () -> mailSender.send((jakarta.mail.internet.MimeMessage) null));
        assertThrows(UnsupportedOperationException.class,
                () -> mailSender.send((jakarta.mail.internet.MimeMessage[]) null));
    }
}
