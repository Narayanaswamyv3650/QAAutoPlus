# QA Auto Plus Web Application

A comprehensive QA automation testing platform built with Java and Jetty.

## Features

- **Test Automation**: Automate your testing workflows with ease
- **API Testing**: Test and validate your APIs efficiently
- **Reporting**: Generate comprehensive test reports
- **REST API**: Built-in REST API for integration

## Project Structure

```
qaautoplus/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── qaautoplus/
│   │   │           ├── Main.java
│   │   │           └── servlets/
│   │   │               ├── HomeServlet.java
│   │   │               └── ApiServlet.java
│   │   ├── resources/
│   │   │   └── application.properties
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       ├── css/
│   │       │   └── style.css
│   │       └── js/
│   │           └── app.js
│   └── test/
│       └── java/
└── pom.xml
```

## Technologies Used

- Java 20
- Jakarta Servlet API 6.0
- Jetty 11.0.15 (Embedded Web Server)
- Jackson 2.15.2 (JSON Processing)
- JUnit 5 (Testing)
- Selenium WebDriver 4.16.1 (Browser Automation)

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 20 or higher
- Apache Maven 3.6 or higher

### Building the Project

```bash
cd qaautoplus
mvn clean install
```

### Running the Application

#### Option 1: Using Maven Jetty Plugin

```bash
mvn jetty:run
```

The application will be available at: `http://localhost:8080/qaautoplus`

#### Option 2: Using the Main Class

```bash
mvn clean package
java -cp target/classes:target/dependency/* com.qaautoplus.Main
```

The application will be available at: `http://localhost:8080`

### Running Tests

```bash
mvn test
```

## API Endpoints

- `GET /api/status` - Check API status
- `GET /api/info` - Get application information

### Example API Request

```bash
curl http://localhost:8080/api/status
```

Response:
```json
{
  "status": "OK",
  "message": "QA Auto Plus API is running",
  "timestamp": "2026-02-25T00:00:00",
  "version": "1.0.0"
}
```

## Development

### Adding New Servlets

1. Create a new servlet class in `src/main/java/com/qaautoplus/servlets/`
2. Extend `HttpServlet`
3. Register the servlet in `web.xml` or use annotations
4. Add the servlet mapping in `Main.java` if using embedded Jetty

### Adding Dependencies

Edit `pom.xml` and add the dependency in the `<dependencies>` section:

```xml
<dependency>
    <groupId>group-id</groupId>
    <artifactId>artifact-id</artifactId>
    <version>version</version>
</dependency>
```

Then run:
```bash
mvn clean install
```

## License

Copyright © 2026 QA Auto Plus. All rights reserved.

## Contact

For questions or support, please contact the development team.

