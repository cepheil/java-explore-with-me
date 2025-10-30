FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV SERVER_PORT=9090
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://stats-db:5432/ewm-stats
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV TZ=UTC
EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
