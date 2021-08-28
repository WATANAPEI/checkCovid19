package dev.wpei.checkcovid19;

import dev.wpei.checkcovid19.controller.LocalExecutionHandler;
import dev.wpei.checkcovid19.infra.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

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
		LocalDate date = DateUtil.dateToFetchDate(specifiedDate);
		LocalExecutionHandler.execute(date);
	}


}
