# CareerPilot --- AI-Enabled Job Board & Application Platform

CareerPilot is a full-stack job discovery and application platform built
with Java and Spring Boot. It helps candidates find relevant
opportunities, extract skills from their resumes, compare their skills
with job requirements, and submit applications through a streamlined
workflow.

> **Project status:** Personal learning and portfolio project.

## Features

### Smart job discovery

-   Search by job title, company, location, and required skills.
-   Filter opportunities by work mode and employment type, including
    remote, hybrid, full-time, and contract roles.

### Resume parsing and skill extraction

-   Upload a resume or paste resume text.
-   Extract technical skills by matching resume content against a
    technology dictionary.
-   Use extracted skills to enrich the candidate profile.

### Job matching and Auto-Apply

-   Compare candidate skills with the skills required by a job.

-   Calculate a compatibility score using a skill-matching heuristic:

    `match score = (matched required skills / total required skills) × 100`

-   Submit applications to suitable positions through a single workflow.

-   Prevent duplicate applications with a database-level unique
    constraint on the candidate and job.

### Authentication and security

-   Stateless JWT authentication using HMAC-SHA256 signing.
-   Token revocation and logout through a token blacklist.
-   OAuth2 social login integrations for Google, GitHub, and X.
-   BCrypt password hashing.
-   Externalized configuration for secrets.

### Application analytics

-   Track total applications.
-   Compare automatically submitted and manually submitted applications.
-   View average job compatibility scores.

## Technology stack

  Area             Technologies
  ---------------- ----------------------------------------------------------
  Language         Java 23
  Backend          Spring Boot, Spring Security, Spring Data JPA, Hibernate
  Authentication   JWT (JJWT), OAuth2 Client, BCrypt
  Database         H2
  Frontend         Vanilla JavaScript, HTML, CSS Grid, Fetch API
  Build            Gradle
  Testing          JUnit 5, Spring Boot Test

## Architecture and engineering highlights

-   REST API with DTOs separating API contracts from persistence
    entities.
-   Custom JWT authentication filter in the Spring Security filter
    chain.
-   Centralized exception handling with `@RestControllerAdvice`.
-   Resume skill extraction using normalized text and dictionary
    matching.
-   Job compatibility scoring using set-based skill matching.
-   Database uniqueness constraints to protect against duplicate
    applications.
-   Unit and integration testing for core workflows.

## Getting started

### Prerequisites

-   Java 23
-   Git
-   A terminal
-   Gradle Wrapper included in this repository

### Clone the repository

``` bash
git clone https://github.com/pinkyprerana/ai_enabled_career_pilot.git
cd ai_enabled_career_pilot
```

### Configure environment variables

Review `.env.example` and provide the required values in your local
environment. Do not commit real credentials, OAuth client secrets,
signing keys, or other sensitive values.

### Run the application

On macOS or Linux:

``` bash
./gradlew bootRun
```

On Windows:

``` bat
gradlew.bat bootRun
```

If your local setup requires a different configuration, check the
application configuration files and `.env.example` before starting the
app.

## Demo video

A screen-recorded walkthrough can be added here after recording the
running application.

**Suggested demo flow:** job search and filters → resume upload and
extracted skills → job match score → Auto-Apply workflow → application
analytics.

To add a video to this README, upload it to a video host or attach it to
a GitHub issue/release, then replace this section with the resulting
link. Avoid adding secrets, personal candidate data, or real credentials
to the recording.

## Screenshots

Add screenshots of the running application here. The GitHub README
editor screenshot is not an application screenshot, so it is
intentionally not included.

## Future improvements

-   Improve matching with semantic skill similarity and explainable
    recommendations.
-   Add persistent token revocation storage for deployments requiring
    revocation across instances.
-   Expand application status tracking and candidate notifications.
-   Add deployment configuration and end-to-end test coverage.

## Feedback

Suggestions, issues, and technical feedback are welcome. Please open an
issue in the repository to discuss improvements.

------------------------------------------------------------------------

Built as a hands-on project to explore Spring Boot, application
security, REST API design, and job-matching workflows.
