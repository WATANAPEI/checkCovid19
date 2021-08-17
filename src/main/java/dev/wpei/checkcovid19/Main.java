package dev.wpei.checkcovid19;

import dev.wpei.checkcovid19.service.CheckCovid19Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

	public static void main(String[] args) {
		CheckCovid19Service checkCovid19Service = new CheckCovid19Service();
		try {
			checkCovid19Service.saveLatestPatientsNumberCSV();
		} catch(Exception e) {
		    log.error("application failed." + e);
		    System.exit(1);
		}

	}

}
