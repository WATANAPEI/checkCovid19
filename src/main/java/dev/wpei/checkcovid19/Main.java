package dev.wpei.checkcovid19;

import dev.wpei.checkcovid19.controller.LocalExecutionHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Entry point of local execution
 */
@Slf4j
public class Main {
	public static void main(String[] args) {
		if(args.length > 1) {
			log.error("Number of argument must be 0 or 1.");
			log.error("arg0: program name, optional arg1: date(format: yyyyyMMdd)");
			System.exit(1);
		}

		String specifiedDate = null;
		if(args.length == 1) {
			specifiedDate = args[0];
		}
		LocalDate date = dateToFetchDate(specifiedDate);
		LocalExecutionHandler.execute(date);
	}

	/**
	 * set default date
	 * @return
	 */
	private static LocalDate dateToFetchDate(String arg) {
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
