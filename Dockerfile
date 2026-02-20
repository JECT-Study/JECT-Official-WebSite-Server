FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 보안 계정 생성
RUN apk add --no-cache curl && \
    addgroup -S spring && adduser -S spring -G spring

# JAR 파일 복사
COPY build/libs/ject*.jar app.jar

# 권한 변경
RUN chown spring:spring app.jar

# 계정 전환
USER spring:spring

EXPOSE 8080

# 환경 변수 설정
ENV JAVA_OPTS="-XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# 프로파일을 환경변수로 분리
ENV SPRING_PROFILES_ACTIVE=prod

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
   CMD curl -f http://localhost:8080/health || exit 1

# 실행 명령어
ENTRYPOINT ["sh", "-c", "exec java -XX:MaxRAMPercentage=75.0 $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Duser.timezone=Asia/Seoul -jar app.jar"]