# Create jar file
FROM maven:3.8.4-amazoncorretto-11 AS build
COPY . .
RUN mvn install -DskipTests=true
# Build docker image with lambda execute env
FROM public.ecr.aws/lambda/java:11
COPY --from=build target/classes ${LAMBDA_TASK_ROOT}
COPY --from=build target/dependency/* ${LAMBDA_TASK_ROOT}/lib/
CMD [ "dev.wpei.checkcovid19.controller.LambdaHandler::handleRequest" ]