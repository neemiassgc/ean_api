FROM ibm-semeru-runtimes:open-11-jre-focal
WORKDIR /app
COPY ./build/libs/grocery-products-api-SNAPSHOT.jar ./
CMD ["java", "-jar", "./grocery-products-api-SNAPSHOT.jar"]