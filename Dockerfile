# Stage 1: compile with Maven
FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/UserService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8005
ENTRYPOINT ["java", "-jar", "app.jar"]
