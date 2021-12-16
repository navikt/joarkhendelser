FROM navikt/java:17

COPY app/target/app.jar /app/app.jar

ENV JAVA_OPTS="-Xmx512m \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"
