FROM openjdk:17-alpine

RUN apk add --no-cache bash unzip

WORKDIR /app

COPY target/universal/room-service-1.0-SNAPSHOT.zip /app/room-service.zip

RUN unzip /app/room-service.zip && rm /app/room-service.zip

ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV PLAY_PORT=${PLAY_PORT}
ENV PLAY_SECRET=${PLAY_SECRET}

RUN mv /app/room-service-1.0-SNAPSHOT /app/room-service

RUN chmod +x /app/room-service/bin/room-service

CMD ["/bin/bash", "-c", "exec /app/room-service/bin/room-service"]