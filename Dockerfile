# build stage
FROM gradle:8.14.3-jdk17 AS build

WORKDIR /app

# 필요한 모든 빌드 파일 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 실행 권한 부여 및 검증
RUN chmod +x ./gradlew && ls -la ./gradlew

# 소스 코드 복사 후 바로 빌드
COPY src ./src

RUN ./gradlew clean build -x test

# final stage
FROM amazoncorretto:17

ENV PROJECT_NAME=discodeit \
    PROJECT_VERSION=1.2-M8 \
    JVM_OPTS=""

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]