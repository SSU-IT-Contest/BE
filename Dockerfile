# 공통 Dockerfile (예: 모듈마다 복붙해서 사용)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# JAR 복사
COPY build/libs/*SNAPSHOT.jar app.jar
# 포트는 각자 다름 (예: 8080, 8761 등)
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]

# [Claude 제안] OOM 방지를 위한 JVM 메모리 제한 옵션
# ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:MetaspaceSize=128m", "-XX:MaxMetaspaceSize=256m", "-jar"]
# CMD ["app.jar"]
