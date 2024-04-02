FROM node:21 AS ng-builder

RUN npm i -g @angular/cli

WORKDIR /ngapp

COPY frontend/package*.json .
COPY frontend/angular.json .
COPY frontend/tsconfig.* .
COPY frontend/ngsw-config.json .
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

ENV SPRING_REDIS_HOST= SPRING_REDIS_PORT=
ENV SPRING_REDIS_USERNAME= SPRING_REDIS_PASSWORD=
ENV SPRING_REDIS_DATABASE_USERS= GST=
ENV SPRING_DATASOURCE_URL= SPRING_DATA_MONGODB_URI=
ENV S3_ACCESS_KEY= S3_SECRET_KEY=
ENV S3_BUCKET_NAME= SENDGRID_EMAIL_APIKEY=
ENV STRIPE_SECRET_KEY= STRIPE_PUBLIC_KEY=
ENV STRIPE_WEBHOOK_ENDPOINT= JWT_SECRET_KEY= 
ENV WEBAPP_HOST_URL= TELEGRAM_API_KEY=
ENV PORT=8080

EXPOSE ${PORT}

ENTRYPOINT SERVER_PORT=${PORT} java -jar app.jar