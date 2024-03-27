FROM node:21 AS ng-builder

RUN npm i -g @angular/cli

WORKDIR /ngapp

COPY frontend/package*.json .
COPY frontend/angular.json .
COPY frontend/tsconfig.* .
COPY frontend/src src

RUN npm ci && ng build


FROM maven:eclipse-temurin AS sb-builder

WORKDIR /sbapp

COPY backend/mvnw .
COPY backend/mvnw.cmd .
COPY backend/.mvn .mvn
COPY backend/src src
COPY backend/pom.xml .
COPY --from=ng-builder ngapp/dist/frontend/browser src/main/resources/static

RUN mvn package -Dmaven.test.skip=true


FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=sb-builder /sbapp/target/backend-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_REDIS_HOST=url SPRING_REDIS_PORT=0000
ENV SPRING_REDIS_USERNAME=default SPRING_REDIS_PASSWORD=password
ENV SPRING_REDIS_DATABASE_USERS=0 SPRING_REDIS_DATABASE_ORDERS=1
ENV SPRING_DATASOURCE_URL=url SPRING_DATA_MONGODB_URI=url
ENV S3_ACCESS_KEY=key S3_SECRET_KEY=key
ENV S3_BUCKET_NAME=name SENDGRID_EMAIL_APIKEY=key
ENV STRIPE_SECRET_KEY=key STRIPE_PUBLIC_KEY=key
ENV STRIPE_WEBHOOK_ENDPOINT=key WEBAPP_HOST_URL=url
ENV JWT_SECRET_KEY=key PORT=8080

EXPOSE ${PORT}

ENTRYPOINT SERVER_PORT=${PORT} java -jar app.jar