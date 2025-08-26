

FROM openjdk:21-jdk-slim
WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean && \
    addgroup --system spring && adduser --system spring --ingroup spring

COPY build/libs/ject*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring
EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1


ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=prod -Duser.timezone=Asia/Seoul -jar app.jar"]
