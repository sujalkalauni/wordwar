# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Cache Maven dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S wordwar && adduser -S wordwar -G wordwar

COPY --from=builder /app/target/*.jar app.jar

RUN chown wordwar:wordwar app.jar
USER wordwar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
