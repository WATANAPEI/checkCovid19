package dev.wpei.checkcovid19.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.wpei.checkcovid19.infra.DataFetcher;
import dev.wpei.checkcovid19.infra.DateUtil;
import dev.wpei.checkcovid19.controller.Covid19DailyPatientsResource;
import dev.wpei.checkcovid19.model.DailyPatient;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;


@Slf4j
public class CheckCovid19Service {

    DataFetcher dataFetcher;

    private ResourceBundle bundle = ResourceBundle.getBundle("application");
    public CheckCovid19Service() {
        dataFetcher = new DataFetcher();
    }

    public void saveLatestPatientsNumberCSV() {
        // generate date string format: yyyyMMdd
        String todayString = DateUtil.generateDateFrom(0);
        String dateOfData = DateUtil.generateDateFrom(3);
        // get response
        List<DailyPatient> itemList = fetchDailyPatients(dateOfData);
        //save response data as csv file
        Path saveFilePath = Path.of("target", "patients" + todayString + ".csv");
        CsvService csvService = new CsvService(itemList);
        csvService.saveCsvToLocal(saveFilePath);
        // upload csv file
        String bucketName = "lambda-artifacts-fs2wafw43";
        S3Service s3Service = new S3Service(bucketName, Region.US_EAST_2);
        s3Service.putFile(saveFilePath);
    }

    public List<DailyPatient> fetchDailyPatients(String dateString) {
        final String BASE_COVID_INFO_URL = "https://opendata.corona.go.jp/api/Covid19JapanAll";
        String urlToFetchLatestCovidInfo = BASE_COVID_INFO_URL + "?date=" + dateString;
        log.info("URL: " + urlToFetchLatestCovidInfo);
        log.debug("Fetching latest covid log...");
        String body = dataFetcher.getHttpResponseBody(urlToFetchLatestCovidInfo);
        ObjectMapper mapper = new ObjectMapper();
        Covid19DailyPatientsResource covid19DailyPatientsResource = null;
        try {
            covid19DailyPatientsResource = mapper.readValue(body, Covid19DailyPatientsResource.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        log.debug("Data fetch finished.");
        return covid19DailyPatientsResource.getDailyPatientList();

    }

}
