FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/install/ktor-final-task-server/ ./
EXPOSE 8080
CMD ["bin/ktor-final-task-server"]
