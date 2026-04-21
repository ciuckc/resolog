package com.resolog.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResologApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResologApplication.class, args);
	}

}
