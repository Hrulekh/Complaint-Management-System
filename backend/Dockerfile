FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
ENV MAVEN_OPTS="-Xmx300m -XX:MaxMetaspaceSize=128m"
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appuser && adduser -S appuser -G appuser
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
