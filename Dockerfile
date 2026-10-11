# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY settings.xml .
COPY pom.xml .
RUN mvn -s settings.xml -q -B dependency:go-offline
COPY src ./src
RUN mvn -s settings.xml -q -B clean package -Dmaven.test.skip=true

# ---------- Run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx300m"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
