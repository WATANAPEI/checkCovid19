package dev.wpei.checkcovid19.infra;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateUtil {
    public static String generateDateFrom(int minusDay) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        OffsetDateTime dayBeforeYesterday = OffsetDateTime.now().minusDays(minusDay);
        return formatter.format(dayBeforeYesterday);
    }
    public static String translateLocalDateToString(LocalDate dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return formatter.format(dateTime);
    }
    /**
     * set default date
     * @return
     */
    public static LocalDate dateToFetchDate(String arg) {
        if(arg == null) {
            //return current time as default time
            return LocalDate.now();
        }

        LocalDate specifiedDate = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            specifiedDate = LocalDate.parse(arg, formatter);
        } catch(DateTimeParseException e) {
            log.error("Invalid datetime format. Valid format: yyyyMMdd (ex. 20210823)");
            log.error(e.getMessage());
            System.exit(1);
        }
        return specifiedDate;
    }
}

