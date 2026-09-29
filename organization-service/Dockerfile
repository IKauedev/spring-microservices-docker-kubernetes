ARG MVN_VERSION=3.9.9
ARG JDK_VERSION=17

FROM maven:${MVN_VERSION}-eclipse-temurin-${JDK_VERSION} as build

WORKDIR /build
COPY pom.xml .
# create a layer with all of the Manven dependencies, first time it takes a while consequent call are very fast
RUN mvn dependency:go-offline

COPY ./pom.xml /tmp/
COPY ./src /tmp/src/
WORKDIR /tmp/
# build the project
RUN mvn clean package

# extract JAR layers (the old "layertools" jarmode was removed in Spring
# Boot 4.1; "tools" is its replacement)
WORKDIR /tmp/target
RUN cp *.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# runtime image
FROM gcr.io/distroless/java17-debian13:nonroot as runtime

USER nonroot:nonroot
WORKDIR /application

# copy layers from build image to runtime image as nonroot user
COPY --from=build --chown=nonroot:nonroot /tmp/target/extracted/dependencies/ ./
COPY --from=build --chown=nonroot:nonroot /tmp/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=nonroot:nonroot /tmp/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=nonroot:nonroot /tmp/target/extracted/application/ ./

EXPOSE 8080

ENV _JAVA_OPTIONS "-XX:MinRAMPercentage=60.0 -XX:MaxRAMPercentage=90.0 \
-Djava.security.egd=file:/dev/./urandom \
-Djava.awt.headless=true -Dfile.encoding=UTF-8 \
-Dspring.output.ansi.enabled=ALWAYS \
-Dspring.profiles.active=default"

# set entrypoint to layered Spring Boot application
ENTRYPOINT ["java", "-jar", "application.jar"]
