FROM openjdk:17-alpine

RUN apk add --no-cache bash unzip

WORKDIR /app

COPY target/universal/reservation-service-1.0-SNAPSHOT.zip /app/reservation-service.zip

RUN unzip /app/reservation-service.zip && rm /app/reservation-service.zip

ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV PLAY_PORT=${PLAY_PORT}
ENV PLAY_SECRET=${PLAY_SECRET}

RUN mv /app/reservation-service-1.0-SNAPSHOT /app/reservation-service

RUN chmod +x /app/reservation-service/bin/reservation-service

CMD ["/bin/bash", "-c", "exec /app/reservation-service/bin/reservation-service"]