FROM gcr.io/distroless/java21-debian12:nonroot

COPY app/target/app.jar /app/app.jar
WORKDIR /app

ENV TZ="Europe/Oslo"
ENV JAVA_OPTS="-Xmx256m -Dspring.profiles.active=nais"

CMD ["app.jar"]