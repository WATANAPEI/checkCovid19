package dev.wpei.checkcovid19.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import dev.wpei.checkcovid19.common.DataFetcher;
import dev.wpei.checkcovid19.common.LogClient;
import dev.wpei.checkcovid19.model.Item;
import dev.wpei.checkcovid19.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CheckCovid19Service {
    private static final Logger logger = LoggerFactory.getLogger(CheckCovid19Service.class);
    private static final String BASE_COVID_INFO_URL = "https://opendata.corona.go.jp/api/Covid19JapanAll";
    DataFetcher dataFetcher;

    private ResourceBundle bundle = ResourceBundle.getBundle("application");
    public CheckCovid19Service() {
        dataFetcher = new DataFetcher();
    }
    public void echo(String echoString) {
        //System.out.println(echoString);
        LogClient.info(logger, "echoString: " + echoString + ", property: " + bundle.getString("test"));

    }
    public void run() {
            getAllCovidLog();
    }

    public String getAllCovidLog() {
        LogClient.debug(logger, "Fetching all covid log...");
        String body = dataFetcher.getHttpResponseBody(BASE_COVID_INFO_URL);
        LogClient.debug(logger, body);
        LogClient.debug(logger, "Data fetch finished.");
        return body;
    }
    public String getLatestCovidLog() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        OffsetDateTime dayBeforeYesterday = OffsetDateTime.now().minusDays(3);
        String todayString = formatter.format(dayBeforeYesterday);

        String urlToFetchLatestCovidInfo = BASE_COVID_INFO_URL + "?date=" + todayString;
        LogClient.info(logger, "URL: " + urlToFetchLatestCovidInfo);
        LogClient.debug(logger, "Fetching latest covid log...");
        String body = dataFetcher.getHttpResponseBody(urlToFetchLatestCovidInfo);
        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Response response = null;
        try {
            response = mapper.readValue(body, Response.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        LogClient.debug(logger, "Data fetch finished.");
        LogClient.debug(logger, "Translating response to csv...");
        try ( Writer writer = new FileWriter("test.csv")) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            try {
                beanToCsv.write(response.getItemList());
                LogClient.debug(logger, "Translation to csv finished.");
            } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
                throw new IllegalArgumentException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return body;
    }

}
