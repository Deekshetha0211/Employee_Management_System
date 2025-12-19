FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy the entire project (pom.xml, mvnw, src, etc.)
COPY .. .

# Ensure the Maven wrapper is executable
RUN chmod +x mvnw

# Build the Spring Boot jar
RUN ./mvnw -q clean test package

# ---- Runtime image ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
