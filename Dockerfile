# Create jar file
FROM maven/3.8.4-amazoncorretto-11 AS build
COPY . .
RUN ["maven", "build"]
# Build docker image with lambda execute env
FROM amazon/aws-lambda-java:latest
COPY --from=build target/check_covid19.jar .
CMD