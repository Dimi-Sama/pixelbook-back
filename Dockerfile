# Build stage
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create non-root user
RUN addgroup --system javauser && adduser --system --ingroup javauser javauser
USER javauser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 