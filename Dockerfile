FROM gcr.io/distroless/java17-debian12:nonroot

COPY app/target/app.jar /app/app.jar
WORKDIR /app

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS="-Xmx512m -Dspring.profiles.active=nais"

CMD ["app.jar"]