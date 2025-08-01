# Payment Integration

A Java Maven project for payment integration services.

## Project Structure

This is a standard Maven project with the following structure:

```
payment-integration/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/zuora/aitest/payment/
│   │   │       ├── PaymentApplication.java
│   │   │       ├── PaymentService.java
│   │   │       ├── PaymentRequest.java
│   │   │       └── PaymentResponse.java
│   │   └── resources/
│   │       └── logback.xml
│   └── test/
│       └── java/
│           └── com/zuora/aitest/payment/
│               └── PaymentServiceTest.java
└── README.md
```

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

To compile the project:
```bash
mvn compile
```

To run tests:
```bash
mvn test
```

To package the application:
```bash
mvn package
```

## Running the Application

To run the main application:
```bash
mvn exec:java -Dexec.mainClass="com.zuora.aitest.payment.PaymentApplication"
```

Or after packaging:
```bash
java -cp target/payment-integration-1.0.0-SNAPSHOT.jar com.zuora.aitest.payment.PaymentApplication
```

## Dependencies

- **SLF4J & Logback**: For logging
- **Jackson**: For JSON processing
- **Apache HttpClient**: For HTTP requests
- **JUnit 5**: For testing
- **Mockito**: For mocking in tests

## Features

- Basic payment processing service
- Request/Response model for payments
- Comprehensive logging
- Unit tests with JUnit 5
- Maven build configuration

## Development

To add new payment providers or extend functionality:

1. Extend the `PaymentService` class
2. Add new request/response models as needed
3. Update tests accordingly
4. Add dependencies to `pom.xml` if required
