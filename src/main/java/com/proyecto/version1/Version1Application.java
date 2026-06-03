package com.proyecto.version1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@org.springframework.scheduling.annotation.EnableAsync
public class Version1Application {

	public static void main(String[] args) {
		SpringApplication.run(Version1Application.class, args);
	}

}
