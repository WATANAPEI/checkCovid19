package dev.wpei.checkcovid19;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.wpei.checkcovid19.model.LambdaSampleEvent;
import org.apache.commons.lang3.RandomUtils;

import java.util.Map;

public class Handler implements RequestHandler<Map<String, String>, String> {
    @Override
    public String handleRequest(Map<String, String> event, Context context) {
        LambdaLogger logger = context.getLogger();
        String response = "200 ok desu.";
        ObjectMapper mapper = new ObjectMapper();

        try {
            logger.log("Env: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(System.getenv()));
            //logger.log("Context: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(context));
            logger.log("Event: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event));
            logger.log("Event type: " + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event.getClass()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}

