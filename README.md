# university-academic-auth

Microservicio responsable de la **autenticación, autorización y gestión de usuarios** dentro del sistema académico distribuido. Este módulo maneja la creación de cuentas, inicio y cierre de sesión, recuperación de contraseñas, emisión y validación de tokens JWT, así como la integración con el API Gateway y Eureka Server.

---

## Descripción del repositorio

Este repositorio contiene la implementación del **Auth Service**, encargado de centralizar los procesos de seguridad y autenticación del sistema.

Responsabilidades principales:

* Gestionar la **autenticación de usuarios** mediante JWT (JSON Web Tokens).  
* Administrar la **autorización por roles** (Administrador, Docente, Estudiante).  
* Validar credenciales y emitir tokens firmados.  
* Controlar la expiración, validación y revocación de tokens.  
* Proporcionar endpoints seguros para login, registro, recuperación de contraseña y validación de sesión.  
* Integrarse con el **API Gateway** y el **Eureka Server** para enrutamiento y descubrimiento de servicios.  

Tecnologías principales:

* Java 21  
* Spring Boot 3.5.5  
* Spring Security  
* JWT (JSON Web Token)  
* Spring Cloud Netflix Eureka Client  
* Base de datos: PostgreSQL  
* Build: Maven  
* Tests: JUnit 5  
* Contenerización prevista (Docker)

---

## Estructura del repositorio

```
/
├─ src/
│  ├─ main/
│  │  ├─ java/         → código fuente (configuración de seguridad, controladores, servicios)
│  │  └─ resources/    → application.yml
│  └─ test/            → pruebas unitarias con JUnit 5
├─ Dockerfile
├─ pom.xml
├─ .gitignore
└─ README.md
```

---

## Políticas de rama y flujo de trabajo

Ramas principales:

* **main** → Código listo para producción.  
* **develop** → Rama de integración.  
* **qa** → Rama de validación de calidad.  
* **feature/** → Ramas de desarrollo de funcionalidades específicas.  
* **release/** → Ramas de estabilización previas al merge en `main`.

Flujo de trabajo recomendado:  
`feature/*` → PR → `develop` → `qa` → `release/*` → `main`

---

## Configuración de seguridad (SecurityConfig)

El módulo utiliza **Spring Security con JWT** para proteger los endpoints y validar accesos.  
Ejemplo de configuración básica (`SecurityConfig.java`):

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/forgot-password").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

---

## Endpoints principales

| Método | Endpoint | Descripción |
|--------|-----------|--------------|
| `POST` | `/api/v1/auth/register` | Registrar un nuevo usuario. |
| `POST` | `/api/v1/auth/login` | Autenticar usuario y generar token JWT. |
| `POST` | `/api/v1/auth/refresh-token` | Renovar un token JWT expirado. |
| `POST` | `/api/v1/auth/forgot-password` | Enviar enlace de recuperación de contraseña. |
| `POST` | `/api/v1/auth/reset-password` | Restablecer contraseña con token válido. |
| `GET` | `/api/v1/auth/me` | Obtener información del usuario autenticado. |

---

## Integración con Eureka y Gateway

En el archivo `application.yml`:

```yaml
spring:
  application:
    name: auth-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

Configuración en el **API Gateway** (para enrutar peticiones al AuthService):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/v1/auth/**
```

---

## Ejecución local

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/<ORG>/auth-service.git
   ```
2. Compilar el proyecto:
   ```bash
   mvn clean install
   ```
3. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
4. Verificar en Eureka:
   ```
   http://localhost:8761
   ```

---

## Dockerización

Ejemplo de `Dockerfile`:

```dockerfile
FROM openjdk:21-jdk
WORKDIR /app
COPY target/auth-service.jar auth-service.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/auth-service.jar"]
```

Comandos para construir y ejecutar:

```bash
docker build -t auth-service .
docker run -p 8081:8081 auth-service
```

---

## Monitoreo y despliegue

El servicio expone un endpoint de salud en:  
```
/actuator/health
```

Y puede integrarse con Prometheus o Grafana para monitoreo de disponibilidad.

---

## Enlaces relevantes

* Repositorio base: [university-academic-tracker](https://github.com/IAndresPH/university-academic-tracker.git)  
* Documentación JWT: [https://jwt.io](https://jwt.io)  
* Spring Security Docs: [https://spring.io/projects/spring-security](https://spring.io/projects/spring-security)  
* Spring Cloud Gateway Docs: [https://spring.io/projects/spring-cloud-gateway](https://spring.io/projects/spring-cloud-gateway)
