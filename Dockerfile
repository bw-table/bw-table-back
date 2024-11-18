FROM amazoncorretto:17

ARG JAR_FILE=build/libs/*jar

COPY ./build/libs/bw-table-back-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]