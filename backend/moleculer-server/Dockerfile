FROM node:22-alpine

WORKDIR /app

# Instaliraj curl odmah nakon što baza bude spremna
RUN apk add --no-cache curl

COPY package*.json ./
RUN npm install --production

COPY . .

EXPOSE 3400

CMD ["node", "api/api.js"]
