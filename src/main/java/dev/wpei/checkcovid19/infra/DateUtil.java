package dev.wpei.checkcovid19.infra;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static String generateDateFrom(int minusDay) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        OffsetDateTime dayBeforeYesterday = OffsetDateTime.now().minusDays(minusDay);
        return formatter.format(dayBeforeYesterday);
    }
}
