FROM ibm-semeru-runtimes:open-11-jre-focal
WORKDIR /app
COPY ./build/libs/saveg-local-market-api-SNAPSHOT.jar ./
CMD ["java", "-jar", "./saveg-local-market-api-SNAPSHOT.jar"]