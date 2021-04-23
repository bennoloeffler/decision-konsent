FROM openjdk:8-alpine

COPY target/uberjar/decision-konsent.jar /decision-konsent/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/decision-konsent/app.jar"]
