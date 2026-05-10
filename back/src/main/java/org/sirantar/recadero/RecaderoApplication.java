package org.sirantar.recadero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class RecaderoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecaderoApplication.class, args);
	}

}
