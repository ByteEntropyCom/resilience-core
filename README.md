# resilience-core

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Resilience4j](https://img.shields.io/badge/Resilience4j-2.2.0-5B6B95?style=for-the-badge)](https://resilience4j.readme.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

[![CI/CD Pipeline](https://github.com/ByteEntropyCom/resilience-core/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/ByteEntropyCom/resilience-core/actions)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/ByteEntropyCom/resilience-core?style=social)](https://github.com/ByteEntropyCom/resilience-core/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/ByteEntropyCom/resilience-core?style=social)](https://github.com/ByteEntropyCom/resilience-core/network/members)

*A robust Spring Boot library for building resilient applications with fault tolerance, circuit breakers, and retry mechanisms*

</div>

---

## Overview

**resilience-core** is an enterprise-grade Java library built on top of Spring Boot 3.2.4 and Resilience4j 2.2.0. It provides comprehensive fault tolerance patterns including circuit breakers, retries, timeouts, bulkheads, and rate limiting to help you build resilient, production-ready applications.

## Key Features

✨ **Circuit Breaker Pattern** - Prevent cascading failures with intelligent circuit breaking  
🔄 **Retry Mechanism** - Automatic retry logic with exponential backoff  
⏱️ **Timeout Management** - Gracefully handle long-running operations  
🛡️ **Bulkhead Isolation** - Limit resource consumption with thread and connection pools  
📊 **Rate Limiting** - Control request flow and prevent resource exhaustion  
🔍 **Observability** - Built-in metrics and monitoring integration  
🚀 **Spring Boot Integration** - Seamless auto-configuration and bean management  
📝 **AOP Support** - Declarative fault tolerance with annotations  

## Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 3.2.4 | Application framework |
| Resilience4j | 2.2.0 | Fault tolerance library |
| Maven | 3.8+ | Build automation |
| JUnit 5 | Latest | Testing framework |
| Awaitility | 4.2.0 | Async testing |

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.8.0 or higher
- Spring Boot 3.2.4+

### Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.byteentropy</groupId>
    <artifactId>resilience-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

#### 1. Circuit Breaker Example

```java
@Service
@Slf4j
public class UserService {
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(String userId) {
        // Your service logic
        return userRepository.findById(userId).orElseThrow();
    }
    
    private User getUserFallback(String userId, Exception ex) {
        log.warn("Circuit breaker triggered for user: {}", userId);
        return User.builder()
            .id(userId)
            .name("Default User")
            .build();
    }
}
```

#### 2. Retry with Backoff

```java
@Service
@Slf4j
public class PaymentService {
    
    @Retry(name = "paymentRetry", fallbackMethod = "processPaymentFallback")
    public PaymentResult processPayment(Payment payment) {
        return externalPaymentAPI.process(payment);
    }
    
    private PaymentResult processPaymentFallback(Payment payment, Exception ex) {
        log.error("Payment failed after retries", ex);
        return PaymentResult.PENDING;
    }
}
```

#### 3. Timeout Protection

```java
@Service
@Slf4j
public class DataService {
    
    @Timeout(name = "dataServiceTimeout")
    public Data fetchData(String id) {
        return externalAPI.getData(id); // max 5 seconds
    }
}
```

#### 4. Rate Limiting

```java
@Service
@Slf4j
public class ApiController {
    
    @RateLimiter(name = "apiRateLimiter")
    public ResponseEntity<String> limitedEndpoint() {
        return ResponseEntity.ok("Success");
    }
}
```

## Configuration

Add to `application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        registerHealthIndicator: true
        slidingWindowSize: 100
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3

  retry:
    instances:
      paymentRetry:
        maxAttempts: 3
        waitDuration: 1000
        retryExceptions:
          - java.io.IOException

  timelimiter:
    instances:
      dataServiceTimeout:
        timeoutDuration: 5000

  ratelimiter:
    instances:
      apiRateLimiter:
        registerHealthIndicator: false
        limitRefreshPeriod: 1m
        limitForPeriod: 100

  bulkhead:
    instances:
      bulkheadService:
        maxConcurrentCalls: 10
        maxWaitDuration: 10ms
```

## Project Structure

```
resilience-core/
├── src/
│   ├── main/java/com/byteentropy/
│   │   ├── resilience/
│   │   ├── config/
│   │   ├── aspect/
│   │   └── util/
│   └── test/java/
├── pom.xml
├── README.md
├── LICENSE
└── .github/
    └── workflows/
        └── ci-cd.yml
```

## Running Tests

Execute the comprehensive test suite:

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=YourTestClass

# Run integration tests
mvn verify
```

## Building the Project

```bash
# Clean build
mvn clean package

# Skip tests (not recommended)
mvn package -DskipTests

# Build with all checks
mvn clean verify
```

## CI/CD Pipeline

This project includes an automated CI/CD pipeline with:

- ✅ **Build & Test** - Java 21 & 22 compatibility matrix
- 🔍 **Code Quality** - SonarQube analysis
- 🛡️ **Security** - OWASP Dependency Check
- 📦 **Artifact Publishing** - Maven Central deployment (on main branch)

View the workflow: [.github/workflows/ci-cd.yml](.github/workflows/ci-cd.yml)

## Code Quality & Security Metrics

- **Build Status**: See [Actions](https://github.com/ByteEntropyCom/resilience-core/actions)
- **Code Coverage**: Monitored via Codecov
- **Security Scanning**: OWASP Dependency-Check integration
- **Static Analysis**: SonarQube/SonarCloud

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Development Guidelines

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Write unit tests for all new features (minimum 80% coverage)
- Update documentation for API changes
- All PRs must pass CI/CD pipeline before merging

## Troubleshooting

### Common Issues

**Issue**: Tests failing with `CircuitBreakerOpenException`
```
Solution: Ensure proper CircuitBreaker reset between test cases
Use @DirtiesContext or manual reset in @After method
```

**Issue**: Timeout exceptions in integration tests
```
Solution: Increase timeout values or use Awaitility for async operations
```

**Issue**: Spring context not loading
```
Solution: Ensure all @Configuration classes are in component scan path
```

## Performance Considerations

- Circuit breaker sliding window: 100 calls (configurable)
- Default retry attempts: 3 with 1s wait duration
- Timeout defaults: 5 seconds (adjust per use case)
- Thread pool: Configure based on application load

## Roadmap

- [ ] Add distributed tracing support (Sleuth/Jaeger)
- [ ] Implement reactive (Project Reactor) variants
- [ ] Enhanced dashboard for monitoring
- [ ] GraphQL integration examples
- [ ] Kubernetes deployment guides

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support & Community

- 📧 **Issues**: [GitHub Issues](https://github.com/ByteEntropyCom/resilience-core/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/ByteEntropyCom/resilience-core/discussions)
- 🐦 **Follow**: [@ByteEntropyCom](https://twitter.com/ByteEntropyCom)

## Authors

**ByteEntropyCom** - *Initial work* - [GitHub Profile](https://github.com/ByteEntropyCom)

## Acknowledgments

- [Resilience4j](https://resilience4j.readme.io/) - Fault tolerance library
- [Spring Boot](https://spring.io/projects/spring-boot) - Framework
- [Spring Cloud](https://spring.io/projects/spring-cloud) - Cloud patterns

---

<div align="center">

Made with ❤️ by [ByteEntropyCom](https://github.com/ByteEntropyCom)

</div>
