# 공통 Dockerfile (예: 모듈마다 복붙해서 사용)
FROM openjdk:17-jdk-slim
WORKDIR /app

# JAR 복사
COPY build/libs/*SNAPSHOT.jar app.jar
# 포트는 각자 다름 (예: 8080, 8761 등)
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]