# -----------------------------
# 빌드 단계
# -----------------------------
# Gradle Wrapper(./gradlew) 사용 — arm64 / amd64 모두 호환되는 Temurin 기반
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src

# Gradle 캐시 최적화 (wrapper & 설정만 먼저 복사)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --version

# 나머지 소스 복사 및 빌드
COPY . .
RUN ./gradlew clean bootJar -x test

# -----------------------------
# 런타임 단계
# -----------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Build stage에서 만들어진 JAR 복사
COPY --from=build /src/build/libs/*-SNAPSHOT.jar app.jar

# 환경변수 및 포트 설정
ENV JAVA_OPTS=""
EXPOSE 8080

# 컨테이너 실행 시 Spring Boot 앱 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]