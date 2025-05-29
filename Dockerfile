# Use a lightweight Java 17 runtime for ARM32v7 (e.g. Raspberry Pi)
FROM arm32v7/eclipse-temurin:17-jre

# Copy the built JAR file into the container
COPY target/telegram-bot.jar /app.jar

# Run the application
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx128m", "-jar", "/app.jar"]

