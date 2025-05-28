FROM arm32v7/eclipse-temurin:17-jre
COPY target/telegram-bot-1.0.0.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
