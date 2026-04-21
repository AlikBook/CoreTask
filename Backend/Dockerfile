FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jre
ARG APP_PORT=8080
ARG GRPC_PORT=9090
ENV SERVER_PORT=${APP_PORT}
ENV GRPC_SERVER_PORT=${GRPC_PORT}
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE ${APP_PORT}
EXPOSE ${GRPC_PORT}
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]