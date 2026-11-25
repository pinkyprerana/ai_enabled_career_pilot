#!/usr/bin/env python3
import os
import sys
from docx_builder import DocxBuilder

base_dir = "/Users/pinkyprerna/Desktop/learningSpringBootApp"
docx_path = os.path.join(base_dir, "Full_Stack_Spring_Boot_Master_Documentation.docx")
md_path = os.path.join(base_dir, "FULL_STACK_SPRING_BOOT_GUIDE.md")

doc = DocxBuilder(docx_path)
md_lines = []

def h1(title):
    doc.add_h1(title)
    md_lines.append(f"\n# {title}\n")

def h2(title):
    doc.add_h2(title)
    md_lines.append(f"\n## {title}\n")

def h3(title):
    doc.add_h3(title)
    md_lines.append(f"\n### {title}\n")

def p(text, bold_prefix=None):
    doc.add_paragraph(text, bold_prefix)
    if bold_prefix:
        md_lines.append(f"\n**{bold_prefix}** {text}\n")
    else:
        md_lines.append(f"\n{text}\n")

def bullet(text, bold_prefix=None):
    doc.add_bullet(text, bold_prefix)
    if bold_prefix:
        md_lines.append(f"- **{bold_prefix}** {text}")
    else:
        md_lines.append(f"- {text}")

def callout(title, text):
    doc.add_callout(title, text)
    md_lines.append(f"\n> **{title}**: {text}\n")

def code(source, lang="java"):
    doc.add_code_block(source)
    md_lines.append(f"\n```{lang}\n{source}\n```\n")

def table(headers, rows):
    doc.add_table(headers, rows)
    md_lines.append("\n| " + " | ".join(headers) + " |")
    md_lines.append("| " + " | ".join(["---"] * len(headers)) + " |")
    for r in rows:
        md_lines.append("| " + " | ".join(r) + " |")
    md_lines.append("\n")

def read_source(rel_path):
    full = os.path.join(base_dir, rel_path)
    if os.path.exists(full):
        with open(full, "r", encoding="utf-8") as f:
            return f.read().strip()
    return f"// File not found: {rel_path}"

# Document Title
doc.add_title(
    "FULL STACK SOFTWARE ENGINEERING MASTER GUIDE",
    "Enterprise Spring Boot 3/4 REST API Architecture, Security (JWT/BCrypt), JPA Persistence & Token-by-Token Reference"
)
md_lines.append("# FULL STACK SOFTWARE ENGINEERING MASTER GUIDE\n")
md_lines.append("### Enterprise Spring Boot 3/4 REST API Architecture, Security (JWT/BCrypt), JPA Persistence & Token-by-Token Reference\n")

# Module 1: Architecture Overview
h1("Module 1: Full-Stack System Architecture & Request Lifecycle")
p("In enterprise full-stack development, modern applications separate client-side presentation from server-side domain logic. The backend operates as a secure, stateless REST API handling authentication, authorization, business validation, and database persistence.")

h2("1.1 The Enterprise Request-Response Pipeline")
p("When a web or mobile client makes an HTTP request to this Spring Boot application, it travels through distinct architectural boundaries:")
bullet("The client (React, Vue, mobile) sends an HTTP request with JSON payload and an optional 'Authorization: Bearer <token>' header.", "1. Client Gateway")
bullet("The request hits the Servlet Filter chain. JwtAuthenticationFilter inspects the Bearer token, validates cryptographic signature and expiration, and sets the authenticated principal in SecurityContextHolder.", "2. Security Filter Chain")
bullet("DispatcherServlet receives the filtered request, consults HandlerMapping, and delegates execution to the matched Controller method.", "3. DispatcherServlet & Routing")
bullet("Spring validates incoming JSON against DTO constraints (@Valid, @NotBlank, @Email). Violations trigger MethodArgumentNotValidException caught by GlobalExceptionHandler.", "4. DTO & Validation Layer")
bullet("The Controller delegates business execution to the Service layer. The Service enforces business invariants (such as preventing duplicate email registration).", "5. Service Layer (Domain Logic)")
bullet("The Service invokes the Spring Data JPA Repository, which automatically maps domain calls to optimized SQL queries via Hibernate ORM against the database (H2 in memory).", "6. Persistence Layer (JPA / Hibernate)")
bullet("The result is converted from an internal Entity to a clean Response DTO (excluding sensitive fields like passwords), returned as HTTP 200/201/204 with clean JSON.", "7. Response Transformation")

callout("Stateless Architecture Principle", "REST APIs should never maintain server-side HTTP Sessions (HttpSession). Instead, each request carries its own cryptographic proof of identity (JWT). This makes the backend horizontally scalable across multiple server instances without sticky sessions or centralized session caches.")

# Module 2: Build & Configuration
h1("Module 2: Project Build & Runtime Configuration")

h2("2.1 build.gradle (Build & Dependency Engine)")
p("The Gradle build script defines JVM toolchains, framework dependencies, compiler arguments, and test executors.")
code(read_source("build.gradle"), "groovy")

table(
    ["Token / Configuration", "Type", "Full-Stack Software Engineering Explanation"],
    [
        ["id 'org.springframework.boot'", "Gradle Plugin", "Applies the Spring Boot Gradle plugin, providing dependency management, executable JAR packaging, and bootRun tasks."],
        ["id 'io.spring.dependency-management'", "Gradle Plugin", "Manages compatible dependency versions transitively based on the Spring Boot Bill of Materials (BOM), eliminating manual version numbers."],
        ["JavaLanguageVersion.of(23)", "JVM Toolchain", "Pins the Java language version to 23, guaranteeing reproducible builds across development, CI/CD, and production environments."],
        ["spring-boot-starter-webmvc", "Starter Dependency", "Includes Spring MVC, Jackson (JSON serializer/deserializer), and an embedded Apache Tomcat web server."],
        ["spring-boot-starter-data-jpa", "Starter Dependency", "Brings in Spring Data JPA and Hibernate ORM for database abstraction and automatic repository query generation."],
        ["spring-boot-starter-security", "Starter Dependency", "Provides authentication, authorization, BCrypt password hashing, and filter chain protection."],
        ["spring-boot-starter-validation", "Starter Dependency", "Provides Jakarta Bean Validation (Hibernate Validator) for validating request DTOs (@NotBlank, @Email, @Size)."],
        ["com.h2database:h2", "Runtime Dependency", "Zero-configuration embedded SQL database used for rapid local development and automated integration testing."],
        ["io.jsonwebtoken:jjwt-api / impl / jackson", "JWT Library", "Standard Java JWT library (0.12.x) for creating, signing, and parsing cryptographically verified JSON Web Tokens."],
        ["options.compilerArgs << '-parameters'", "Compiler Argument", "CRITICAL: Forces javac to retain parameter names in bytecode. In Spring 6 / Boot 3+, missing this flag causes HTTP 500 on @PathVariable without explicit names!"]
    ]
)

h2("2.2 application.yaml (Application Configuration)")
p("The application.yaml file configures externalized properties including the database connection, Hibernate behavior, web consoles, and JWT encryption keys.")
code(read_source("src/main/resources/application.yaml"), "yaml")

table(
    ["Configuration Property", "Environment Role", "Full-Stack Architectural Purpose"],
    [
        ["spring.application.name", "Metadata", "Identifies the microservice in logs, APM traces (Prometheus/Zipkin), and service registries (Eureka/Consul)."],
        ["spring.datasource.url", "JDBC Connection", "Specifies the JDBC URL for H2 in-memory mode ('jdbc:h2:mem:studentdb'). DB_CLOSE_DELAY=-1 prevents DB drop between connections."],
        ["spring.jpa.hibernate.ddl-auto", "Schema DDL", "'update' automatically creates/updates database tables based on @Entity classes upon application startup."],
        ["spring.h2.console.enabled", "Developer Tool", "Enables the web-based SQL administration console accessible at http://localhost:8080/h2-console."],
        ["jwt.secret", "Security Key", "256-bit hexadecimal cryptographic secret key used to compute the HMAC-SHA256 signature of every issued JWT."],
        ["jwt.expiration-ms", "Token Lifetime", "Token validity in milliseconds (86,400,000 ms = 24 hours). After expiration, requests receive HTTP 401 Unauthorized."],
        ["payment.provider", "Feature Flag", "Controls conditional bean registration (@ConditionalOnProperty) to toggle payment processors dynamically."]
    ]
)

# Module 3: Domain & Persistence Layer
h1("Module 3: Domain Models & Database Persistence Layer")

h2("3.1 StudentEntity.java (Student JPA Entity)")
p("Represents the persistent data schema for students stored in the relational database table 'students'.")
code(read_source("src/main/java/com/example/learningSpringBootApp/entity/StudentEntity.java"), "java")

table(
    ["Token / Keyword", "Category", "Technical Explanation"],
    [
        ["@Entity", "JPA Annotation", "Marks the Java class as a persistent entity to be managed by the Hibernate EntityManager."],
        ["@Table(name = 'students')", "JPA Annotation", "Maps the entity to the 'students' SQL table and declares table-level constraints."],
        ["uniqueConstraints = @UniqueConstraint(columnNames = 'email')", "Database DDL", "Enforces a database-level UNIQUE constraint on the email column. Prevents race conditions from inserting duplicate emails."],
        ["@Id", "JPA Annotation", "Designates the 'id' field as the primary key of the table."],
        ["@GeneratedValue(strategy = GenerationType.IDENTITY)", "Primary Key Strategy", "Uses the database auto-increment identity column to assign IDs sequentially."],
        ["@Column(nullable = false, unique = true)", "Column Metadata", "Generates NOT NULL and UNIQUE column constraints in the generated SQL DDL schema."],
        ["public StudentEntity() {}", "Constructor", "Mandatory no-argument constructor required by Hibernate to instantiate entities via Java reflection."]
    ]
)

h2("3.2 UserEntity.java (User Authentication Entity)")
p("Represents registered system users who have credentials to log into the platform.")
code(read_source("src/main/java/com/example/learningSpringBootApp/entity/UserEntity.java"), "java")

table(
    ["Token / Keyword", "Category", "Technical Explanation"],
    [
        ["password", "Entity Field", "Stores the BCrypt-hashed password digest. Plain-text passwords must NEVER be saved in this field."],
        ["role", "Entity Field", "Stores the user authority string (e.g., 'ROLE_USER', 'ROLE_ADMIN') for Role-Based Access Control (RBAC)."],
        ["unique = true", "Column Constraint", "Guarantees that two users cannot register with the identical email address."]
    ]
)

h2("3.3 StudentRepository.java & UserRepository.java")
p("Spring Data JPA repositories abstract database access into type-safe Java interfaces.")
code(read_source("src/main/java/com/example/learningSpringBootApp/repository/StudentRepository.java"), "java")

table(
    ["Repository Method", "Generated SQL Equivalent", "Full-Stack Purpose"],
    [
        ["extends JpaRepository<StudentEntity, Long>", "Base Interface", "Inherits CRUD operations (save, findById, findAll, deleteById, count) without writing any boilerplate code."],
        ["existsByEmail(String email)", "SELECT COUNT(*) > 0 FROM students WHERE email = ?", "Fast O(1) indexed existence check used during creation to block duplicate emails."],
        ["existsByEmailAndIdNot(String email, Long id)", "SELECT COUNT(*) > 0 FROM students WHERE email = ? AND id != ?", "Ensures that when updating student X, the new email isn't already used by student Y!"],
        ["findByEmail(String email)", "SELECT * FROM students WHERE email = ? LIMIT 1", "Fetches entity by email returning an Optional<StudentEntity> to prevent NullPointerExceptions."]
    ]
)

# Module 4: DTOs & Validation
h1("Module 4: Data Transfer Objects (DTOs) & Validation Layer")
p("Professional full-stack architecture enforces a strict separation between internal database Entities and external API DTOs. Direct entity exposure causes security vulnerabilities (over-posting, mass assignment) and tight coupling.")

h2("4.1 StudentRequest.java & StudentResponse.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/dto/StudentRequest.java"), "java")
code(read_source("src/main/java/com/example/learningSpringBootApp/dto/StudentResponse.java"), "java")

table(
    ["Token / Annotation", "Category", "Technical Explanation"],
    [
        ["record", "Java 16+ Keyword", "Creates an immutable data carrier with transparent constructor, getters, equals(), hashCode(), and toString()."],
        ["@NotBlank(message = '...')", "Bean Validation", "Ensures the string is not null and contains at least one non-whitespace character."],
        ["@Email(message = '...')", "Bean Validation", "Validates that the string conforms to RFC 5322 email address format specifications."],
        ["StudentResponse", "Output DTO", "Safely exposes only the public attributes (id, name, email) of the student to the client."]
    ]
)

h2("4.2 SignupRequest, LoginRequest & AuthResponse")
code(read_source("src/main/java/com/example/learningSpringBootApp/dto/SignupRequest.java"), "java")
code(read_source("src/main/java/com/example/learningSpringBootApp/dto/LoginRequest.java"), "java")
code(read_source("src/main/java/com/example/learningSpringBootApp/dto/AuthResponse.java"), "java")

table(
    ["Token / Annotation", "Category", "Technical Explanation"],
    [
        ["@Size(min = 6)", "Validation Rule", "Enforces a minimum password length of 6 characters for credential security."],
        ["AuthResponse", "Response DTO", "Returns the signed JWT token, token type ('Bearer'), user ID, name, email, and assigned role."],
        ["Bearer", "HTTP Auth Scheme", "Standard OAuth 2.0 / RFC 6750 token scheme where the bearer of the token possesses access rights."]
    ]
)

h2("4.3 ErrorResponse.java")
p("Unified error structure returned across all failed requests, complying with REST best practices.")
code(read_source("src/main/java/com/example/learningSpringBootApp/dto/ErrorResponse.java"), "java")

# Module 5: Centralized Exception Handling
h1("Module 5: Centralized Exception Handling Tier")
p("Spring's Controller Advice pattern decouples error presentation from business logic.")

h2("5.1 Custom Business Exceptions")
code(read_source("src/main/java/com/example/learningSpringBootApp/exception/DuplicateResourceException.java"), "java")
code(read_source("src/main/java/com/example/learningSpringBootApp/exception/ResourceNotFoundException.java"), "java")

h2("5.2 GlobalExceptionHandler.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/exception/GlobalExceptionHandler.java"), "java")

table(
    ["ExceptionHandler Method", "HTTP Status", "Scenario Handled"],
    [
        ["@RestControllerAdvice", "N/A", "Registers a global interceptor around all @RestController endpoints."],
        ["handleDuplicateResource()", "409 Conflict", "Triggered when duplicate student/user email is registered."],
        ["handleResourceNotFound()", "404 Not Found", "Triggered when a requested student ID or user does not exist in the database."],
        ["handleValidationErrors()", "400 Bad Request", "Catches MethodArgumentNotValidException and converts field errors into a clean JSON map."],
        ["handleBadCredentials()", "401 Unauthorized", "Triggered when login credentials (email or password) do not match."],
        ["handleAccessDenied()", "403 Forbidden", "Triggered when an authenticated user attempts to access an endpoint beyond their role."],
        ["handleGeneralException()", "500 Internal Error", "Fallback safety net catching unexpected server errors with structured JSON."]
    ]
)

# Module 6: Security & JWT
h1("Module 6: Security, JWT & Authentication Architecture")

h2("6.1 JwtUtils.java (Token Generation & Verification)")
code(read_source("src/main/java/com/example/learningSpringBootApp/security/JwtUtils.java"), "java")

table(
    ["Method / Token", "Category", "Full-Stack Security Role"],
    [
        ["Keys.hmacShaKeyFor()", "Cryptography", "Derives a SecretKey instance from the 256-bit UTF-8 secret for HMAC-SHA signing."],
        ["Jwts.builder().subject().claim()", "JWT Creation", "Embeds the user email as subject, assigns custom claims ('role'), sets issue/expiration timestamps, and signs the compact JWT."],
        ["Jwts.parser().verifyWith()", "JWT Verification", "Cryptographically verifies that the token was signed with our secret key and has not been tampered with."],
        ["extractClaim()", "Claim Extraction", "Extracts identity metadata (subject email, role, expiration) from the verified token payload."],
        ["validateToken()", "Validation Logic", "Verifies both that the username matches userDetails and that the expiration date is in the future."]
    ]
)

h2("6.2 CustomUserDetailsService.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/security/CustomUserDetailsService.java"), "java")

h2("6.3 JwtAuthenticationFilter.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/security/JwtAuthenticationFilter.java"), "java")

table(
    ["Token / Method", "Category", "Execution Detail"],
    [
        ["OncePerRequestFilter", "Base Class", "Guarantees this filter executes exactly once per incoming HTTP request."],
        ["request.getHeader('Authorization')", "HTTP Header", "Reads the Bearer authentication token sent by frontend clients."],
        ["SecurityContextHolder.getContext().setAuthentication()", "Spring Security", "Sets the authenticated principal for the current thread execution, granting access to protected routes."]
    ]
)

h2("6.4 SecurityConfig.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/security/SecurityConfig.java"), "java")

table(
    ["Security Rule", "Method Call", "Architectural Reasoning"],
    [
        ["CSRF Disabled", "csrf(csrf -> csrf.disable())", "Safe and required for stateless REST APIs using Bearer tokens (browser does not auto-attach Bearer tokens like cookies)."],
        ["Stateless Sessions", "SessionCreationPolicy.STATELESS", "Prevents server from creating JSESSIONID cookies. Scales horizontally."],
        ["Public Endpoints", "requestMatchers('/api/auth/**').permitAll()", "Allows unauthenticated users to access sign-up, login, and H2 console."],
        ["Protected Endpoints", "requestMatchers('/api/students/**').authenticated()", "Blocks unauthenticated access; requires valid JWT token."],
        ["Filter Ordering", "addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)", "Executes JWT parsing BEFORE Spring's default username/password filter."],
        ["BCryptPasswordEncoder", "@Bean PasswordEncoder", "Industry standard adaptive cryptographic hashing function with built-in salting against rainbow table attacks."]
    ]
)

# Module 7: Business Services & Controllers
h1("Module 7: Business Services & REST Controllers")

h2("7.1 AuthService.java & AuthController.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/auth/AuthService.java"), "java")
code(read_source("src/main/java/com/example/learningSpringBootApp/auth/AuthController.java"), "java")

h2("7.2 StudentService.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/StudentService.java"), "java")

table(
    ["Service Method", "Business Rule Enforced", "Behavior"],
    [
        ["create()", "No Duplicate Email", "Calls studentRepository.existsByEmail(). If true, aborts immediately with DuplicateResourceException (409)."],
        ["update()", "No Duplicate Email on Edit", "Calls existsByEmailAndIdNot() to ensure updated email is not held by another student."],
        ["findById()", "Entity Existence", "Uses orElseThrow() to produce clean ResourceNotFoundException (404) if ID is absent."],
        ["delete()", "Idempotent / Valid Deletion", "Verifies existence with existsById() before deleting, preventing silent failures."]
    ]
)

h2("7.3 StudentController.java (The Bug Fix Explained)")
code(read_source("src/main/java/com/example/learningSpringBootApp/StudentController.java"), "java")

callout("The 500 Error Bug & How It Was Resolved", 
        "In Spring Framework 6 (Spring Boot 3+), LocalVariableTableParameterNameDiscoverer was deprecated and removed. "
        "When using bare '@PathVariable Long id' without explicit names, the compiler generated bytecode with 'arg0'. "
        "Spring could not resolve {id} and threw IllegalArgumentException resulting in HTTP 500! "
        "The fix was two-fold: 1) Explicitly naming @PathVariable('id') Long id, and 2) Adding '-parameters' to build.gradle.")

table(
    ["Endpoint", "HTTP Method", "Status Code", "Description"],
    [
        ["/api/students", "GET", "200 OK", "Returns list of all students."],
        ["/api/students/{id}", "GET", "200 OK", "Returns student by ID. Throws 404 if not found."],
        ["/api/students", "POST", "201 Created", "Creates student. Returns Location header and created student body."],
        ["/api/students/{id}", "PUT", "200 OK", "Updates existing student data with duplicate check."],
        ["/api/students/{id}", "DELETE", "204 No Content", "Deletes student record."]
    ]
)

# Module 8: Extensibility, Inversion of Control & Testing
h1("Module 8: Extensibility, Inversion of Control & Testing")

h2("8.1 Payment Service & Conditional Beans")
code(read_source("src/main/java/com/example/learningSpringBootApp/PaymentService.java"), "java")
code(read_source("src/main/java/com/example/learningSpringBootApp/RazorpayPaymentService.java"), "java")
code(read_source("src/main/java/com/example/learningSpringBootApp/StripePaymentService.java"), "java")

table(
    ["Token / Concept", "Category", "Explanation"],
    [
        ["PaymentService", "Java Interface", "Defines the contract adhering to the Dependency Inversion Principle (High-level modules should depend on abstractions)."],
        ["@ConditionalOnProperty", "Spring Boot Annotation", "Registers the bean ONLY if 'payment.provider' in application.yaml matches the specified value."],
        ["Polymorphic Injection", "Spring DI", "The consumer (LearningSpringBootAppApplication) injects PaymentService without knowing whether Stripe or Razorpay is active!"]
    ]
)

h2("8.2 LearningSpringBootAppApplication.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/LearningSpringBootAppApplication.java"), "java")

h2("8.3 LearningSpringBootAppApplicationTests.java")
code(read_source("src/test/java/com/example/learningSpringBootApp/LearningSpringBootAppApplicationTests.java"), "java")

# Module 9: Full-Stack Integration
h1("Module 9: Full-Stack Frontend Integration (React / Next.js)")
p("To complete the full-stack architecture, here is the production-ready TypeScript/React client code showing how modern frontends interact with this API.")

code('''// api/client.ts - Axios instance with automatic JWT Bearer token injection
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
);''', "typescript")

# Module 10: Full-Stack Interview Guide
h1("Module 10: Top 10 Full-Stack Software Engineering Interview Questions")

table(
    ["Question", "Core Engineering Concept Tested", "Ideal Answer Summary"],
    [
        ["1. What is Inversion of Control (IoC) and Dependency Injection (DI)?", "Spring Core", "IoC inverts control of object lifecycle from the developer to the Spring container. DI is the mechanism where the container injects dependencies via constructors."],
        ["2. Why use DTOs instead of exposing JPA Entities directly?", "Security & API Design", "Prevents mass assignment vulnerabilities, avoids Hibernate lazy-loading serialization errors, and decouples the API contract from the DB schema."],
        ["3. How does JWT authentication differ from Session cookies?", "Security & Scalability", "Sessions store state on the server (requiring shared cache or sticky sessions). JWTs are cryptographically signed self-contained tokens verified on every request, making APIs stateless and horizontally scalable."],
        ["4. What caused the 500 error in @PathVariable Long id in Spring Boot 3?", "Framework Internals", "Spring 6 removed LocalVariableTableParameterNameDiscoverer. Without the '-parameters' compiler flag or @PathVariable('id'), parameter names become arg0, failing to match {id}."],
        ["5. How do you prevent duplicate records in concurrent environments?", "Database & Concurrency", "Combining service-level existence checks (existsByEmail) with database-level UNIQUE constraints to prevent race conditions during simultaneous requests."],
        ["6. What is the difference between @Component, @Service, and @Repository?", "Spring Stereotypes", "All are Spring @Component stereotypes. @Service indicates domain logic. @Repository provides automatic DataAccessException translation for database exceptions."],
        ["7. What does @RestControllerAdvice do?", "Cross-Cutting Concerns", "Provides global exception handling via AOP, intercepting exceptions thrown from any @RestController and serializing uniform ErrorResponse JSON."],
        ["8. What is the Hibernate N+1 Query Problem?", "ORM Performance", "Occurs when loading N parent entities causes 1 initial query plus N additional queries for child collections. Solved using 'JOIN FETCH' or @EntityGraph."],
        ["9. Why is CSRF protection disabled in stateless REST APIs?", "Security Architecture", "CSRF exploits browser ambient credentials (cookies). Because REST APIs use custom Authorization headers (Bearer <token>), browsers do not attach them automatically, making CSRF attacks impossible."],
        ["10. What is BCrypt and why is it preferred over SHA-256 for passwords?", "Cryptography", "BCrypt is a slow hashing algorithm with built-in salting and configurable work factor (cost). It is computationally expensive, making brute-force and GPU rainbow-table attacks infeasible."]
    ]
)

# Module 11: AI-Powered Job Board, Candidate Resume Parser & Auto-Apply Architecture
h1("Module 11: AI-Powered Job Board, Resume Parser & Automated Job Application Architecture")
p("To bridge candidates with career opportunities, the application includes an end-to-end recruitment module featuring an interactive Job Board, a Candidate Profile Management system with automated resume parsing, and an intelligent Auto-Apply engine.")

h2("11.1 Candidate Profile & Automated Resume Parsing Pipeline")
p("The Candidate Profile subsystem allows candidates to maintain structured career data (headline, years of experience, skills, summary) and upload resumes (in plain text or document format). When a resume is received:")
bullet("StandardServletMultipartResolver handles multipart uploads or plain text payloads.", "1. File Ingestion")
bullet("CandidateProfileService scans the resume text against an enterprise tech skill dictionary (Java, Spring Boot, React, Docker, Kubernetes, AWS, SQL, Kafka, etc.).", "2. Skill Extraction")
bullet("Extracted skills are deduplicated, normalized, and automatically merged into the candidate's active profile skills.", "3. Profile Enrichment")
bullet("The enriched profile updates the database and immediately recalibrates match scores across all active job openings.", "4. Match Recalibration")

h2("11.2 The Matching Algorithm & Auto-Apply Execution Pipeline")
p("For each open job posting, required skills are parsed into distinct normalized tokens. The engine compares these requirements against the candidate's combined knowledge pool (skills, headline, summary, and resume text):")
code('''// JobService.java Matching Logic
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
}''', "java")

p("When the candidate clicks '⚡ Auto-Apply Suitable Jobs':")
bullet("The system verifies the candidate has completed their profile or uploaded a resume.", "1. Profile Precondition Check")
bullet("Active positions are fetched from JobRepository. Jobs where the candidate has already applied are skipped to ensure idempotency.", "2. Deduplication Check")
bullet("Roles meeting the suitability threshold (match score >= 35% or matching at least 1 core requirement) are automatically applied with autoApplied=true.", "3. Batch Application Submission")
bullet("Detailed application receipts and compatibility statistics are returned and displayed in the user's dashboard.", "4. Instant Feedback")

h2("11.3 Complete Source Code: Job, Profile & Auto-Apply Components")

h3("JobEntity.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/entity/JobEntity.java"), "java")

h3("CandidateProfileEntity.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/entity/CandidateProfileEntity.java"), "java")

h3("JobApplicationEntity.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/entity/JobApplicationEntity.java"), "java")

h3("JobService.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/JobService.java"), "java")

h3("CandidateProfileService.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/CandidateProfileService.java"), "java")

h3("JobAutoApplyService.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/JobAutoApplyService.java"), "java")

h3("JobController.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/JobController.java"), "java")

h3("ProfileController.java")
code(read_source("src/main/java/com/example/learningSpringBootApp/ProfileController.java"), "java")

# Save Word Document
doc.save()

# Save Markdown Document
with open(md_path, "w", encoding="utf-8") as f:
    f.write("\n".join(md_lines))

print(f"Generated Markdown at {md_path} ({os.path.getsize(md_path)} bytes)")
print("Documentation build complete!")
