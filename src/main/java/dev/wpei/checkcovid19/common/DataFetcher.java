package dev.wpei.checkcovid19.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(DataFetcher.class);

    public DataFetcher() {}
    public String getHttpResponseBody(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                LogClient.warn(logger, "HTTP status is not ok.");
            }
            return response.body();
        } catch(IOException | InterruptedException e) {
            LogClient.error(logger, e.getMessage());
            throw e;
        }
    }
}
