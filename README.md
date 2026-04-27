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

[![CI/CD Pipeline](https://github.com/ByteEntropyCom/resilience-core/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/ByteEntropyCom/resilience-core/actions)

[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)
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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support & Community

- 📧 **Issues**: [GitHub Issues](https://github.com/ByteEntropyCom/resilience-core/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/ByteEntropyCom/resilience-core/discussions)
- 🐦 **Follow**: [@ByteEntropyCom](https://twitter.com/ByteEntropyCom)

## Authors

**ByteEntropyCom** - *Initial work* - [GitHub Profile](https://github.com/ByteEntropyCom)

---

<div align="center">

Made with ❤️ by [ByteEntropyCom](https://github.com/ByteEntropyCom)

</div>
