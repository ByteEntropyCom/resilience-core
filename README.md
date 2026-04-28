# resilience-core

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-2.2.0-5B6B95?style=for-the-badge)](https://resilience4j.readme.io/)
[![H2 Database](https://img.shields.io/badge/Database-H2-003D6A?style=for-the-badge&logo=databricks&logoColor=white)](https://www.h2database.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

[![Virtual Threads](https://img.shields.io/badge/Architecture-Virtual_Threads-blue?style=flat-square&logo=java&logoColor=white)](https://openjdk.org/jeps/444)
[![Idempotency](https://img.shields.io/badge/FinTech-Idempotent_API-success?style=flat-square)](https://github.com/ByteEntropyCom/resilience-core)
[![Status](https://img.shields.io/badge/Production-Ready-brightgreen?style=flat-square)](https://github.com/ByteEntropyCom/resilience-core)

[![CI/CD Pipeline](https://github.com/ByteEntropyCom/resilience-core/actions/workflows/ci-cd.yml/badge.svg?branch=main)](https://github.com/ByteEntropyCom/resilience-core/actions)

[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/ByteEntropyCom/resilience-core?style=social)](https://github.com/ByteEntropyCom/resilience-core/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ByteEntropyCom/resilience-core?style=social)](https://github.com/ByteEntropyCom/resilience-core/network/members)

*A robust Spring Boot library for building resilient applications with fault tolerance, circuit breakers, and retry mechanisms*

</div>

---

## Overview

A high-performance Spring Boot library designed for extreme fault tolerance. Resilience Core leverages Java 21's Virtual Threads (Project Loom) to handle high-concurrency external integrations without the overhead of traditional thread management.

---

## 🚀 Key Features
### Project Loom Integration - 
Utilizes VirtualThreadPerTaskExecutor for lightweight, non-blocking asynchronous processing.

### Layered Resilience - 
Pre-configured pipeline combining:

   * **Circuit Breaker: Prevents system saturation during downstream outages.

   * **Rate Limiter: Protects upstream and downstream resources.

   * **Retry Pattern: Handles transient network glitches with configurable backoff.

   * **Time Limiter: Ensures requests don't hang indefinitely.

### Idempotency Protection - 
Built-in persistence layer via JPA to prevent duplicate processing of critical transactions.

### Real-time Observability - 
Full integration with Spring Boot Actuator and Prometheus for monitoring circuit states and failure rates.

---
## 🛠️ Technology Stack

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | 21 | Virtual Threads (Project Loom) & Modern Syntax |
| **Spring Boot** | 3.2.4 | Core Framework & Auto-configuration |
| **Resilience4j** | 2.2.0 | Fault Tolerance Patterns (Circuit Breaker, Retry, etc.) |
| **JPA / H2** | Latest | Transactional Idempotency & Persistence History |
| **Maven** | 3.9+ | Build Automation & Dependency Management |

---

## 📋 Prerequisites

* **JDK 21+** (Strictly required for Virtual Threads support)
* **Maven 3.9.x**
* **GitHub Secrets:** (Optional) Set `SONAR_TOKEN` and `NVD_API_KEY` for full CI/CD features.

---

## ⚙️ Installation & Setup
  1. Clone the Repository

  ```bash
    git clone https://github.com/ByteEntropyCom/resilience-core.git
    cd resilience-core
   ```
  
  2. Configure Environment (Optional)
     The application uses smart defaults, but you can override them in application.properties or via environment variables:

      ```Properties
      # Example: Adjusting the Circuit Breaker Threshold
      CB_FAILURE_THRESHOLD=50
      CB_WINDOW_SIZE=10
      ``` 
    3. Build, Test and Run

      ```bash
        mvn clean install
        mvn test
        mvn spring-boot:run
      ```
    4. Run with Docker:

        ```bash
        # Build the image
        docker build -t resilience-core .
        
        # Run the container
        docker run -p 8080:8080 resilience-core
       ```

 ---
 
  ## 🕹️ Usage Example
  The core logic resides in the ShieldPipeline, which orchestrates the resilience decorators.

      ```Java
      @Autowired
      private ShieldPipeline pipeline;
      
      public void processPayment(PaymentRequest request) {
          pipeline.execute(request)
              .thenAccept(response -> log.info("Result: " + response.status()))
              .exceptionally(ex -> {
                  log.error("Pipeline failed: " + ex.getMessage());
                  return null;
              });
      }
      ```

    ---
    
     ## 📊 Monitoring & Observability
      Once running, you can monitor the health of your resilience patterns:
      
      * **Health Status: GET /actuator/health (Shows Circuit Breaker state)
      * **Metrics: GET /actuator/prometheus (Detailed failure/success counters)
      * **H2 Console: localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)

      ## 🛡️ Security & Quality
      This project includes a rigorous CI/CD pipeline:
      * **OWASP Dependency Check: Scans for vulnerable libraries.
      * **SonarQube: Monitors code quality and technical debt.
      * **Automated Tests: Ensures 100% context loading and logic verification.

    ---
    
      ## 📄 License
      Distributed under the MIT License. See LICENSE for more information.
      
      ----
      Developed with ❤️ by ByteEntropy
