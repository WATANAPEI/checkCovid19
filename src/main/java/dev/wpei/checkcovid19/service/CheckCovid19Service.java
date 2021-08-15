package dev.wpei.checkcovid19.service;

import dev.wpei.checkcovid19.common.DataFetcher;
import dev.wpei.checkcovid19.common.LogClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    public void run() throws IOException, InterruptedException {
        try {
            getAllCovidLog();
        } catch(IOException | InterruptedException e) {
            logger.error(e.getMessage());

        }
    }
    public String getAllCovidLog() throws IOException, InterruptedException {
        logger.debug("Fetching all covid log...");
        String body = dataFetcher.getHttpResponseBody(BASE_COVID_INFO_URL);
        logger.debug(body);
        logger.debug("Data fetch finished.");
        return body;
    }
    public String getLatestCovidLog() throws IOException, InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        OffsetDateTime dayBeforeYesterday = OffsetDateTime.now().minusDays(3);
        String todayString = formatter.format(dayBeforeYesterday);

        String urlToFetchLatestCovidInfo = BASE_COVID_INFO_URL + "?date=" + todayString;
        LogClient.info(logger, "URL: " + urlToFetchLatestCovidInfo);
        logger.debug("Fetching latest covid log...");
        String body = dataFetcher.getHttpResponseBody(urlToFetchLatestCovidInfo);
        logger.debug(body);
        logger.debug("Data fetch finished.");
        return body;
    }

}
