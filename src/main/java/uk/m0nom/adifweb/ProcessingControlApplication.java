package uk.m0nom.adifweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProcessingControlApplication {
	public static void main(String[] args) {
		System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
		SpringApplication.run(ProcessingControlApplication.class, args);
	}
}
