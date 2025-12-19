package com.grootan.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;
import org.springframework.cache.annotation.EnableCaching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EnableCaching
@SpringBootApplication
public class EmsApplication {

	private static final Logger log = LoggerFactory.getLogger(EmsApplication.class);

	public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		log.info("Default timezone set to UTC; starting EMS application");
		SpringApplication.run(EmsApplication.class, args);
		log.info("EMS application started");
	}

}
