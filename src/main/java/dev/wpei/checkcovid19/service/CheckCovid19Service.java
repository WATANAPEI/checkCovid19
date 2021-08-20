package dev.wpei.checkcovid19.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import dev.wpei.checkcovid19.common.DataFetcher;
import dev.wpei.checkcovid19.model.Item;
import dev.wpei.checkcovid19.model.Response;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.profiles.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.UUID;


@Slf4j
public class CheckCovid19Service {
    private static final String BASE_COVID_INFO_URL = "https://opendata.corona.go.jp/api/Covid19JapanAll";
    DataFetcher dataFetcher;

    private ResourceBundle bundle = ResourceBundle.getBundle("application");
    public CheckCovid19Service() {
        dataFetcher = new DataFetcher();
    }

    public String getAllCovidLog() {
        log.debug("Fetching all covid log...");
        String body = dataFetcher.getHttpResponseBody(BASE_COVID_INFO_URL);
        log.debug(body);
        log.debug("Data fetch finished.");
        return body;
    }
    public void saveLatestPatientsNumberCSV() {
        // generate date string format: yyyyMMdd
        String todayString = generateDateFrom(0);
        String dateOfData = generateDateFrom(3);
        // get response
        Response response = fetchPatientsNumberPerDate(dateOfData);
        //save response data as csv file
        final String saveFilePath = "target/patients" + todayString + ".csv";
        File saveFile = new File(saveFilePath);
        saveCsvToLocal(response.getItemList(), saveFile);
        saveCsvToS3(response.getItemList(), saveFile);
    }

    private String generateDateFrom(int minusDay) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        OffsetDateTime dayBeforeYesterday = OffsetDateTime.now().minusDays(minusDay);
        return formatter.format(dayBeforeYesterday);
    }
    private Response fetchPatientsNumberPerDate(String dateString) {
        String urlToFetchLatestCovidInfo = BASE_COVID_INFO_URL + "?date=" + dateString;
        log.info("URL: " + urlToFetchLatestCovidInfo);
        log.debug("Fetching latest covid log...");
        String body = dataFetcher.getHttpResponseBody(urlToFetchLatestCovidInfo);
        ObjectMapper mapper = new ObjectMapper();
        Response response = null;
        try {
            response = mapper.readValue(body, Response.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        log.debug("Data fetch finished.");
        return response;

    }
    private void saveCsvToS3(List<Item> itemList, File saveFile) {
        String bucketName = "lambda-artifacts-fs2wafw43";
        String objectKey = UUID.randomUUID().toString();
        saveCsvToLocal(itemList, saveFile);
        log.debug("Putting object "+ objectKey +" into bucket "+bucketName);
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("default");
        try(S3Client s3Client = S3Client.builder()
                .region(Region.US_EAST_2)
                .credentialsProvider(credentialsProvider)
                .build();) {

            try {
                String result = putS3Object(s3Client, bucketName, objectKey, saveFile);
                log.debug("Tag info: " +result);


            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

    }
    private String putS3Object(S3Client s3, String bucketName, String objectKey, File localFile) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectResponse putObjectResponse = s3.putObject(putObjectRequest, RequestBody.fromFile(localFile));
            return putObjectResponse.eTag();
        } catch(S3Exception e) {
            throw new IOException(e);
        }
    }
    private void saveCsvToLocal(List<Item> itemList, File saveFilePath) {
        log.debug("Translating response to csv...");
        try ( Writer writer = new FileWriter(saveFilePath)) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            try {
                beanToCsv.write(itemList);
                writer.close();
                log.debug("Translation to csv finished.");
            } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
                throw new IllegalArgumentException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
