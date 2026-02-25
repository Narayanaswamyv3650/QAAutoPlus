# QA Auto Plus - Quick Start Guide

## Project Overview

QA Auto Plus is a web application project for QA automation testing, built with:
- Java 20
- Jakarta Servlet API 6.0
- Jetty Web Server 11.0.15
- Jackson for JSON processing
- JUnit 5 for testing
- Selenium WebDriver for browser automation

## Project Location

```
C:\Users\njonnala\Automation\WaterProject\qaautoplus\
```

## Quick Commands

### Build the Project
```bash
cd C:\Users\njonnala\Automation\WaterProject\qaautoplus
mvn clean install
```

Or simply double-click: `build.bat`

### Run the Application

#### Method 1: Using the startup script
Double-click `start.bat` in the project folder

#### Method 2: Using Maven
```bash
cd C:\Users\njonnala\Automation\WaterProject\qaautoplus
mvn jetty:run
```

#### Method 3: Using Java directly
```bash
cd C:\Users\njonnala\Automation\WaterProject\qaautoplus
mvn clean package
java -cp target/classes;target/dependency/* com.qaautoplus.Main
```

### Run Tests
```bash
cd C:\Users\njonnala\Automation\WaterProject\qaautoplus
mvn test
```

## Access the Application

Once running, access the application at:
- **Main Page**: http://localhost:8080
- **API Status**: http://localhost:8080/api/status
- **API Info**: http://localhost:8080/api/info

## Project Structure

```
qaautoplus/
├── src/
│   ├── main/
│   │   ├── java/com/qaautoplus/
│   │   │   ├── Main.java                    # Application entry point
│   │   │   └── servlets/
│   │   │       ├── HomeServlet.java         # Home page servlet
│   │   │       └── ApiServlet.java          # API endpoints servlet
│   │   ├── resources/
│   │   │   └── application.properties       # Configuration file
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml                  # Servlet configuration
│   │       ├── css/
│   │       │   └── style.css                # Stylesheet
│   │       └── js/
│   │           └── app.js                   # JavaScript
│   └── test/
│       └── java/com/qaautoplus/
│           └── ApplicationTest.java         # Unit tests
├── pom.xml                                  # Maven configuration
├── build.bat                                # Build script
├── start.bat                                # Startup script
├── README.md                                # Documentation
└── .gitignore                              # Git ignore file
```

## Key Features

1. **Home Page**: Clean, responsive web interface
2. **REST API**: JSON-based API endpoints for integration
3. **Test Framework**: JUnit 5 test setup ready
4. **Selenium Support**: Browser automation capabilities included
5. **Easy Deployment**: WAR file generation for servlet containers

## Development Workflow

1. **Make code changes** in `src/main/java/`
2. **Test locally** using `mvn test`
3. **Build the project** using `build.bat` or `mvn clean package`
4. **Run the application** using `start.bat` or `mvn jetty:run`
5. **Access in browser** at http://localhost:8080

## Adding New Features

### Add a New Servlet
1. Create a new servlet class in `src/main/java/com/qaautoplus/servlets/`
2. Extend `HttpServlet`
3. Add servlet mapping in `web.xml` or register in `Main.java`

### Add Dependencies
1. Edit `pom.xml`
2. Add dependency in `<dependencies>` section
3. Run `mvn clean install`

### Add Test Cases
1. Create test class in `src/test/java/com/qaautoplus/`
2. Use JUnit 5 annotations
3. Run `mvn test`

## Troubleshooting

### Port Already in Use
If port 8080 is in use, edit `Main.java` and change the PORT constant, or edit `pom.xml` to change the Jetty port.

### Build Errors
Ensure you have:
- JDK 20 or higher installed
- Maven 3.6 or higher installed
- Internet connection for downloading dependencies

### Dependencies Not Downloading
Run: `mvn clean install -U` to force update dependencies

## Next Steps

1. Customize the home page in `HomeServlet.java`
2. Add your API endpoints in `ApiServlet.java` or create new servlets
3. Add your test automation scripts in `src/test/java/`
4. Configure application properties in `application.properties`
5. Deploy to production server using the generated WAR file

## Support

For more information, see the full README.md file in the project directory.

