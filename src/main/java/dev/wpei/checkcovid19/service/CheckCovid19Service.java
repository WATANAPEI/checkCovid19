package dev.wpei.checkcovid19.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import dev.wpei.checkcovid19.infra.DataFetcher;
import dev.wpei.checkcovid19.infra.DateUtil;
import dev.wpei.checkcovid19.model.CovidPatientItem;
import dev.wpei.checkcovid19.model.Response;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;


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
        String todayString = DateUtil.generateDateFrom(0);
        String dateOfData = DateUtil.generateDateFrom(3);
        // get response
        List<CovidPatientItem> itemList = fetchPatientsNumberPerDate(dateOfData).getItemList();
        //save response data as csv file
        final String saveFilePath = "target/patients" + todayString + ".csv";
        File saveFile = new File(saveFilePath);
        saveCsvToLocal(itemList, saveFile);
        String bucketName = "lambda-artifacts-fs2wafw43";
        S3Service s3Service = new S3Service(bucketName, Region.US_EAST_2);
        s3Service.saveCsvToS3(itemList, saveFile);
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
    private void saveCsvToLocal(List<CovidPatientItem> covidPatientItemList, File saveFilePath) {
        log.debug("Translating response to csv...");
        try ( Writer writer = new FileWriter(saveFilePath)) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            try {
                beanToCsv.write(covidPatientItemList);
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
