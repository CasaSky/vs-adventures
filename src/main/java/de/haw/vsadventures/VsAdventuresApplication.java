package de.haw.vsadventures;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class VsAdventuresApplication {

	//@Autowired
	public static RestTemplate restTemplate;

	/*@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}*/

	public static void main(String[] args) {

		restTemplate = new RestTemplate();
		Client client = new Client(restTemplate);
		try {
			client.menu();
		} catch (Exception e) {
			e.printStackTrace();
		}
		SpringApplication.run(VsAdventuresApplication.class, args);
	}
}
