package dev.wpei.checkcovid19.controller;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.wpei.checkcovid19.infra.DateUtil;
import dev.wpei.checkcovid19.model.DailyPatient;
import dev.wpei.checkcovid19.service.CheckCovid19Service;
import dev.wpei.checkcovid19.service.CsvService;
import dev.wpei.checkcovid19.service.S3Service;
import software.amazon.awssdk.regions.Region;

import java.util.List;

public class LambdaHandler implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String event, Context context) {
        LambdaLogger logger = context.getLogger();
        ObjectMapper mapper = new ObjectMapper();
        String response = null;

        try {
            //logger.log("Env: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(System.getenv()));
            //logger.log("Context: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(context));
            logger.log("Event: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event));
            logger.log("Event type: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getClass()));

            CheckCovid19Service checkCovid19Service = new CheckCovid19Service();
            String dateString = event;
            List<DailyPatient> dailyPatients = checkCovid19Service.fetchDailyPatients(dateString);
            CsvService csvService = new CsvService(dailyPatients);
            String csvString = csvService.getCsvString(dailyPatients);
            String bucketName = "lambda-artifacts-fs2wafw43";
            Region region = Region.US_EAST_2;
            S3Service s3Service = new S3Service(bucketName, region);
            String fileName = dateString + ".csv";
            s3Service.putFile(csvString, fileName);

            LambdaResponse lambdaResponse = new LambdaResponse(fileName);
            response = mapper.writeValueAsString(lambdaResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}

