package com.authservice;

import com.authservice.config.JwtAuthenticationFilter;
import com.authservice.config.JwtConfig;
import com.authservice.config.MockMailConfig;
import com.authservice.config.SecurityConfig;
import com.authservice.config.WebClientConfig;
import com.authservice.controller.AuthController;
import com.authservice.controller.PasswordResetController;
import com.authservice.repository.PasswordResetTokenRepository;
import com.authservice.repository.UserRepository;
import com.authservice.repository.UserRoleRepository;
import com.authservice.service.AuthService;
import com.authservice.service.PasswordResetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceApplicationTests {

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserRoleRepository userRoleRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    void contextLoads() {
        assertNotNull(context, "El ApplicationContext no debería ser nulo");
    }

    @DisplayName("Todos los beans clave del módulo AUTH deben estar presentes en el contexto")
    @ParameterizedTest(name = "Bean presente por tipo: {0}")
    @MethodSource("beanTypes")
    void beansShouldBePresentByType(Class<?> type) {
        Object bean = assertDoesNotThrow(
                () -> context.getBean(type),
                () -> "No se pudo resolver bean de tipo: " + type.getName()
        );

        assertNotNull(bean, "El bean no debería ser nulo");
        assertTrue(
                type.isInstance(bean) || type.isAssignableFrom(bean.getClass()),
                "El bean obtenido no es del tipo esperado (puede ser un proxy)"
        );
    }

    static Stream<Class<?>> beanTypes() {
        return Stream.of(
                AuthController.class,
                PasswordResetController.class,

                AuthService.class,
                PasswordResetService.class,

                SecurityConfig.class,
                JwtConfig.class,
                JwtAuthenticationFilter.class,
                WebClientConfig.class,
                MockMailConfig.class,

                UserRepository.class,
                UserRoleRepository.class,
                PasswordResetTokenRepository.class
        );
    }
}
