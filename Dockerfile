# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-20 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Runtime stage ----
FROM eclipse-temurin:20-jre-alpine
WORKDIR /app
COPY --from=build /app/target/qaautoplus.jar ./qaautoplus.jar

# Cloud platforms inject PORT; default to 8088
ENV PORT=8088
EXPOSE ${PORT}

# Run the fat-jar — Jetty reads PORT from the environment automatically
ENTRYPOINT ["java", "-jar", "qaautoplus.jar"]

