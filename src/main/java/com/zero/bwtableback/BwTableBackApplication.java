package com.zero.bwtableback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BwTableBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BwTableBackApplication.class, args);
	}

}
