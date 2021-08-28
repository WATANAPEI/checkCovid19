package dev.wpei.checkcovid19.controller;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.wpei.checkcovid19.service.CheckCovid19Service;

import java.util.Map;

public class LambdaHandler implements RequestHandler<Map<String, String>, String> {
    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        String response = "200 ok.";
        ObjectMapper mapper = new ObjectMapper();

        try {
            //logger.log("Env: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(System.getenv()));
            //logger.log("Context: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(context));
            logger.log("Event: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event));
            logger.log("Event type: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getClass()));

            CheckCovid19Service checkCovid19Service = new CheckCovid19Service();
            try {

            } catch(Exception e) {
                logger.log("application failed." + e);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}

