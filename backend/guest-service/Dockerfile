FROM openjdk:17-alpine

RUN apk add --no-cache bash unzip

WORKDIR /app

COPY target/universal/guest-service-0.1.0-SNAPSHOT.zip /app/guest-service.zip

RUN unzip /app/guest-service.zip && rm /app/guest-service.zip

ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}

RUN mv /app/guest-service-0.1.0-SNAPSHOT /app/guest-service

RUN chmod +x /app/guest-service/bin/guest-service

CMD ["/bin/bash", "-c", "exec /app/guest-service/bin/guest-service"]