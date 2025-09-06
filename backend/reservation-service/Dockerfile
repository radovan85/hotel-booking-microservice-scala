FROM openjdk:17-alpine

RUN apk add --no-cache bash unzip

WORKDIR /app

COPY target/universal/reservation-service-1.0-SNAPSHOT.zip /app/reservation-service.zip

RUN unzip /app/reservation-service.zip && rm /app/reservation-service.zip

ENV DB_URL=jdbc:mariadb://mariadb:3306/hotel-db
ENV DB_USERNAME=admin
ENV DB_PASSWORD=admin

RUN mv /app/reservation-service-1.0-SNAPSHOT /app/reservation-service

RUN chmod +x /app/reservation-service/bin/reservation-service

CMD ["/bin/bash", "-c", "/app/reservation-service/bin/reservation-service"]