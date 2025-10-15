package com.runpics.runpics_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RunpicsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RunpicsBackendApplication.class, args);
	}

}
