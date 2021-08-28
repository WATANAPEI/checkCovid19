package dev.wpei.checkcovid19.controller;

import dev.wpei.checkcovid19.infra.DateUtil;
import dev.wpei.checkcovid19.model.DailyPatient;
import dev.wpei.checkcovid19.service.CheckCovid19Service;
import dev.wpei.checkcovid19.service.CsvService;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class LocalExecutionHandler {
    public static void execute(LocalDate date) {
        CheckCovid19Service checkCovid19Service = new CheckCovid19Service();
        String dateString = DateUtil.translateLocalDateToString(date);
        List<DailyPatient> dailyPatients = checkCovid19Service.fetchDailyPatients(dateString);
        CsvService csvService = new CsvService(dailyPatients);
        Path pathToSave = Path.of("target", dateString + ".csv");
        csvService.saveCsvToLocal(pathToSave);
    }
}
