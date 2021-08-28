package dev.wpei.checkcovid19.service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import dev.wpei.checkcovid19.model.DailyPatient;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class CsvService {
    private List<DailyPatient> dailyPatientList;

    public CsvService(List<DailyPatient> dailyPatientList) {
        this.dailyPatientList = dailyPatientList;
    }
    public String getCsvString(List<DailyPatient> patientList) {
        log.debug("Translating response to csv...");
        StringWriter stringWriter = new StringWriter();

        try(CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            String[] header = {"date", "pref", "patients"};
            csvWriter.writeNext(header);
            for(DailyPatient p: patientList) {
                csvWriter.writeNext(new String[]{p.getDate(), p.getName_jp(), String.valueOf(p.getNpatients())});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }


    public void saveCsvToLocal(Path saveFilePath) {
        try ( Writer writer = new FileWriter(saveFilePath.toFile())) {
            StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
            try {
                beanToCsv.write(dailyPatientList);
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
