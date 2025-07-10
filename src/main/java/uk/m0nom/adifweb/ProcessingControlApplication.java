package uk.m0nom.adifweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "uk.m0nom.adifproc.db")
@EntityScan("uk.m0nom.adifproc.domain")
public class ProcessingControlApplication {
	public static void main(String[] args) {
		System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
		try {
			SpringApplication.run(ProcessingControlApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
