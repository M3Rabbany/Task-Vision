package com.red.team.taskvisionapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TaskVisionApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskVisionApplication.class, args);
	}

}
