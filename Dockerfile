# --- build stage ---
FROM gradle:8.7-jdk17-alpine AS build
WORKDIR /src

# gradle 캐시 최적화
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew --version

# 나머지 소스 복사 후 빌드
COPY . .
RUN ./gradlew clean bootJar -x test

# --- runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/build/libs/*-SNAPSHOT.jar app.jar

# 환경변수를 통해 설정 주입 (infra compose에서 전달)
ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]