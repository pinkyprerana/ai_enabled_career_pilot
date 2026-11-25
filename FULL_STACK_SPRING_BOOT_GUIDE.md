# FULL STACK SOFTWARE ENGINEERING MASTER GUIDE

### Enterprise Spring Boot 3/4 REST API Architecture, Security (JWT/BCrypt), JPA Persistence & Token-by-Token Reference


# Module 1: Full-Stack System Architecture & Request Lifecycle


In enterprise full-stack development, modern applications separate client-side presentation from server-side domain logic. The backend operates as a secure, stateless REST API handling authentication, authorization, business validation, and database persistence.


## 1.1 The Enterprise Request-Response Pipeline


When a web or mobile client makes an HTTP request to this Spring Boot application, it travels through distinct architectural boundaries:

- **1. Client Gateway** The client (React, Vue, mobile) sends an HTTP request with JSON payload and an optional 'Authorization: Bearer <token>' header.
- **2. Security Filter Chain** The request hits the Servlet Filter chain. JwtAuthenticationFilter inspects the Bearer token, validates cryptographic signature and expiration, and sets the authenticated principal in SecurityContextHolder.
- **3. DispatcherServlet & Routing** DispatcherServlet receives the filtered request, consults HandlerMapping, and delegates execution to the matched Controller method.
- **4. DTO & Validation Layer** Spring validates incoming JSON against DTO constraints (@Valid, @NotBlank, @Email). Violations trigger MethodArgumentNotValidException caught by GlobalExceptionHandler.
- **5. Service Layer (Domain Logic)** The Controller delegates business execution to the Service layer. The Service enforces business invariants (such as preventing duplicate email registration).
- **6. Persistence Layer (JPA / Hibernate)** The Service invokes the Spring Data JPA Repository, which automatically maps domain calls to optimized SQL queries via Hibernate ORM against the database (H2 in memory).
- **7. Response Transformation** The result is converted from an internal Entity to a clean Response DTO (excluding sensitive fields like passwords), returned as HTTP 200/201/204 with clean JSON.

> **Stateless Architecture Principle**: REST APIs should never maintain server-side HTTP Sessions (HttpSession). Instead, each request carries its own cryptographic proof of identity (JWT). This makes the backend horizontally scalable across multiple server instances without sticky sessions or centralized session caches.


# Module 2: Project Build & Runtime Configuration


## 2.1 build.gradle (Build & Dependency Engine)


The Gradle build script defines JVM toolchains, framework dependencies, compiler arguments, and test executors.


```groovy
plugins {
	id 'java'
	id 'org.springframework.boot' version '4.1.2'
	id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-webmvc'
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-security'
	implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	runtimeOnly 'com.h2database:h2'
	implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
	runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
	runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

	testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
	testImplementation 'org.springframework.boot:spring-boot-starter-security-test'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
	implementation 'org.springframework.boot:spring-boot-starter-actuator'
}

tasks.withType(JavaCompile).configureEach {
	options.compilerArgs << '-parameters'
}

tasks.named('test') {
	useJUnitPlatform()
}
```


| Token / Configuration | Type | Full-Stack Software Engineering Explanation |
| --- | --- | --- |
| id 'org.springframework.boot' | Gradle Plugin | Applies the Spring Boot Gradle plugin, providing dependency management, executable JAR packaging, and bootRun tasks. |
| id 'io.spring.dependency-management' | Gradle Plugin | Manages compatible dependency versions transitively based on the Spring Boot Bill of Materials (BOM), eliminating manual version numbers. |
| JavaLanguageVersion.of(23) | JVM Toolchain | Pins the Java language version to 23, guaranteeing reproducible builds across development, CI/CD, and production environments. |
| spring-boot-starter-webmvc | Starter Dependency | Includes Spring MVC, Jackson (JSON serializer/deserializer), and an embedded Apache Tomcat web server. |
| spring-boot-starter-data-jpa | Starter Dependency | Brings in Spring Data JPA and Hibernate ORM for database abstraction and automatic repository query generation. |
| spring-boot-starter-security | Starter Dependency | Provides authentication, authorization, BCrypt password hashing, and filter chain protection. |
| spring-boot-starter-validation | Starter Dependency | Provides Jakarta Bean Validation (Hibernate Validator) for validating request DTOs (@NotBlank, @Email, @Size). |
| com.h2database:h2 | Runtime Dependency | Zero-configuration embedded SQL database used for rapid local development and automated integration testing. |
| io.jsonwebtoken:jjwt-api / impl / jackson | JWT Library | Standard Java JWT library (0.12.x) for creating, signing, and parsing cryptographically verified JSON Web Tokens. |
| options.compilerArgs << '-parameters' | Compiler Argument | CRITICAL: Forces javac to retain parameter names in bytecode. In Spring 6 / Boot 3+, missing this flag causes HTTP 500 on @PathVariable without explicit names! |



## 2.2 application.yaml (Application Configuration)


The application.yaml file configures externalized properties including the database connection, Hibernate behavior, web consoles, and JWT encryption keys.


```yaml
spring:
  application:
    name: learningSpringBootApp
  datasource:
    url: jdbc:h2:mem:studentdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driverClassName: org.h2.Driver
    username: sa
    password: 
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:google-client-id}
            client-secret: ${GOOGLE_CLIENT_SECRET:google-client-secret}
            scope:
              - profile
              - email
          github:
            client-id: ${GITHUB_CLIENT_ID:github-client-id}
            client-secret: ${GITHUB_CLIENT_SECRET:github-client-secret}
            scope:
              - read:user
              - user:email
          twitter:
            client-id: ${TWITTER_CLIENT_ID:twitter-client-id}
            client-secret: ${TWITTER_CLIENT_SECRET:twitter-client-secret}
            client-name: Twitter
            provider: twitter
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope:
              - tweet.read
              - users.read
              - offline.access
        provider:
          twitter:
            authorization-uri: https://twitter.com/i/oauth2/authorize
            token-uri: https://api.twitter.com/2/oauth2/token
            user-info-uri: https://api.twitter.com/2/users/me
            user-name-attribute: data

jwt:
  secret: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
  expiration-ms: ${JWT_EXPIRATION_MS:86400000}

payment:
  provider: razorpay

debug: false
```


| Configuration Property | Environment Role | Full-Stack Architectural Purpose |
| --- | --- | --- |
| spring.application.name | Metadata | Identifies the microservice in logs, APM traces (Prometheus/Zipkin), and service registries (Eureka/Consul). |
| spring.datasource.url | JDBC Connection | Specifies the JDBC URL for H2 in-memory mode ('jdbc:h2:mem:studentdb'). DB_CLOSE_DELAY=-1 prevents DB drop between connections. |
| spring.jpa.hibernate.ddl-auto | Schema DDL | 'update' automatically creates/updates database tables based on @Entity classes upon application startup. |
| spring.h2.console.enabled | Developer Tool | Enables the web-based SQL administration console accessible at http://localhost:8080/h2-console. |
| jwt.secret | Security Key | 256-bit hexadecimal cryptographic secret key used to compute the HMAC-SHA256 signature of every issued JWT. |
| jwt.expiration-ms | Token Lifetime | Token validity in milliseconds (86,400,000 ms = 24 hours). After expiration, requests receive HTTP 401 Unauthorized. |
| payment.provider | Feature Flag | Controls conditional bean registration (@ConditionalOnProperty) to toggle payment processors dynamically. |



# Module 3: Domain Models & Database Persistence Layer


## 3.1 StudentEntity.java (Student JPA Entity)


Represents the persistent data schema for students stored in the relational database table 'students'.


```java
package com.example.learningSpringBootApp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "students", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    public StudentEntity() {
    }

    public StudentEntity(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public StudentEntity(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```


| Token / Keyword | Category | Technical Explanation |
| --- | --- | --- |
| @Entity | JPA Annotation | Marks the Java class as a persistent entity to be managed by the Hibernate EntityManager. |
| @Table(name = 'students') | JPA Annotation | Maps the entity to the 'students' SQL table and declares table-level constraints. |
| uniqueConstraints = @UniqueConstraint(columnNames = 'email') | Database DDL | Enforces a database-level UNIQUE constraint on the email column. Prevents race conditions from inserting duplicate emails. |
| @Id | JPA Annotation | Designates the 'id' field as the primary key of the table. |
| @GeneratedValue(strategy = GenerationType.IDENTITY) | Primary Key Strategy | Uses the database auto-increment identity column to assign IDs sequentially. |
| @Column(nullable = false, unique = true) | Column Metadata | Generates NOT NULL and UNIQUE column constraints in the generated SQL DDL schema. |
| public StudentEntity() {} | Constructor | Mandatory no-argument constructor required by Hibernate to instantiate entities via Java reflection. |



## 3.2 UserEntity.java (User Authentication Entity)


Represents registered system users who have credentials to log into the platform.


```java
package com.example.learningSpringBootApp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String authProvider = "LOCAL";

    public UserEntity() {
    }

    public UserEntity(String name, String email, String password, String role) {
        this(name, email, password, role, "LOCAL");
    }

    public UserEntity(String name, String email, String password, String role, String authProvider) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authProvider = authProvider != null ? authProvider : "LOCAL";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }
}
```


| Token / Keyword | Category | Technical Explanation |
| --- | --- | --- |
| password | Entity Field | Stores the BCrypt-hashed password digest. Plain-text passwords must NEVER be saved in this field. |
| role | Entity Field | Stores the user authority string (e.g., 'ROLE_USER', 'ROLE_ADMIN') for Role-Based Access Control (RBAC). |
| unique = true | Column Constraint | Guarantees that two users cannot register with the identical email address. |



## 3.3 StudentRepository.java & UserRepository.java


Spring Data JPA repositories abstract database access into type-safe Java interfaces.


```java
package com.example.learningSpringBootApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningSpringBootApp.entity.StudentEntity;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    Optional<StudentEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
```


| Repository Method | Generated SQL Equivalent | Full-Stack Purpose |
| --- | --- | --- |
| extends JpaRepository<StudentEntity, Long> | Base Interface | Inherits CRUD operations (save, findById, findAll, deleteById, count) without writing any boilerplate code. |
| existsByEmail(String email) | SELECT COUNT(*) > 0 FROM students WHERE email = ? | Fast O(1) indexed existence check used during creation to block duplicate emails. |
| existsByEmailAndIdNot(String email, Long id) | SELECT COUNT(*) > 0 FROM students WHERE email = ? AND id != ? | Ensures that when updating student X, the new email isn't already used by student Y! |
| findByEmail(String email) | SELECT * FROM students WHERE email = ? LIMIT 1 | Fetches entity by email returning an Optional<StudentEntity> to prevent NullPointerExceptions. |



# Module 4: Data Transfer Objects (DTOs) & Validation Layer


Professional full-stack architecture enforces a strict separation between internal database Entities and external API DTOs. Direct entity exposure causes security vulnerabilities (over-posting, mass assignment) and tight coupling.


## 4.1 StudentRequest.java & StudentResponse.java


```java
package com.example.learningSpringBootApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email
) {}
```


```java
package com.example.learningSpringBootApp.dto;

public record StudentResponse(
    Long id,
    String name,
    String email
) {}
```


| Token / Annotation | Category | Technical Explanation |
| --- | --- | --- |
| record | Java 16+ Keyword | Creates an immutable data carrier with transparent constructor, getters, equals(), hashCode(), and toString(). |
| @NotBlank(message = '...') | Bean Validation | Ensures the string is not null and contains at least one non-whitespace character. |
| @Email(message = '...') | Bean Validation | Validates that the string conforms to RFC 5322 email address format specifications. |
| StudentResponse | Output DTO | Safely exposes only the public attributes (id, name, email) of the student to the client. |



## 4.2 SignupRequest, LoginRequest & AuthResponse


```java
package com.example.learningSpringBootApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String password
) {}
```


```java
package com.example.learningSpringBootApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be blank")
    String password
) {}
```


```java
package com.example.learningSpringBootApp.dto;

public record AuthResponse(
    String token,
    String type,
    Long id,
    String name,
    String email,
    String role
) {
    public AuthResponse(String token, Long id, String name, String email, String role) {
        this(token, "Bearer", id, name, email, role);
    }
}
```


| Token / Annotation | Category | Technical Explanation |
| --- | --- | --- |
| @Size(min = 6) | Validation Rule | Enforces a minimum password length of 6 characters for credential security. |
| AuthResponse | Response DTO | Returns the signed JWT token, token type ('Bearer'), user ID, name, email, and assigned role. |
| Bearer | HTTP Auth Scheme | Standard OAuth 2.0 / RFC 6750 token scheme where the bearer of the token possesses access rights. |



## 4.3 ErrorResponse.java


Unified error structure returned across all failed requests, complying with REST best practices.


```java
package com.example.learningSpringBootApp.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
```


# Module 5: Centralized Exception Handling Tier


Spring's Controller Advice pattern decouples error presentation from business logic.


## 5.1 Custom Business Exceptions


```java
package com.example.learningSpringBootApp.exception;

public class DuplicateResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }
}
```


```java
package com.example.learningSpringBootApp.exception;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```


## 5.2 GlobalExceptionHandler.java


```java
package com.example.learningSpringBootApp.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.learningSpringBootApp.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid email or password",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Access denied",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```


| ExceptionHandler Method | HTTP Status | Scenario Handled |
| --- | --- | --- |
| @RestControllerAdvice | N/A | Registers a global interceptor around all @RestController endpoints. |
| handleDuplicateResource() | 409 Conflict | Triggered when duplicate student/user email is registered. |
| handleResourceNotFound() | 404 Not Found | Triggered when a requested student ID or user does not exist in the database. |
| handleValidationErrors() | 400 Bad Request | Catches MethodArgumentNotValidException and converts field errors into a clean JSON map. |
| handleBadCredentials() | 401 Unauthorized | Triggered when login credentials (email or password) do not match. |
| handleAccessDenied() | 403 Forbidden | Triggered when an authenticated user attempts to access an endpoint beyond their role. |
| handleGeneralException() | 500 Internal Error | Fallback safety net catching unexpected server errors with structured JSON. |



# Module 6: Security, JWT & Authentication Architecture


## 6.1 JwtUtils.java (Token Generation & Verification)


```java
package com.example.learningSpringBootApp.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }
}
```


| Method / Token | Category | Full-Stack Security Role |
| --- | --- | --- |
| Keys.hmacShaKeyFor() | Cryptography | Derives a SecretKey instance from the 256-bit UTF-8 secret for HMAC-SHA signing. |
| Jwts.builder().subject().claim() | JWT Creation | Embeds the user email as subject, assigns custom claims ('role'), sets issue/expiration timestamps, and signs the compact JWT. |
| Jwts.parser().verifyWith() | JWT Verification | Cryptographically verifies that the token was signed with our secret key and has not been tampered with. |
| extractClaim() | Claim Extraction | Extracts identity metadata (subject email, role, expiration) from the verified token payload. |
| validateToken() | Validation Logic | Verifies both that the username matches userDetails and that the expiration date is in the future. |



## 6.2 CustomUserDetailsService.java


```java
package com.example.learningSpringBootApp.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
```


## 6.3 JwtAuthenticationFilter.java


```java
package com.example.learningSpringBootApp.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils,
                                  UserDetailsService userDetailsService,
                                  TokenBlacklistService tokenBlacklistService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (tokenBlacklistService.isBlacklisted(token)) {
                filterChain.doFilter(request, response);
                return;
            }
            try {
                String email = jwtUtils.extractEmail(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    if (jwtUtils.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }
}
```


| Token / Method | Category | Execution Detail |
| --- | --- | --- |
| OncePerRequestFilter | Base Class | Guarantees this filter executes exactly once per incoming HTTP request. |
| request.getHeader('Authorization') | HTTP Header | Reads the Bearer authentication token sent by frontend clients. |
| SecurityContextHolder.getContext().setAuthentication() | Spring Security | Sets the authenticated principal for the current thread execution, granting access to protected routes. |



## 6.4 SecurityConfig.java


```java
package com.example.learningSpringBootApp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.learningSpringBootApp.security.oauth2.CustomOAuth2UserService;
import com.example.learningSpringBootApp.security.oauth2.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          CustomOAuth2UserService customOAuth2UserService,
                          OAuth2AuthenticationSuccessHandler oauth2SuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/", "/index.html", "/favicon.ico", "/css/**", "/js/**", "/hello", "/static/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/students/**", "/api/jobs/**", "/api/profile/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oauth2SuccessHandler)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```


| Security Rule | Method Call | Architectural Reasoning |
| --- | --- | --- |
| CSRF Disabled | csrf(csrf -> csrf.disable()) | Safe and required for stateless REST APIs using Bearer tokens (browser does not auto-attach Bearer tokens like cookies). |
| Stateless Sessions | SessionCreationPolicy.STATELESS | Prevents server from creating JSESSIONID cookies. Scales horizontally. |
| Public Endpoints | requestMatchers('/api/auth/**').permitAll() | Allows unauthenticated users to access sign-up, login, and H2 console. |
| Protected Endpoints | requestMatchers('/api/students/**').authenticated() | Blocks unauthenticated access; requires valid JWT token. |
| Filter Ordering | addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) | Executes JWT parsing BEFORE Spring's default username/password filter. |
| BCryptPasswordEncoder | @Bean PasswordEncoder | Industry standard adaptive cryptographic hashing function with built-in salting against rainbow table attacks. |



# Module 7: Business Services & REST Controllers


## 7.1 AuthService.java & AuthController.java


```java
package com.example.learningSpringBootApp.auth;

import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.AuthResponse;
import com.example.learningSpringBootApp.dto.LoginRequest;
import com.example.learningSpringBootApp.dto.SignupRequest;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.exception.DuplicateResourceException;
import com.example.learningSpringBootApp.repository.UserRepository;
import com.example.learningSpringBootApp.security.JwtUtils;
import com.example.learningSpringBootApp.security.TokenBlacklistService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       AuthenticationManager authenticationManager,
                       TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email '" + request.email() + "' is already registered");
        }

        String role = "ROLE_USER";
        UserEntity user = new UserEntity(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                role
        );
        UserEntity savedUser = userRepository.save(user);

        String token = jwtUtils.generateToken(savedUser.getEmail(), savedUser.getRole());
        return new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Date expiration = jwtUtils.extractExpiration(token);
                tokenBlacklistService.blacklistToken(token, expiration);
            } catch (Exception ignored) {
            }
        }
        SecurityContextHolder.clearContext();
    }
}
```


```java
package com.example.learningSpringBootApp.auth;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.learningSpringBootApp.dto.AuthResponse;
import com.example.learningSpringBootApp.dto.LoginRequest;
import com.example.learningSpringBootApp.dto.SignupRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController { 

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
```


## 7.2 StudentService.java


```java
package com.example.learningSpringBootApp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.StudentRequest;
import com.example.learningSpringBootApp.dto.StudentResponse;
import com.example.learningSpringBootApp.entity.StudentEntity;
import com.example.learningSpringBootApp.exception.DuplicateResourceException;
import com.example.learningSpringBootApp.exception.ResourceNotFoundException;
import com.example.learningSpringBootApp.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StudentResponse findById(Long id) {
        StudentEntity student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToResponse(student);
    }

    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Student with email '" + request.email() + "' already exists");
        }
        StudentEntity entity = new StudentEntity(request.name(), request.email());
        StudentEntity saved = studentRepository.save(entity);
        return mapToResponse(saved);
    }

    public StudentResponse update(Long id, StudentRequest request) {
        StudentEntity existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (studentRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Student with email '" + request.email() + "' already exists");
        }

        existing.setName(request.name());
        existing.setEmail(request.email());
        StudentEntity updated = studentRepository.save(existing);
        return mapToResponse(updated);
    }

    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    private StudentResponse mapToResponse(StudentEntity entity) {
        return new StudentResponse(entity.getId(), entity.getName(), entity.getEmail());
    }
}
```


| Service Method | Business Rule Enforced | Behavior |
| --- | --- | --- |
| create() | No Duplicate Email | Calls studentRepository.existsByEmail(). If true, aborts immediately with DuplicateResourceException (409). |
| update() | No Duplicate Email on Edit | Calls existsByEmailAndIdNot() to ensure updated email is not held by another student. |
| findById() | Entity Existence | Uses orElseThrow() to produce clean ResourceNotFoundException (404) if ID is absent. |
| delete() | Idempotent / Valid Deletion | Verifies existence with existsById() before deleting, preventing silent failures. |



## 7.3 StudentController.java (The Bug Fix Explained)


```java
package com.example.learningSpringBootApp;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.learningSpringBootApp.dto.StudentRequest;
import com.example.learningSpringBootApp.dto.StudentResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> findAll() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public StudentResponse findById(@PathVariable("id") Long id) {
        return studentService.findById(id);
    }

    @PostMapping
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest student) {
        StudentResponse created = studentService.create(student);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable("id") Long id, @Valid @RequestBody StudentRequest student) {
        return studentService.update(id, student);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```


> **The 500 Error Bug & How It Was Resolved**: In Spring Framework 6 (Spring Boot 3+), LocalVariableTableParameterNameDiscoverer was deprecated and removed. When using bare '@PathVariable Long id' without explicit names, the compiler generated bytecode with 'arg0'. Spring could not resolve {id} and threw IllegalArgumentException resulting in HTTP 500! The fix was two-fold: 1) Explicitly naming @PathVariable('id') Long id, and 2) Adding '-parameters' to build.gradle.


| Endpoint | HTTP Method | Status Code | Description |
| --- | --- | --- | --- |
| /api/students | GET | 200 OK | Returns list of all students. |
| /api/students/{id} | GET | 200 OK | Returns student by ID. Throws 404 if not found. |
| /api/students | POST | 201 Created | Creates student. Returns Location header and created student body. |
| /api/students/{id} | PUT | 200 OK | Updates existing student data with duplicate check. |
| /api/students/{id} | DELETE | 204 No Content | Deletes student record. |



# Module 8: Extensibility, Inversion of Control & Testing


## 8.1 Payment Service & Conditional Beans


```java
package com.example.learningSpringBootApp;

public interface PaymentService {
    String pay();
}
```


```java
package com.example.learningSpringBootApp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "razorpay")
public class RazorpayPaymentService implements PaymentService{

    @Override
    public String pay(){
        String payment = "Razorpay Payment";
        System.out.println("Payment from: "+payment);
        return payment;
    }
}
```


```java
package com.example.learningSpringBootApp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

//@Component
//@Service
//@RestController
//@Controller
@Repository
@ConditionalOnProperty(name = "payment.provider", havingValue = "stripe")
public class StripePaymentService implements PaymentService {

    @Override
    public String pay() {
        String payment = "Stripe Payment";
        System.out.println("Payment from: " + payment);
        return payment;
    }
}
```


| Token / Concept | Category | Explanation |
| --- | --- | --- |
| PaymentService | Java Interface | Defines the contract adhering to the Dependency Inversion Principle (High-level modules should depend on abstractions). |
| @ConditionalOnProperty | Spring Boot Annotation | Registers the bean ONLY if 'payment.provider' in application.yaml matches the specified value. |
| Polymorphic Injection | Spring DI | The consumer (LearningSpringBootAppApplication) injects PaymentService without knowing whether Stripe or Razorpay is active! |



## 8.2 LearningSpringBootAppApplication.java


```java
package com.example.learningSpringBootApp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningSpringBootAppApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(LearningSpringBootAppApplication.class, args);
	}

	private final PaymentService paymentService;

	public LearningSpringBootAppApplication(PaymentService payment) {
		this.paymentService = payment;
	}

	@Override
	public void run(String... args) throws Exception {
		String payment = paymentService.pay();
		System.out.println("Payment done! "+payment);
	}
}
```


## 8.3 LearningSpringBootAppApplicationTests.java


```java
package com.example.learningSpringBootApp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LearningSpringBootAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
```


# Module 9: Full-Stack Frontend Integration (React / Next.js)


To complete the full-stack architecture, here is the production-ready TypeScript/React client code showing how modern frontends interact with this API.


```typescript
// api/client.ts - Axios instance with automatic JWT Bearer token injection
import axios from "axios";

export const apiClient = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

// Request Interceptor: Attach JWT token to every protected request
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("auth_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response Interceptor: Handle 401 Unauthorized (Auto-Logout) and 409 Conflict
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("auth_token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
```


# Module 10: Top 10 Full-Stack Software Engineering Interview Questions


| Question | Core Engineering Concept Tested | Ideal Answer Summary |
| --- | --- | --- |
| 1. What is Inversion of Control (IoC) and Dependency Injection (DI)? | Spring Core | IoC inverts control of object lifecycle from the developer to the Spring container. DI is the mechanism where the container injects dependencies via constructors. |
| 2. Why use DTOs instead of exposing JPA Entities directly? | Security & API Design | Prevents mass assignment vulnerabilities, avoids Hibernate lazy-loading serialization errors, and decouples the API contract from the DB schema. |
| 3. How does JWT authentication differ from Session cookies? | Security & Scalability | Sessions store state on the server (requiring shared cache or sticky sessions). JWTs are cryptographically signed self-contained tokens verified on every request, making APIs stateless and horizontally scalable. |
| 4. What caused the 500 error in @PathVariable Long id in Spring Boot 3? | Framework Internals | Spring 6 removed LocalVariableTableParameterNameDiscoverer. Without the '-parameters' compiler flag or @PathVariable('id'), parameter names become arg0, failing to match {id}. |
| 5. How do you prevent duplicate records in concurrent environments? | Database & Concurrency | Combining service-level existence checks (existsByEmail) with database-level UNIQUE constraints to prevent race conditions during simultaneous requests. |
| 6. What is the difference between @Component, @Service, and @Repository? | Spring Stereotypes | All are Spring @Component stereotypes. @Service indicates domain logic. @Repository provides automatic DataAccessException translation for database exceptions. |
| 7. What does @RestControllerAdvice do? | Cross-Cutting Concerns | Provides global exception handling via AOP, intercepting exceptions thrown from any @RestController and serializing uniform ErrorResponse JSON. |
| 8. What is the Hibernate N+1 Query Problem? | ORM Performance | Occurs when loading N parent entities causes 1 initial query plus N additional queries for child collections. Solved using 'JOIN FETCH' or @EntityGraph. |
| 9. Why is CSRF protection disabled in stateless REST APIs? | Security Architecture | CSRF exploits browser ambient credentials (cookies). Because REST APIs use custom Authorization headers (Bearer <token>), browsers do not attach them automatically, making CSRF attacks impossible. |
| 10. What is BCrypt and why is it preferred over SHA-256 for passwords? | Cryptography | BCrypt is a slow hashing algorithm with built-in salting and configurable work factor (cost). It is computationally expensive, making brute-force and GPU rainbow-table attacks infeasible. |



# Module 11: AI-Powered Job Board, Resume Parser & Automated Job Application Architecture


To bridge candidates with career opportunities, the application includes an end-to-end recruitment module featuring an interactive Job Board, a Candidate Profile Management system with automated resume parsing, and an intelligent Auto-Apply engine.


## 11.1 Candidate Profile & Automated Resume Parsing Pipeline


The Candidate Profile subsystem allows candidates to maintain structured career data (headline, years of experience, skills, summary) and upload resumes (in plain text or document format). When a resume is received:

- **1. File Ingestion** StandardServletMultipartResolver handles multipart uploads or plain text payloads.
- **2. Skill Extraction** CandidateProfileService scans the resume text against an enterprise tech skill dictionary (Java, Spring Boot, React, Docker, Kubernetes, AWS, SQL, Kafka, etc.).
- **3. Profile Enrichment** Extracted skills are deduplicated, normalized, and automatically merged into the candidate's active profile skills.
- **4. Match Recalibration** The enriched profile updates the database and immediately recalibrates match scores across all active job openings.

## 11.2 The Matching Algorithm & Auto-Apply Execution Pipeline


For each open job posting, required skills are parsed into distinct normalized tokens. The engine compares these requirements against the candidate's combined knowledge pool (skills, headline, summary, and resume text):


```java
// JobService.java Matching Logic
public static MatchResult computeMatch(JobEntity job, CandidateProfileEntity profile) {
    if (job == null || profile == null) return new MatchResult(0, "");

    List<String> requiredSkills = Arrays.stream(job.getRequiredSkills().split(","))
        .map(String::trim).filter(s -> !s.isEmpty()).toList();

    if (requiredSkills.isEmpty()) return new MatchResult(100, "");

    String candidatePool = (profile.getSkills() + " " + profile.getHeadline() + " " +
                           profile.getSummary() + " " + profile.getResumeText()).toLowerCase();

    List<String> matched = requiredSkills.stream()
        .filter(req -> containsSkill(candidatePool, req.toLowerCase()))
        .toList();

    int score = (int) Math.round(((double) matched.size() / requiredSkills.size()) * 100.0);
    return new MatchResult(Math.min(score, 100), String.join(", ", matched));
}
```


When the candidate clicks '⚡ Auto-Apply Suitable Jobs':

- **1. Profile Precondition Check** The system verifies the candidate has completed their profile or uploaded a resume.
- **2. Deduplication Check** Active positions are fetched from JobRepository. Jobs where the candidate has already applied are skipped to ensure idempotency.
- **3. Batch Application Submission** Roles meeting the suitability threshold (match score >= 35% or matching at least 1 core requirement) are automatically applied with autoApplied=true.
- **4. Instant Feedback** Detailed application receipts and compatibility statistics are returned and displayed in the user's dashboard.

## 11.3 Complete Source Code: Job, Profile & Auto-Apply Components


### JobEntity.java


```java
package com.example.learningSpringBootApp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class JobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String type; // Full-time, Remote, Hybrid, Contract

    @Column(nullable = false)
    private String experienceLevel; // Entry-level, Mid-level, Senior, Lead

    private String salaryRange;

    @Column(length = 3000, nullable = false)
    private String description;

    @Column(length = 1000, nullable = false)
    private String requiredSkills; // Comma-separated: "Java, Spring Boot, React, SQL"

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public JobEntity() {
    }

    public JobEntity(String title, String company, String location, String type,
                     String experienceLevel, String salaryRange, String description,
                     String requiredSkills) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.type = type;
        this.experienceLevel = experienceLevel;
        this.salaryRange = salaryRange;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
```


### CandidateProfileEntity.java


```java
package com.example.learningSpringBootApp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "candidate_profiles", uniqueConstraints = {
    @UniqueConstraint(columnNames = "userId")
})
public class CandidateProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    private String fullName;

    private String email;

    private String headline; // e.g. "Full Stack Java Developer | Spring Boot, React, Cloud"

    @Column(length = 3000)
    private String summary;

    @Column(length = 2000)
    private String skills; // e.g. "Java, Spring Boot, Microservices, React, Docker, Kubernetes, PostgreSQL"

    private Integer experienceYears;

    private String resumeFileName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String resumeText;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public CandidateProfileEntity() {
    }

    public CandidateProfileEntity(Long userId, String fullName, String email) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```


### JobApplicationEntity.java


```java
package com.example.learningSpringBootApp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "job_applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_id", "user_id"})
})
public class JobApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String candidateName;

    private String candidateEmail;

    private int matchScore; // 0 - 100

    @Column(length = 1000)
    private String matchingSkills;

    @Column(nullable = false)
    private String status = "APPLIED"; // APPLIED, UNDER_REVIEW, SHORTLISTED, REJECTED

    private boolean autoApplied = false;

    private LocalDateTime appliedAt = LocalDateTime.now();

    public JobApplicationEntity() {
    }

    public JobApplicationEntity(Long jobId, Long userId, String candidateName,
                                String candidateEmail, int matchScore,
                                String matchingSkills, boolean autoApplied) {
        this.jobId = jobId;
        this.userId = userId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.matchScore = matchScore;
        this.matchingSkills = matchingSkills;
        this.autoApplied = autoApplied;
        this.status = "APPLIED";
        this.appliedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(String matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAutoApplied() {
        return autoApplied;
    }

    public void setAutoApplied(boolean autoApplied) {
        this.autoApplied = autoApplied;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
```


### JobService.java


```java
package com.example.learningSpringBootApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.JobDto;
import com.example.learningSpringBootApp.entity.CandidateProfileEntity;
import com.example.learningSpringBootApp.entity.JobEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.JobApplicationRepository;
import com.example.learningSpringBootApp.repository.JobRepository;

import jakarta.annotation.PostConstruct;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    public JobService(JobRepository jobRepository,
                      JobApplicationRepository jobApplicationRepository,
                      CandidateProfileRepository candidateProfileRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.candidateProfileRepository = candidateProfileRepository;
    }

    @PostConstruct
    public void seedInitialJobs() {
        if (jobRepository.count() == 0) {
            List<JobEntity> seedJobs = List.of(
                new JobEntity(
                    "Senior Full Stack Java Engineer",
                    "Google Cloud",
                    "Sunnyvale, CA (Remote)",
                    "Full-time",
                    "Senior (5+ yrs)",
                    "$160,000 - $210,000",
                    "Design and build scalable microservices and reactive user interfaces for Google Cloud Console. Work with high-concurrency systems, Kubernetes orchestration, and Spring Boot enterprise architectures.",
                    "Java, Spring Boot, Microservices, React, Docker, Kubernetes, Cloud, SQL"
                ),
                new JobEntity(
                    "Backend Spring Boot Developer",
                    "Amazon Web Services",
                    "Seattle, WA (Hybrid)",
                    "Full-time",
                    "Mid-level (3+ yrs)",
                    "$135,000 - $175,000",
                    "Develop robust RESTful APIs and distributed event-driven systems using Spring Boot, Kafka, and DynamoDB. Optimize database transactions and implement rock-solid security protocols.",
                    "Java, Spring Boot, REST API, Microservices, Kafka, Redis, SQL, AWS"
                ),
                new JobEntity(
                    "Frontend React & TypeScript Specialist",
                    "Netflix",
                    "Los Gatos, CA (Remote)",
                    "Full-time",
                    "Mid-Senior (4+ yrs)",
                    "$145,000 - $190,000",
                    "Create intuitive, high-performance UI components for Netflix internal tool suites and creator portals. Deep experience with modern React, TypeScript, state management, and responsive CSS is essential.",
                    "React, TypeScript, JavaScript, Redux, HTML5, CSS3, Tailwind CSS, REST API"
                ),
                new JobEntity(
                    "DevOps & Cloud Infrastructure Engineer",
                    "Microsoft Azure",
                    "Redmond, WA (Hybrid)",
                    "Full-time",
                    "Mid-level (3+ yrs)",
                    "$130,000 - $170,000",
                    "Maintain continuous integration and deployment pipelines (CI/CD), automate multi-region infrastructure provisioning using Terraform, and monitor containerized Kubernetes clusters.",
                    "Docker, Kubernetes, AWS, CI/CD, Terraform, Linux, Git, Cloud"
                ),
                new JobEntity(
                    "Full Stack Software Engineer",
                    "Meta",
                    "Menlo Park, CA (Remote)",
                    "Full-time",
                    "Mid-level (2+ yrs)",
                    "$140,000 - $185,000",
                    "Collaborate across cross-functional teams to deliver end-to-end features connecting React frontends with Java Spring Boot and GraphQL services. Passionate about clean code, unit testing, and scalable architecture.",
                    "Java, Spring Boot, React, JavaScript, GraphQL, PostgreSQL, Git, Docker"
                ),
                new JobEntity(
                    "Junior Java / Spring Boot Developer",
                    "Accenture",
                    "Austin, TX (On-site)",
                    "Full-time",
                    "Entry-level (0-2 yrs)",
                    "$75,000 - $95,000",
                    "Great opportunity for early-career software engineers! Learn from experienced architects while building enterprise Spring Boot microservices, writing unit tests, and designing relational databases.",
                    "Java, Spring Boot, SQL, Git, OOP, REST API"
                ),
                new JobEntity(
                    "Staff Distributed Systems Architect",
                    "Uber",
                    "San Francisco, CA (Hybrid)",
                    "Full-time",
                    "Lead / Staff (8+ yrs)",
                    "$210,000 - $280,000",
                    "Lead architecture and technical strategy for mission-critical dispatch and routing engines. Deep mastery of low-latency Java concurrency, Kafka event pipelines, and cloud scalability is required.",
                    "Java, Spring Boot, Microservices, Distributed Systems, Kafka, Kubernetes, Redis, Cloud"
                ),
                new JobEntity(
                    "Mobile Application Engineer (Android)",
                    "Spotify",
                    "New York, NY (Remote)",
                    "Full-time",
                    "Mid-level (3+ yrs)",
                    "$130,000 - $165,000",
                    "Craft smooth, delightful music streaming interfaces on Android. Work with Kotlin, Jetpack Compose, asynchronous coroutines, and REST / GraphQL networking.",
                    "Android, Kotlin, Java, REST API, Git, Mobile"
                )
            );
            jobRepository.saveAll(seedJobs);
        }
    }

    public List<JobDto> listJobs(Long userId, String query, String type, String location) {
        List<JobEntity> jobs = jobRepository.filterJobs(query, type, location);

        Optional<CandidateProfileEntity> profileOpt = (userId != null)
            ? candidateProfileRepository.findByUserId(userId)
            : Optional.empty();

        CandidateProfileEntity profile = profileOpt.orElse(null);

        return jobs.stream()
            .map(job -> toDto(job, userId, profile))
            .collect(Collectors.toList());
    }

    public JobDto getJobById(Long jobId, Long userId) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job with ID " + jobId + " not found."));

        Optional<CandidateProfileEntity> profileOpt = (userId != null)
            ? candidateProfileRepository.findByUserId(userId)
            : Optional.empty();

        return toDto(job, userId, profileOpt.orElse(null));
    }

    public JobDto toDto(JobEntity job, Long userId, CandidateProfileEntity profile) {
        boolean alreadyApplied = false;
        if (userId != null) {
            alreadyApplied = jobApplicationRepository.existsByJobIdAndUserId(job.getId(), userId);
        }

        int matchScore = 0;
        String matchingSkills = "";

        if (profile != null) {
            MatchResult match = computeMatch(job, profile);
            matchScore = match.score;
            matchingSkills = match.matchingSkills;
        }

        return new JobDto(
            job.getId(),
            job.getTitle(),
            job.getCompany(),
            job.getLocation(),
            job.getType(),
            job.getExperienceLevel(),
            job.getSalaryRange(),
            job.getDescription(),
            job.getRequiredSkills(),
            alreadyApplied,
            matchScore,
            matchingSkills,
            job.getCreatedAt()
        );
    }

    public static class MatchResult {
        public final int score;
        public final String matchingSkills;

        public MatchResult(int score, String matchingSkills) {
            this.score = score;
            this.matchingSkills = matchingSkills;
        }
    }

    public static MatchResult computeMatch(JobEntity job, CandidateProfileEntity profile) {
        if (job == null || profile == null) {
            return new MatchResult(0, "");
        }

        String jobSkillsRaw = job.getRequiredSkills() != null ? job.getRequiredSkills() : "";
        List<String> requiredSkills = Arrays.stream(jobSkillsRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        if (requiredSkills.isEmpty()) {
            return new MatchResult(100, "");
        }

        // Candidate search pool: skills + headline + summary + resumeText
        StringBuilder candidatePool = new StringBuilder();
        if (profile.getSkills() != null) candidatePool.append(" ").append(profile.getSkills());
        if (profile.getHeadline() != null) candidatePool.append(" ").append(profile.getHeadline());
        if (profile.getSummary() != null) candidatePool.append(" ").append(profile.getSummary());
        if (profile.getResumeText() != null) candidatePool.append(" ").append(profile.getResumeText());

        String poolLower = candidatePool.toString().toLowerCase();

        List<String> matched = new ArrayList<>();
        for (String req : requiredSkills) {
            String reqLower = req.toLowerCase();
            // Match whole word / phrase
            if (containsSkill(poolLower, reqLower)) {
                matched.add(req);
            }
        }

        int score = (int) Math.round(((double) matched.size() / requiredSkills.size()) * 100.0);
        if (score > 100) score = 100;

        return new MatchResult(score, String.join(", ", matched));
    }

    private static boolean containsSkill(String text, String skill) {
        if (skill.length() <= 3) {
            // For short acronyms (AWS, SQL, Git, OOP), enforce boundary check
            return text.matches(".*\\b" + java.util.regex.Pattern.quote(skill) + "\\b.*");
        }
        return text.contains(skill);
    }
}
```


### CandidateProfileService.java


```java
package com.example.learningSpringBootApp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.CandidateProfileDto;
import com.example.learningSpringBootApp.entity.CandidateProfileEntity;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.UserRepository;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    private static final List<String> COMMON_SKILLS_DICTIONARY = List.of(
        "Java", "Spring Boot", "Microservices", "REST API", "Hibernate", "JPA",
        "React", "Angular", "Vue.js", "TypeScript", "JavaScript", "HTML5", "CSS3", "Tailwind CSS", "Redux", "Next.js",
        "Python", "Django", "FastAPI", "Go", "C++", "C#", ".NET",
        "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Kafka", "RabbitMQ", "Elasticsearch",
        "Docker", "Kubernetes", "AWS", "Azure", "Google Cloud", "CI/CD", "Terraform", "Linux", "Git",
        "GraphQL", "OAuth2", "JWT", "OOP", "Distributed Systems", "Android", "Kotlin", "Swift"
    );

    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository,
                                  UserRepository userRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.userRepository = userRepository;
    }

    public CandidateProfileDto getProfile(Long userId, String email, String name) {
        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                CandidateProfileEntity newProfile = new CandidateProfileEntity(userId, name, email);
                newProfile.setHeadline("Software Engineer");
                newProfile.setSkills("Java, Spring Boot, REST API, SQL, Git");
                newProfile.setExperienceYears(2);
                newProfile.setSummary("Motivated software developer passionate about building scalable, secure backend and full stack web applications.");
                return candidateProfileRepository.save(newProfile);
            });

        return toDto(profile);
    }

    public CandidateProfileDto updateProfile(Long userId, CandidateProfileDto dto) {
        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> new CandidateProfileEntity(userId, dto.getFullName(), dto.getEmail()));

        if (dto.getFullName() != null) profile.setFullName(dto.getFullName().trim());
        if (dto.getHeadline() != null) profile.setHeadline(dto.getHeadline().trim());
        if (dto.getSummary() != null) profile.setSummary(dto.getSummary().trim());
        if (dto.getSkills() != null) profile.setSkills(cleanAndFormatSkills(dto.getSkills()));
        if (dto.getExperienceYears() != null) profile.setExperienceYears(dto.getExperienceYears());

        profile.setUpdatedAt(LocalDateTime.now());
        CandidateProfileEntity saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    public CandidateProfileDto saveResume(Long userId, String fileName, String resumeContent) {
        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserEntity user = userRepository.findById(userId).orElse(null);
                String name = user != null ? user.getName() : "Candidate";
                String email = user != null ? user.getEmail() : "";
                return new CandidateProfileEntity(userId, name, email);
            });

        profile.setResumeFileName(fileName != null ? fileName : "resume.txt");
        profile.setResumeText(resumeContent);
        profile.setUpdatedAt(LocalDateTime.now());

        // Auto-extract tech skills detected in resume content and merge with existing skills
        if (resumeContent != null && !resumeContent.isBlank()) {
            Set<String> existingSkills = parseSkillsToSet(profile.getSkills());
            String lowerContent = resumeContent.toLowerCase();

            for (String dictSkill : COMMON_SKILLS_DICTIONARY) {
                String dictLower = dictSkill.toLowerCase();
                boolean matched = false;
                if (dictSkill.length() <= 3) {
                    matched = resumeContent.matches("(?i).*\\b" + java.util.regex.Pattern.quote(dictSkill) + "\\b.*");
                } else {
                    matched = lowerContent.contains(dictLower);
                }

                if (matched) {
                    existingSkills.add(dictSkill);
                }
            }

            profile.setSkills(String.join(", ", existingSkills));
        }

        CandidateProfileEntity saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    private Set<String> parseSkillsToSet(String skills) {
        if (skills == null || skills.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(skills.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String cleanAndFormatSkills(String rawSkills) {
        if (rawSkills == null || rawSkills.isBlank()) return "";
        return Arrays.stream(rawSkills.split("[,;\\n]"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .distinct()
            .collect(Collectors.joining(", "));
    }

    public CandidateProfileDto toDto(CandidateProfileEntity entity) {
        return new CandidateProfileDto(
            entity.getId(),
            entity.getUserId(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getHeadline(),
            entity.getSummary(),
            entity.getSkills(),
            entity.getExperienceYears(),
            entity.getResumeFileName(),
            entity.getResumeText(),
            entity.getUpdatedAt()
        );
    }
}
```


### JobAutoApplyService.java


```java
package com.example.learningSpringBootApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learningSpringBootApp.dto.AutoApplyResultDto;
import com.example.learningSpringBootApp.dto.JobApplicationDto;
import com.example.learningSpringBootApp.entity.CandidateProfileEntity;
import com.example.learningSpringBootApp.entity.JobApplicationEntity;
import com.example.learningSpringBootApp.entity.JobEntity;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.JobApplicationRepository;
import com.example.learningSpringBootApp.repository.JobRepository;
import com.example.learningSpringBootApp.repository.UserRepository;

@Service
public class JobAutoApplyService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    public JobAutoApplyService(JobRepository jobRepository,
                               JobApplicationRepository jobApplicationRepository,
                               CandidateProfileRepository candidateProfileRepository,
                               UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JobApplicationDto applySingleJob(Long jobId, Long userId) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job with ID " + jobId + " not found."));

        if (!job.isActive()) {
            throw new IllegalStateException("This job opening is no longer active.");
        }

        if (jobApplicationRepository.existsByJobIdAndUserId(jobId, userId)) {
            throw new IllegalStateException("You have already submitted an application for this position.");
        }

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID " + userId));

        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                CandidateProfileEntity fallback = new CandidateProfileEntity(userId, user.getName(), user.getEmail());
                return candidateProfileRepository.save(fallback);
            });

        JobService.MatchResult match = JobService.computeMatch(job, profile);

        JobApplicationEntity application = new JobApplicationEntity(
            job.getId(),
            userId,
            profile.getFullName() != null ? profile.getFullName() : user.getName(),
            profile.getEmail() != null ? profile.getEmail() : user.getEmail(),
            match.score,
            match.matchingSkills,
            false // manual apply
        );

        JobApplicationEntity saved = jobApplicationRepository.save(application);
        return toDto(saved, job);
    }

    @Transactional
    public AutoApplyResultDto autoApplyAllMatchingJobs(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID " + userId));

        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                CandidateProfileEntity fallback = new CandidateProfileEntity(userId, user.getName(), user.getEmail());
                fallback.setHeadline("Software Developer");
                fallback.setSkills("Java, Spring Boot, SQL, Git");
                return candidateProfileRepository.save(fallback);
            });

        boolean hasSkills = profile.getSkills() != null && !profile.getSkills().isBlank();
        boolean hasResume = profile.getResumeText() != null && !profile.getResumeText().isBlank();

        if (!hasSkills && !hasResume) {
            throw new IllegalStateException(
                "Please add skills or upload your resume in the 'My Profile' tab before running Auto-Apply."
            );
        }

        List<JobEntity> activeJobs = jobRepository.findByActiveTrueOrderByIdDesc();
        int evaluated = activeJobs.size();
        int appliedCount = 0;
        int alreadyAppliedCount = 0;
        int skippedLowMatchCount = 0;
        List<JobApplicationDto> newlyAppliedList = new ArrayList<>();

        for (JobEntity job : activeJobs) {
            if (jobApplicationRepository.existsByJobIdAndUserId(job.getId(), userId)) {
                alreadyAppliedCount++;
                continue;
            }

            JobService.MatchResult match = JobService.computeMatch(job, profile);

            // Suitability condition:
            // Either match score >= 35%, or candidate matches at least 1 significant required skill
            int matchedSkillCount = match.matchingSkills.isBlank() ? 0 : match.matchingSkills.split(",").length;
            boolean isSuitable = match.score >= 35 || matchedSkillCount >= 1;

            if (isSuitable) {
                JobApplicationEntity application = new JobApplicationEntity(
                    job.getId(),
                    userId,
                    profile.getFullName() != null ? profile.getFullName() : user.getName(),
                    profile.getEmail() != null ? profile.getEmail() : user.getEmail(),
                    match.score,
                    match.matchingSkills,
                    true // autoApplied
                );

                JobApplicationEntity saved = jobApplicationRepository.save(application);
                newlyAppliedList.add(toDto(saved, job));
                appliedCount++;
            } else {
                skippedLowMatchCount++;
            }
        }

        String msg;
        if (appliedCount > 0) {
            msg = String.format("Successfully auto-applied to %d suitable jobs matching your profile & resume!", appliedCount);
        } else if (alreadyAppliedCount > 0 && activeJobs.size() == alreadyAppliedCount) {
            msg = "You have already applied to all currently active positions.";
        } else {
            msg = "Evaluated all open positions. No new jobs reached the suitability threshold (35%+ skill match). Consider updating your skills or resume to match more opportunities.";
        }

        return new AutoApplyResultDto(
            evaluated,
            appliedCount,
            alreadyAppliedCount,
            skippedLowMatchCount,
            newlyAppliedList,
            msg
        );
    }

    public List<JobApplicationDto> getMyApplications(Long userId) {
        List<JobApplicationEntity> applications = jobApplicationRepository.findByUserIdOrderByAppliedAtDesc(userId);
        if (applications.isEmpty()) {
            return List.of();
        }

        List<Long> jobIds = applications.stream().map(JobApplicationEntity::getJobId).distinct().toList();
        Map<Long, JobEntity> jobMap = jobRepository.findAllById(jobIds).stream()
            .collect(Collectors.toMap(JobEntity::getId, Function.identity()));

        return applications.stream()
            .map(app -> toDto(app, jobMap.get(app.getJobId())))
            .collect(Collectors.toList());
    }

    private JobApplicationDto toDto(JobApplicationEntity app, JobEntity job) {
        String jobTitle = (job != null) ? job.getTitle() : "Unknown Position";
        String company = (job != null) ? job.getCompany() : "Unknown Company";
        String location = (job != null) ? job.getLocation() : "N/A";
        String type = (job != null) ? job.getType() : "N/A";
        String salaryRange = (job != null) ? job.getSalaryRange() : "Competitive";

        return new JobApplicationDto(
            app.getId(),
            app.getJobId(),
            jobTitle,
            company,
            location,
            type,
            salaryRange,
            app.getMatchScore(),
            app.getMatchingSkills(),
            app.getStatus(),
            app.isAutoApplied(),
            app.getAppliedAt()
        );
    }
}
```


### JobController.java


```java
package com.example.learningSpringBootApp;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.learningSpringBootApp.dto.AutoApplyResultDto;
import com.example.learningSpringBootApp.dto.JobDto;
import com.example.learningSpringBootApp.dto.JobApplicationDto;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.UserRepository;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final JobAutoApplyService jobAutoApplyService;
    private final UserRepository userRepository;

    public JobController(JobService jobService,
                         JobAutoApplyService jobAutoApplyService,
                         UserRepository userRepository) {
        this.jobService = jobService;
        this.jobAutoApplyService = jobAutoApplyService;
        this.userRepository = userRepository;
    }

    private Long getUserIdFromAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName())
            .map(UserEntity::getId)
            .orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<JobDto>> getJobs(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "location", required = false) String location,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        List<JobDto> jobs = jobService.listJobs(userId, query, type, location);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(@PathVariable("id") Long id, Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        JobDto job = jobService.getJobById(id, userId);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<JobApplicationDto> applyToJob(@PathVariable("id") Long id, Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        JobApplicationDto result = jobAutoApplyService.applySingleJob(id, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/auto-apply")
    public ResponseEntity<AutoApplyResultDto> autoApplyJobs(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        AutoApplyResultDto result = jobAutoApplyService.autoApplyAllMatchingJobs(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<JobApplicationDto>> getMyApplications(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<JobApplicationDto> applications = jobAutoApplyService.getMyApplications(userId);
        return ResponseEntity.ok(applications);
    }
}
```


### ProfileController.java


```java
package com.example.learningSpringBootApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.learningSpringBootApp.dto.CandidateProfileDto;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.UserRepository;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CandidateProfileService profileService;
    private final UserRepository userRepository;

    public ProfileController(CandidateProfileService profileService,
                             UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    @GetMapping
    public ResponseEntity<CandidateProfileDto> getProfile(Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        CandidateProfileDto profile = profileService.getProfile(user.getId(), user.getEmail(), user.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<CandidateProfileDto> updateProfile(@RequestBody CandidateProfileDto dto, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        dto.setUserId(user.getId());
        CandidateProfileDto updated = profileService.updateProfile(user.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/resume")
    public ResponseEntity<CandidateProfileDto> uploadResume(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "resumeText", required = false) String resumeText,
            Authentication auth) throws IOException {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String fileName = "resume.txt";
        String content = resumeText;

        if (file != null && !file.isEmpty()) {
            fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_resume.txt";
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        CandidateProfileDto updated = profileService.saveResume(user.getId(), fileName, content);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/resume-text")
    public ResponseEntity<CandidateProfileDto> saveResumeText(@RequestBody Map<String, String> payload, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String text = payload.get("resumeText");
        String fileName = payload.getOrDefault("fileName", "pasted_resume.txt");

        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        CandidateProfileDto updated = profileService.saveResume(user.getId(), fileName, text);
        return ResponseEntity.ok(updated);
    }
}
```
