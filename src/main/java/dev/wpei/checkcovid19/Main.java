package dev.wpei.checkcovid19;

import dev.wpei.checkcovid19.common.LogClient;
import dev.wpei.checkcovid19.service.CheckCovid19Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		CheckCovid19Service checkCovid19Service = new CheckCovid19Service();
		try {
			checkCovid19Service.getLatestCovidLog();
		} catch(Exception e) {
		    LogClient.error(logger, "application failed.");
		    System.exit(1);
		}

	}

}
