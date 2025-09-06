FROM openjdk:17-alpine

RUN apk add --no-cache bash unzip

WORKDIR /app

COPY target/universal/room-service-1.0-SNAPSHOT.zip /app/room-service.zip

RUN unzip /app/room-service.zip && rm /app/room-service.zip

ENV DB_URL=jdbc:mariadb://mariadb:3306/hotel-db
ENV DB_USERNAME=admin
ENV DB_PASSWORD=admin

RUN mv /app/room-service-1.0-SNAPSHOT /app/room-service

RUN chmod +x /app/room-service/bin/room-service

CMD ["/bin/bash", "-c", "/app/room-service/bin/room-service"]