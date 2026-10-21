package com.example.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class ProjectApplication {

	public static void main(String[] args) {
		validateEnvironmentVariables(System.getenv());
		SpringApplication.run(ProjectApplication.class, args);
	}

	static void validateEnvironmentVariables(Map<String, String> environment) {
		for (String name : new String[] {
				"DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET" }) {
			String value = environment.get(name);
			if (value != null && value.trim().equals("${" + name + "}")) {
				throw new IllegalStateException(name
						+ " is set to its literal placeholder. Configure its actual value in the runtime environment.");
			}
		}
	}

}
