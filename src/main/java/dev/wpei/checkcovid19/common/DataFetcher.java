package dev.wpei.checkcovid19.common;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class DataFetcher {
    public DataFetcher() {}
    public String getHttpResponseBody(String url) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                log.warn("HTTP status is not ok.");
            }
            return response.body();
        } catch(IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
