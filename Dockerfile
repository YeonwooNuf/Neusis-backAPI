# # -------- build stage (Gradle 내장 이미지로 의존성 캐시 효과 극대화) --------
# FROM gradle:8.10.2-jdk17-jammy AS build
# WORKDIR /workspace
#
# # 1) 캐시가 덜 변하는 것들부터 복사
# COPY settings.gradle build.gradle gradle.properties ./
# COPY gradle ./gradle
#
# # 2) 의존성만 미리 받아서 레이어 캐시화 (실패해도 캐시 남도록)
# RUN gradle --no-daemon dependencies || true
#
# # 3) 소스는 마지막에 복사 → 소스 변경 시 여기서만 캐시 무효
# COPY src ./src
#
# # 4) 빌드 (테스트 스킵으로 속도↑)
# RUN gradle clean bootJar -x test --no-daemon
#
# # -------- runtime stage --------
# FROM eclipse-temurin:17-jre-jammy
# WORKDIR /app
# COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar
# ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
# EXPOSE 8080
# ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]

# -------- runtime stage only --------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 로컬에서 빌드한 JAR만 복사
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]