FROM eclipse-temurin:21-jre
ARG JAR_PATH=shareit-gateway/target/*.jar
COPY ${JAR_PATH} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
