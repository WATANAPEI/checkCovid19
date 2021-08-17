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

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
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
        String todayString = generateDateFrom(3);
        // get response
        Response response = fetchPatientsNumberPerDate(todayString);
        //save response data as csv file
        saveCsvToLocal(response.getItemList(), todayString);
    }
    private String generateDateFrom(int minusDay) {
        if (minusDay >= 0) {
            throw new IllegalArgumentException("minusDay must be less than 0.");
        }
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
    private void saveCsvToLocal(List<Item> itemList, String todayString) {
        final String saveFilePath = "target/patients" + todayString + ".csv";
        log.debug("Translating response to csv...");
        try ( Writer writer = new FileWriter(saveFilePath)) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            try {
                beanToCsv.write(itemList);
                log.debug("Translation to csv finished.");
            } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
                throw new IllegalArgumentException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
