FROM arm32v7/eclipse-temurin:17-jre
COPY target/telegram-bot.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
