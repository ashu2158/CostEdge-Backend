# Stage 1: Build the Spring Boot app
FROM eclipse-temurin:23.0.2_7-jdk AS builder

WORKDIR /app

# Copy only necessary files for dependency download first
COPY pom.xml .
COPY mvnw .
COPY .mvn/ .mvn/

# Make Maven wrapper executable
RUN chmod +x mvnw

# Preload dependencies
RUN ./mvnw dependency:go-offline


COPY src/ src/

# Build the JAR (skip tests to speed up)
RUN ./mvnw clean package -DskipTests

# Stage 2: Create minimal runtime image
FROM eclipse-temurin:23-jre-alpine-3.21

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
