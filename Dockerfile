FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]